package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Repo implements Serializable {

  private RepoStateMachine REPO_STATE_MACHINE;
  private Map<String, GitCopyStateMachine> BRANCH_STATE_MACHINES;
  private String CURRENT_BRANCH;
  private final String MAIN_DIRECTORY = FileUtils.findGitCopyRootDirectory().getAbsolutePath();
  private final String GITCOPY_DIRECTORY = MAIN_DIRECTORY + File.separator + ".gitcopy";
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";
  private Map<String, Map<String, Blob>> branchesFileBlobMap;
  private final String BLOB_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".blobs";
  private final String STAGED_DIRECTORY = GITCOPY_DIRECTORY + File.separator + ".staging";

  public Repo() {
    // Initialization of a GitCopyStateMachine also creates the first entry
    // in its hashmap attribute with key "REPO" with a val of UninitializedState
    REPO_STATE_MACHINE = new RepoStateMachine();
    BRANCH_STATE_MACHINES = new HashMap<>();
    BRANCH_STATE_MACHINES.put("master", new GitCopyStateMachine());
    branchesFileBlobMap = new HashMap<>();
    branchesFileBlobMap.put("master", new HashMap<>());
    CURRENT_BRANCH = "master";
  }

  public void initializeRepo() throws IOException {

    // Since we already have a REPO key, we can now transition its state to
    // InitializedState via transitionState

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
      if (file.equals("REPO")) {
        continue;
      }
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
    Commit lastCommit = Head.getGlobalHeadCommit();
    Head.setBranchHead(branchName, lastCommit);
  }

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

  private Commit makeInitialCommit() {
    Commit initialCommit = new Commit("Initialization commit", new HashMap<>());
    initialCommit.saveCommit();
    return initialCommit;
  }

  private void stageFile(String filename, Blob blob) throws IOException {

    // Save the blob to disk in .blobs and .staged
    FileUtils.saveObjectToFileDisk(blob.getBlobSHA1(), BLOB_DIRECTORY, blob);
    FileUtils.saveObjectToFileDisk(blob.getBlobSHA1(), STAGED_DIRECTORY, blob);
  }

}
