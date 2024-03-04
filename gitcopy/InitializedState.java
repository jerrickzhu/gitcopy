package gitcopy;

import java.io.File;
import java.io.Serializable;

public class InitializedState extends GitCopyState implements Serializable {

  @Override
  public void processCommand() {
    createFoldersForInit();
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
