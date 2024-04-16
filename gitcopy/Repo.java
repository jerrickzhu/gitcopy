package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Repo implements Serializable {

  private RepoStateMachine REPO_STATE_MACHINE;
  private Map<String, GitCopyStateMachine> BRANCH_STATE_MACHINES;
  private String CURRENT_BRANCH;
  private static final String MAIN_DIRECTORY = FileUtils.findGitCopyRootDirectory().getAbsolutePath();
  private static final String GITCOPY_DIRECTORY = MAIN_DIRECTORY + File.separator + ".gitcopy";
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";
  static final String COMMIT_INIT_SHA1 = "1000000000000000000000000000000000000001";
  // First key are branches, values are hashmaps of the file name (key) and blobs
  // (values)
  private Map<String, Map<String, Blob>> BRANCHES_FILE_BLOP_MAP;
  public static final String BLOB_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".blobs";
  public static final String STAGED_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".staging";
  public static final String COMMIT_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".commits";
  public static final String BRANCH_DIRECOTRY = GITCOPY_DIRECTORY + File.separator + ".branches";

  public Repo() {
    REPO_STATE_MACHINE = new RepoStateMachine();
    BRANCH_STATE_MACHINES = new HashMap<>();
    CURRENT_BRANCH = "master";
    addMasterBranchToStateMachine();
    BRANCHES_FILE_BLOP_MAP = new HashMap<>();
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
    if (!verifyRepoState()) {
      return;
    }

    // Prevent duplicates of files where blob SHA1s are the same. That would
    // indicate the same file with no content changes
    for (String file : files) {
      Map<String, Blob> currBranchFileBlobMap = BRANCHES_FILE_BLOP_MAP.get(CURRENT_BRANCH);
      if (currBranchFileBlobMap != null && currBranchFileBlobMap.containsKey(file)) {
        // check if file with same content already exists in staging area. SHA1 should
        // be equal if so
        String blobSHA1OfFile = currBranchFileBlobMap.get(file).getBlobSHA1();
        File stagedFilePath = FileUtils.createFileInCurrentDirectory(STAGED_DIRECTORY, blobSHA1OfFile);
        if (stagedFilePath.exists()) {
          continue;
        }
      }
      // Don't need to check state since unstaged files don't exist yet
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
    Map<String, Blob> currBranchFileBlobMap = BRANCHES_FILE_BLOP_MAP.get(CURRENT_BRANCH);
    for (String file : files) {
      if (currBranchStateMachine.fileInStateMachine(file)) {
        Blob blob = currBranchFileBlobMap.get(file);
        String blobSHA1 = blob.getBlobSHA1();
        currBranchFileBlobMap.remove(file);
        File blobDirectoryFile = new File(BLOB_DIRECTORY + File.separator + blobSHA1);
        File blobStagedFile = new File(STAGED_DIRECTORY + File.separator + blobSHA1);
        File fileCWD = new File(System.getProperty("user.dir") + File.separator + file);
        deleteFiles(blobDirectoryFile, blobStagedFile, fileCWD);
        // Transitions state then removes from state machine
        currBranchStateMachine.transitionState("rm", file);

      } else {
        System.out.println("This file does not exist.");
      }
    }
  }

  /** Helper function for remove method. Deletes files */
  private void deleteFiles(File... files) {
    for (File file : files) {
      FileUtils.deleteFile(file);
    }
  }

  public void commit(String message) throws IOException {
    Map<String, String> snapMap = new HashMap<>();
    GitCopyStateMachine currBranchStateMachine = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH);
    Map<String, GitCopyStates> files = currBranchStateMachine.getFiles();
    Map<String, Blob> currBranchFileBlobMap = BRANCHES_FILE_BLOP_MAP.get(CURRENT_BRANCH);

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
    if (lastCommit.getSHA1().equals(COMMIT_INIT_SHA1)) {
      System.out.println(
          "Cannot create a new branch so long as the head points to initial commit. Please make a new commit first.");
      return;
    }
    Head.setBranchHead(branchName, lastCommit);
    addBranchToStateMachine(branchName);
    addBranchToFileBlobMap(branchName);
  }

  /** Checks out to another branch. */
  public void checkoutBranch(String branchName) throws IOException {
    System.out.println("prev branch: " + CURRENT_BRANCH);
    CURRENT_BRANCH = branchName;
    Commit branchHeadCommit = Head.getBranchHeadCommit(branchName);
    Head.setGlobalHead(branchName, branchHeadCommit);
    System.out.println("You're now on the " + branchName + " branch.");

    // Restore files in current working directory
    restoreCommit(branchHeadCommit.getSnapshot());

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
    restoreCommit(commitSnapShot);

    // to do: figure out repointing the global head to be at the commit we are now
    // at and how we deal with detached states.
  }

  public void merge(String[] branches) throws IOException {

    // ths function is incomplete

    Commit LCA = getLCACommit(branches);

    // Iterate through branchCommits and compare LCA to each.
    Map<String, String> LCASnapShot = LCA.getSnapshot();
    Map<String, String> currBranchSnapShot = Head.getBranchHeadCommit(CURRENT_BRANCH).getSnapshot();
    // The merge snapshot to commit with. Goes through each condition to add to this
    // snapshot.
    Map<String, String> mergeSnapShotMap = new HashMap<>();
    GitCopyStateMachine currBranchStateMachine = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH);
    Map<String, Blob> currBranchFileBlobMap = BRANCHES_FILE_BLOP_MAP.get(CURRENT_BRANCH);
    // boolean value to use to check if we need to look for files not in LCA but in
    // curr branch (fileNotInLCAButInBranch). Used to reduce redundant code calls in
    // case of many branches to iterate through.
    boolean seenCurrBranch = false;

    // If the LCA commit SHA1 is the same as the global head pointer, then we don't
    // need to continue on with the merge
    if (LCA.getSHA1().equals(Head.getGlobalHeadCommitSHA1())) {
      return;
    }
    for (String branch : branches) {
      Commit branchCommit = Head.getBranchHeadCommit(branch);
      Map<String, String> snapshot = branchCommit.getSnapshot();

      // to do: add in all possible conditions here

      // The given branch is modified, but the curr branch is not.
      Merge.oneBranchChangesOnly(LCASnapShot, snapshot, currBranchSnapShot, mergeSnapShotMap,
          currBranchStateMachine, currBranchFileBlobMap);

      // The curr branch is modified, but the given branch is not.
      Merge.oneBranchChangesOnly(LCASnapShot, currBranchSnapShot, snapshot, mergeSnapShotMap,
          currBranchStateMachine, currBranchFileBlobMap);

      // Both branches modified. Check if the changes are the same or if they are
      // different.
      Merge.isModdedGivenAndCurr(LCASnapShot, snapshot, currBranchSnapShot, mergeSnapShotMap, currBranchStateMachine,
          currBranchFileBlobMap);

      // Check to see if we've already checked fileNotInLCAButInBranch for the curr
      // branch. Reduce redundant calls during iterations.
      if (!seenCurrBranch) {
        // If curr branch has files that LCA doesn't
        Merge.fileNotInLCAButInBranch(LCASnapShot, currBranchSnapShot, mergeSnapShotMap, currBranchStateMachine,
            currBranchFileBlobMap);
        seenCurrBranch = true;
      }

      // If given branch has files that LCA doesn't
      Merge.fileNotInLCAButInBranch(LCASnapShot, snapshot, mergeSnapShotMap, currBranchStateMachine,
          currBranchFileBlobMap);
    }

    // Save commit + remove branch from state machine and blob map
    commitMerge(mergeSnapShotMap);
    for (String branch : branches) {
      BRANCH_STATE_MACHINES.remove(branch);
      BRANCHES_FILE_BLOP_MAP.remove(branch);
    }

  }

  private void commitMerge(Map<String, String> mergeMap) throws IOException {
    String lastCommitSHA1 = Head.getBranchHeadCommit(CURRENT_BRANCH).getSHA1();
    boolean allFileStatesStaged = false;
    for (Map.Entry<String, String> entry : mergeMap.entrySet()) {
      String fileName = entry.getKey();
      GitCopyStates currState = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH).getCurrentStateOfFile(fileName);
      if (currState == GitCopyStates.STAGED) {
        allFileStatesStaged = true;
      } else {
        allFileStatesStaged = false;
        System.out.println("Not all of the files in merge map are staged.");
        return;
      }
    }
    if (allFileStatesStaged) {
      Commit newCommit = new Commit("merge commit", mergeMap, lastCommitSHA1);
      newCommit.saveCommit();

      Branch currentBranch = FileUtils.loadObject(Branch.class, CURRENT_BRANCH, BRANCH_DIRECOTRY);
      String currBranchName = currentBranch.getName();
      Head.setGlobalHead(currBranchName, newCommit);
      Head.setBranchHead(currBranchName, newCommit);
    }
  }

  // Log commits for current branch. Used when no second argument is passed for
  // log. e.g., java gitcopy.Main log
  public void log() throws IOException {
    Commit currBranchCommit = Head.getBranchHeadCommit(CURRENT_BRANCH);
    traverseCommitHistory(currBranchCommit);

  }

  // Log commits for the specific branch.
  // Command in terminal example: java gitcopy.Main log [insert branch name]
  public void log(String branchName) throws IOException {
    Commit branchCommit = Head.getBranchHeadCommit(branchName);
    traverseCommitHistory(branchCommit);
  }

  /**
   * Function to travel commit history and print out relevant information for
   * logging.
   */
  private void traverseCommitHistory(Commit commit) {
    System.out.println();
    System.out.println("Commit hash: " + commit.getSHA1());
    System.out.println("Commit message: " + commit.getMessage());
    System.out.println("Commit Date: " + commit.getTime());

    ArrayList<String> parents = commit.getParents();

    for (String parentSHA1 : parents) {
      Commit parentCommit = FileUtils.loadObject(Commit.class, parentSHA1, COMMIT_DIRECTORY);
      if (!parentCommit.getSHA1().equals(COMMIT_INIT_SHA1)) {
        traverseCommitHistory(parentCommit);
      }
    }
    return;
  }

  private void restoreCommit(Map<String, String> commitSnapShot) throws IOException {
    for (Map.Entry<String, String> snapshotEntry : commitSnapShot.entrySet()) {
      Blob loadedBlob = null;
      String fileName = snapshotEntry.getKey();
      String blobSHA1 = snapshotEntry.getValue();
      if ((new File(BLOB_DIRECTORY, blobSHA1)).exists()) {
        loadedBlob = FileUtils.loadObject(Blob.class, blobSHA1, BLOB_DIRECTORY);
        FileUtils.writeContentsToFile(new File(fileName), loadedBlob.getFileContent());
      }

    }
  }

  /** Helper function to encapsulate getting LCA */
  private Commit getLCACommit(String[] branches) {
    // branchesCommits is an array list that holds all of the branches' commits we
    // need to merge
    ArrayList<Commit> branchesCommits = new ArrayList<>();
    for (String branch : branches) {
      branchesCommits.add(Head.getBranchHeadCommit(branch));
    }
    // Get common ancestor of all branches
    ArrayList<Commit> branchesCommonAncestor = Merge.findLatestCommonAncestor(branchesCommits);

    // Add the global head branch to branchesCommonAncestor to find the final LCA
    branchesCommonAncestor.add(Head.getGlobalHeadCommit());

    // Find the final LCA between master and the other LCA of the branches from
    // branchesCommonAncestor and return the LCA commit
    ArrayList<Commit> latestCommonAncestor = Merge.findLatestCommonAncestor(branchesCommonAncestor);
    return latestCommonAncestor.get(0);
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
    GitCopyStateMachine originalStateMachine = BRANCH_STATE_MACHINES.get(CURRENT_BRANCH);
    GitCopyStateMachine deepCopy = (GitCopyStateMachine) originalStateMachine.clone();
    BRANCH_STATE_MACHINES.put(branchName, deepCopy);
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
    BRANCHES_FILE_BLOP_MAP.put("master", new HashMap<>());
  }

  /**
   * Adds the newest branch made to the branchFileBlobMap. Inputs the
   * branchName as the new key, as well as a copy of the HashMap
   * from the current branch into the new branchName key.
   */
  private void addBranchToFileBlobMap(String branchName) {
    Map<String, Blob> originalFileBlopMap = BRANCHES_FILE_BLOP_MAP.get(CURRENT_BRANCH);
    Map<String, Blob> deepCopy = new HashMap<>();
    for (Map.Entry<String, Blob> entry : originalFileBlopMap.entrySet()) {
      String fileName = entry.getKey();
      Blob originalBlob = entry.getValue();
      Blob deepCopyBlob = originalBlob.clone();
      deepCopy.put(fileName, deepCopyBlob);
    }
    BRANCHES_FILE_BLOP_MAP.put(branchName, deepCopy);
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
        new File(gitCopyDirectory, ".branches"),
        new File(gitCopyDirectory, ".deleted_blobs")
    };

    for (File folder : folders) {
      if (!folder.exists()) {
        folder.mkdir();
      } else {
        System.err.println("Failed to create folder " + folder.getName());
      }
    }
  }

  private boolean verifyRepoState() {
    return REPO_STATE_MACHINE.getCurrentStateOfFile("REPO").equals(GitCopyStates.INITIALIZED);
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
