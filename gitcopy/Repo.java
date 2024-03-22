package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Repo implements Serializable {

  private GitCopyStateMachine STATE_MACHINE;
  private final String MAIN_DIRECTORY = FileUtils.findGitCopyRootDirectory().getAbsolutePath();
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";
  private Map<String, Blob> fileBlobMap;
  private final String BLOB_DIRECTORY = MAIN_DIRECTORY + File.separator + ".gitcopy" + File.separator
      + ".blobs";
  private final String STAGED_DIRECTORY = MAIN_DIRECTORY + File.separator + ".gitcopy" + File.separator
      + ".staging";

  public Repo() {
    // Initialization of a GitCopyStateMachine also creates the first entry
    // in its hashmap attribute with key "REPO" with a val of UninitializedState
    STATE_MACHINE = new GitCopyStateMachine();
    fileBlobMap = new HashMap<>();
  }

  public void initializeRepo() throws IOException {

    // Since we already have a REPO key, we can now transition its state to
    // InitializedState via transitionState

    STATE_MACHINE.transitionState("init", "REPO");

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
      if (fileBlobMap != null && fileBlobMap.containsKey(file)) {
        // check if file with same content already exists in staging area. SHA1 should
        // be equal if so
        String blobSHA1OfFile = fileBlobMap.get(file).getBlobSHA1();
        File stagedFilePath = FileUtils.createFileInCurrentDirectory(STAGED_DIRECTORY, blobSHA1OfFile);
        if (stagedFilePath.exists()) {
          continue;
        }
      }
      STATE_MACHINE.updateFileAndStateToMachine(file, GitCopyStates.UNSTAGED, false);

      // transition state to staged
      STATE_MACHINE.transitionState("add", file);

      // Convert the file into a blob with its contents and a SHA1
      Blob blob = new Blob(file);
      fileBlobMap.put(file, blob);

      stageFile(file, blob);
    }
  }

  public void remove(String[] files) throws IOException {
    for (String file : files) {
      if (STATE_MACHINE.fileInStateMachine(file)) {
        GitCopyStates fileState = STATE_MACHINE.getCurrentStateOfFile(file);
        if (fileState == GitCopyStates.STAGED) {

          // to do: refactor later to break into better encapsulated functions

          String blobSHA1 = fileBlobMap.get(file).getBlobSHA1();
          fileBlobMap.remove(file);
          File blobDirectoryFile = new File(BLOB_DIRECTORY + File.separator + blobSHA1);
          File blobStagedFile = new File(STAGED_DIRECTORY + File.separator + blobSHA1);
          FileUtils.deleteFile(blobDirectoryFile);
          FileUtils.deleteFile(blobStagedFile);
          STATE_MACHINE.transitionState("rm", file);

        } else {
          System.out.println("Can't remove this file. Must be staged.");
        }
      } else {
        System.out.println("This file does not exist in the repository.");
      }
    }
  }

  public void commit(String[] files) throws IOException {
    for (String file : files) {
      if (STATE_MACHINE.fileInStateMachine(file)) {
        GitCopyStates fileState = STATE_MACHINE.getCurrentStateOfFile(file);
        if (fileState == GitCopyStates.STAGED) {
          // to do: continue with commit
          // to do: remove the sha1 from the .staging hidden folder
        } else {
          System.out
              .println("The file " + file + "is not in a staged state. Please stage it to continue with committing.");
        }
      } else {
        System.out.println("The file doesn't exist in this repository.");
      }
    }
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
    initialCommit.saveInitialCommit();
    return initialCommit;
  }

  private void stageFile(String filename, Blob blob) throws IOException {

    // Save the blob to disk in .blobs and .staged
    FileUtils.saveObjectToFileDisk(blob.getBlobSHA1(), BLOB_DIRECTORY, blob);
    FileUtils.saveObjectToFileDisk(blob.getBlobSHA1(), STAGED_DIRECTORY, blob);
  }

}
