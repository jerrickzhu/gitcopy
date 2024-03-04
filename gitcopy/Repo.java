package gitcopy;

import java.io.File;

import gitcopy.state_machine.GitCopyStateMachine;

public class Repo {

  public void initializeRepo() {

    GitCopyStateMachine STATE_MACHINE = new GitCopyStateMachine();

    // create folders
    // createFoldersForInit();

    // initialize statemachine

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
        new File(gitCopyDirectory, ".blobs"),
        new File(gitCopyDirectory, ".states")
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
