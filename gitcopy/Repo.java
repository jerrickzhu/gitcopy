package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Repo implements Serializable {

  public GitCopyStateMachine STATE_MACHINE;
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";

  public Repo() {
    // Initialization of a GitCopyStateMachine also creates the first entry
    // in its hashmap attribute with key "REPO" with a val of UninitializedState
    STATE_MACHINE = new GitCopyStateMachine();
  }

  public void initializeRepo() throws IOException {

    // Since we already have a REPO key, we can now transition its state to
    // InitializedState via transitionState
    STATE_MACHINE.transitionState("init", "REPO");

    // Create hidden folders to store files in
    createFoldersForInit();

    // We add in a new entry to have a parent commit, which is our
    // initial commit. Then saves that to disk.
    makeInitialCommit();
    STATE_MACHINE.addFileAndStateToMachine("INITIAL_COMMIT", GitCopyStates.COMMITTED);

    System.out.println("Successfully initialized repository");
  }

  public void add(String[] files) throws IOException {
    for (String file : files) {
      GitCopyStates fileState = STATE_MACHINE.getCurrentStateOfFile(file);
      if (STATE_MACHINE.fileInStateMachine(file)) {
        if (fileState == GitCopyStates.STAGED) {
          System.out.println(file + " is already staged!");
          continue;
        } else if (fileState == GitCopyStates.COMMITTED) {
          System.out.println(file + " is already commited!");
          continue;
        }
      } else {
        STATE_MACHINE.addFileAndStateToMachine(file, GitCopyStates.STAGED);
        stageFile(file);
      }
    }
  }

  public void commit(String[] files) throws IOException {
    for (String file : files) {
      GitCopyStates fileState = STATE_MACHINE.getCurrentStateOfFile(file);
      if (fileState == GitCopyStates.STAGED) {
        // to do: continue with commit
      } else {
        System.out
            .println("The file " + file + "is not in a staged state. Please stage it to continue with committing.");
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
    };

    for (File folder : folders) {
      if (!folder.exists()) {
        folder.mkdir();
      } else {
        System.err.println("Failed to create folder " + folder.getName());
      }
    }
  }

  private void makeInitialCommit() {
    Commit initialCommit = new Commit("Initialization commit", new HashMap<>());
    initialCommit.saveInitialCommit();
  }

  private void stageFile(String filename) throws IOException {

    // Convert the file into a blob with its contents and a SHA1
    Blob blob = new Blob(filename);

    // Save the blob to disk in .blobs and .staged
    String blobDirectory = System.getProperty("user.dir") + File.separator + ".gitcopy" + File.separator + ".blobs";
    String stagedDirectory = System.getProperty("user.dir") + File.separator + ".gitcopy" + File.separator + ".staging";
    Utils.saveObjectToFileDisk(blob.getBlobSHA1(), blobDirectory, blob);
    Utils.saveObjectToFileDisk(blob.getBlobSHA1(), stagedDirectory, blob);

  }
}
