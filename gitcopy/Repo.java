package gitcopy;

import java.io.File;

public class Repo {

  public void initializeRepo() {

    // create folders
    createFoldersForInit();

    // initialize statemachine
    // GitCopyStateMachine STATE_MACHINE = new GitCopyStateMachine();
    // save initial commit
    return;
  }

  public void createFoldersForInit() {
    String currentDirectory = System.getProperty("user.dir");
    File gitCopyFolder = new File(currentDirectory, ".gitcopy");

    gitCopyFolder.mkdir();
    String gitCopyDirectory = gitCopyFolder.getAbsolutePath();

    File[] folders = {
        new File(gitCopyDirectory, ".staging"),
        new File(gitCopyDirectory, ".log"),
        new File(gitCopyDirectory, ".commits"),
        new File(gitCopyDirectory, ".blobs")
    };

    for (File folder : folders) {
      if (!folder.exists()) {
        folder.mkdir();
      } else {
        System.err.println("Failed to create folder " + folder.getName());
      }
    }
  }
}
