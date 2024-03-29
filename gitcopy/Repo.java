package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Repo implements Serializable {

  private RepoStateMachine REPO_STATE_MACHINE;
  private Map<String, GitCopyStateMachine> BRANCH_STATE_MACHINES;
  private String CURRENT_BRANCH;
  private final String MAIN_DIRECTORY = FileUtils.findGitCopyRootDirectory().getAbsolutePath();
  private final String GITCOPY_DIRECTORY = MAIN_DIRECTORY + File.separator + ".gitcopy";
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";
  static final String COMMIT_INIT_SHA1 = "1000000000000000000000000000000000000001";
  private Map<String, Map<String, Blob>> branchesFileBlobMap;
  private final String BLOB_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".blobs";
  private final String STAGED_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".staging";
  private final String COMMIT_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".commits";

  public Repo() {
    REPO_STATE_MACHINE = new RepoStateMachine();
    BRANCH_STATE_MACHINES = new HashMap<>();
    CURRENT_BRANCH = "master";
    addMasterBranchToStateMachine();
    branchesFileBlobMap = new HashMap<>();
    addMasterBranchToFileBlobMap();
  }

  public void initializeRepo() throws IOException {

    // Since we already have a REPO key from initialization
    // we can now transition its state to
    // INITIALIZED via transitionState

    REPO_STATE_MACHINE.transitionState("init", "REPO");

    // Create hidden folders to store files in
    createFoldersForInit();

    // We add in a new entry to have a parent commit, which is our
    // initial commit. Then saves that to disk. Skip transition state here
    // because there are no files to stage and this is the parent commit.
    Commit initialCommit = makeInitialCommit();
    Head.setGlobalHead("master", initialCommit);
    Head.setBranchHead("master", initialCommit);

    System.out.println("Successfully initialized repository");
  }

  public void add(String[] files) throws IOException {

    // Prevent duplicates of files where blob SHA1s are the same. That would
    // indicate the same file with no content changes
    for (String file : files) {
      Map<String, Blob> currBranchFileBlobMap = branchesFileBlobMap.get(CURRENT_BRANCH);
      if (currBranchFileBlobMap != null && currBranchFileBlobMap.containsKey(file)) {
        // check if file with same content already exists in staging area. SHA1 should
        // be equal if so
        String blobSHA1OfFile = currBranchFileBlobMap.get(file).getBlobSHA1();
        File stagedFilePath = FileUtils.createFileInCurrentDirectory(STAGED_DIRECTORY, blobSHA1OfFile);
        if (stagedFilePath.exists()) {
          continue;
        }
      }
      GitCopyStateMachine currBranchStateMachine = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH);
      currBranchStateMachine.updateFileAndStateToMachine(file, GitCopyStates.UNSTAGED, false);

      // transition state to staged
      currBranchStateMachine.transitionState("add", file);

      // Convert the file into a blob with its contents and a SHA1
      Blob blob = new Blob(file);
      currBranchFileBlobMap.put(file, blob);

      // Stages file via save files to disk
      stageFile(file, blob);
    }
  }

  public void remove(String[] files) throws IOException {
    GitCopyStateMachine currBranchStateMachine = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH);
    Map<String, Blob> currBranchFileBlobMap = branchesFileBlobMap.get(CURRENT_BRANCH);
    for (String file : files) {
      if (currBranchStateMachine.fileInStateMachine(file)) {
        GitCopyStates fileState = currBranchStateMachine.getCurrentStateOfFile(file);
        if (fileState == GitCopyStates.STAGED) {

          // to do: refactor later to break into better encapsulated functions

          String blobSHA1 = currBranchFileBlobMap.get(file).getBlobSHA1();
          currBranchFileBlobMap.remove(file);
          File blobDirectoryFile = new File(BLOB_DIRECTORY + File.separator + blobSHA1);
          File blobStagedFile = new File(STAGED_DIRECTORY + File.separator + blobSHA1);
          FileUtils.deleteFile(blobDirectoryFile);
          FileUtils.deleteFile(blobStagedFile);
          currBranchStateMachine.transitionState("rm", file);

        } else {
          System.out.println("Can't remove this file. Must be staged.");
        }
      } else {
        System.out.println("This file does not exist in the repository.");
      }
    }
  }

  public void commit(String message) throws IOException {
    Map<String, String> snapMap = new HashMap<>();
    GitCopyStateMachine currBranchStateMachine = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH);
    Map<String, GitCopyStates> files = currBranchStateMachine.getFiles();
    Map<String, Blob> currBranchFileBlobMap = branchesFileBlobMap.get(CURRENT_BRANCH);

    for (Map.Entry<String, GitCopyStates> entry : files.entrySet()) {
      String file = entry.getKey();
      GitCopyStates state = entry.getValue();
      if (state == GitCopyStates.STAGED) {
        String blobSHA1 = currBranchFileBlobMap.get(file).getBlobSHA1();
        snapMap.put(file, blobSHA1);
        currBranchStateMachine.transitionState("commit", file);
        File blobStagedFile = new File(STAGED_DIRECTORY + File.separator + blobSHA1);
        FileUtils.deleteFile(blobStagedFile);
      }
    }
    String lastCommitSHA1 = Head.getGlobalHeadCommitSHA1();
    Commit newCommit = new Commit(message, snapMap, lastCommitSHA1);
    newCommit.saveCommit();

    Branch currentBranch = FileUtils.loadObject(Branch.class, "HEAD", GITCOPY_DIRECTORY);
    String currBranchName = currentBranch.getName();
    Head.setGlobalHead(currBranchName, newCommit);
    Head.setBranchHead(currBranchName, newCommit);
  }

  public void branch(String branchName) throws IOException {
    // Grab last global head commit and make it so the branch created
    // has that commit as its first commit pointer
    Commit lastCommit = Head.getGlobalHeadCommit();
    Head.setBranchHead(branchName, lastCommit);
    addBranchToStateMachine(branchName);
    addBranchToFileBlobMap(branchName);
  }

  /** Checks out to another branch. */
  public void checkoutBranch(String branchName) throws IOException {
    CURRENT_BRANCH = branchName;
    Commit branchHeadCommit = Head.getBranchHeadCommit(branchName);
    Head.setGlobalHead(branchName, branchHeadCommit);
  }

  /** Checks out to a commit. */
  public void checkoutCommit(String commitHash) throws IOException {
    // Get the latest head commit pointer, then recursively find the commit hash
    // and its corresponding commit instance.
    Commit lastCommit = Head.getGlobalHeadCommit();
    Commit foundCommit = findCommit(commitHash, lastCommit);
    if (foundCommit == null) {
      System.out.println("Couldn't find the commit hash");
      return;
    }
    // After finding the commit instance, iterate through the snapshot
    // and restore all files from that commit.
    Map<String, String> commitSnapShot = foundCommit.getSnapshot();
    for (Map.Entry<String, String> snapshotEntry : commitSnapShot.entrySet()) {
      String fileName = snapshotEntry.getKey();
      String blobSHA1 = snapshotEntry.getValue();
      Blob loadedBlob = FileUtils.loadObject(Blob.class, blobSHA1, BLOB_DIRECTORY);
      File file = new File(fileName);
      FileUtils.writeContentsToFile(file, loadedBlob.getFileContent());
    }

    // to do: figure out repointing the global head to be at the commit we are now
    // at and how we deal with detached states.
  }

  public void merge(String[] branches) throws IOException {

    // ths function is incomplete
    ArrayList<Commit> branchesCommits = new ArrayList<>();
    for (String branch : branches) {
      branchesCommits.add(Head.getBranchHeadCommit(branch));
    }
    findLatestCommonAncestor(branchesCommits);

  }

  /**
   * Finds the latest common ancestor commit to enable merging.
   * 
   * @param commits - An array list containing all the commits you intend to find
   *                the LCA for.
   * @return - *to do*
   */
  private ArrayList<Commit> findLatestCommonAncestor(ArrayList<Commit> commits) {
    if (commits.size() <= 1) {
      return commits;
    }
    ArrayList<Commit> appender = new ArrayList<>();
    ArrayList<Commit> res = new ArrayList<>();

    for (int index = 0; index < commits.size(); index++) {
      int pairCount = 0;
      int mover = index;
      while (pairCount != 2 && mover < commits.size()) {
        appender.add(commits.get(mover));
        pairCount++;
        mover++;
      }
      if (appender.size() > 1) {
        Set<String> visited = new HashSet<>();
        Commit firstCommit = appender.get(0);
        Commit secondCommit = appender.get(1);
        Commit commonAncestorOfPair = findLatestCommonAncestorOfPair(firstCommit, secondCommit, visited);
        res.add(commonAncestorOfPair);
        System.out.println(res.get(0).getSHA1());
      }
      appender = new ArrayList<>();
    }
    return findLatestCommonAncestor(res);
  }

  private Commit findLatestCommonAncestorOfPair(Commit firstCommit, Commit secondCommit, Set<String> visited) {
    if (firstCommit.getSHA1().equals(COMMIT_INIT_SHA1) || secondCommit.getSHA1().equals(COMMIT_INIT_SHA1)
        || visited.contains(firstCommit.getSHA1()) || visited.contains(secondCommit.getSHA1())) {
      return null;
    }
    if (firstCommit.equals(secondCommit)) {
      return firstCommit;
    }
    visited.add(firstCommit.getSHA1());
    visited.add(secondCommit.getSHA1());

    for (String firstCommitParentSHA1 : firstCommit.getParents()) {
      Commit commit = FileUtils.loadObject(Commit.class, firstCommitParentSHA1, COMMIT_DIRECTORY);
      Commit result = findLatestCommonAncestorOfPair(commit, secondCommit, visited);
      if (result != null) {
        return result;
      }
    }

    for (String secondCommitParentSHA1 : secondCommit.getParents()) {
      Commit commit = FileUtils.loadObject(Commit.class, secondCommitParentSHA1, COMMIT_DIRECTORY);
      Commit result = findLatestCommonAncestorOfPair(firstCommit, commit, visited);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /** Recursive function to find the commit hash in commit parents. */
  private Commit findCommit(String commitHash, Commit lastCommit) {
    if (lastCommit.getSHA1().equals(COMMIT_INIT_SHA1)) {
      return null;
    }
    if (lastCommit.getSHA1().equals(commitHash)) {
      return lastCommit;
    }
    for (String commitParentSHA1 : lastCommit.getParents()) {
      Commit commitFromParentSHA1 = FileUtils.loadObject(Commit.class, commitParentSHA1, COMMIT_DIRECTORY);
      Commit result = findCommit(commitHash, commitFromParentSHA1);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * Inputs a new branch and a copy of the current branches state machine into
   * BRANCH_STATE_MACHINES.
   * 
   * @param branchName - the new branch name we created, which serves as the key
   */
  private void addBranchToStateMachine(String branchName) {
    BRANCH_STATE_MACHINES.put(branchName, BRANCH_STATE_MACHINES.get(CURRENT_BRANCH));
  }

  /**
   * Adds a master key into the BRANCH_STATE_MACHINES and instantiates
   * a new GitCopyStateMachine for the master branch.
   */
  private void addMasterBranchToStateMachine() {
    BRANCH_STATE_MACHINES.put("master", new GitCopyStateMachine());
  }

  /** Adds the master branch into the branchesFileBlobMap. */
  private void addMasterBranchToFileBlobMap() {
    branchesFileBlobMap.put("master", new HashMap<>());
  }

  /**
   * Adds the newest branch made to the branchFileBlobMap. Inputs the
   * branchName as the new key, as well as a copy of the HashMap
   * from the current branch into the new branchName key.
   */
  private void addBranchToFileBlobMap(String branchName) {
    branchesFileBlobMap.put(branchName, branchesFileBlobMap.get(CURRENT_BRANCH));
  }

  /** Creates hidden folders in the .gitcopy directory */
  private void createFoldersForInit() {
    String currentDirectory = System.getProperty("user.dir");
    File gitCopyFolder = new File(currentDirectory, ".gitcopy");

    gitCopyFolder.mkdir();
    String gitCopyDirectory = gitCopyFolder.getAbsolutePath();

    File[] folders = {
        new File(gitCopyDirectory, ".staging"),
        new File(gitCopyDirectory, ".log"),
        new File(gitCopyDirectory, ".commits"),
        new File(gitCopyDirectory, ".blobs"),
        new File(gitCopyDirectory, ".branches")
    };

    for (File folder : folders) {
      if (!folder.exists()) {
        folder.mkdir();
      } else {
        System.err.println("Failed to create folder " + folder.getName());
      }
    }
  }

  private Commit makeInitialCommit() throws IOException {
    Commit initialCommit = new Commit("Initialization commit", COMMIT_INIT_SHA1);
    initialCommit.saveCommit();
    return initialCommit;
  }

  private void stageFile(String filename, Blob blob) throws IOException {

    // Save the blob to disk in .blobs and .staged
    FileUtils.saveObjectToFileDisk(blob.getBlobSHA1(), BLOB_DIRECTORY, blob);
    FileUtils.saveObjectToFileDisk(blob.getBlobSHA1(), STAGED_DIRECTORY, blob);
  }

}
