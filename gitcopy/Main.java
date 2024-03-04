package gitcopy;

import java.io.File;
import java.io.IOException;

public class Main {

  public static Repo newRepo;

  public static void main(String[] args) {
    boolean isValid = Utils.validateArgs(args);
    if (!isValid) {
      System.out.println("You've entered an incorrect command. Please try again.");
      System.exit(0);
    } else {
      String command = args[0];
      switch (command) {
        case "init":
          if (validateGitCopyExists()) {
            System.out.println("A repository already is initialized in this directory");
          } else {
            newRepo = new Repo();
            newRepo.initializeRepo();
            try {
              RepoManager.saveRepo(newRepo, Repo.DEFAULT_SHA1);
            } catch (IOException e) {

            }

          }
          break;
        case "add":
          if (!validateGitCopyExists()) {
            System.out.println("A repository does not exist here!");
          } else {
            RepoManager.loadRepo();
          }
      }
    }
  }

  public static boolean validateGitCopyExists() {
    String currentDirectory = System.getProperty("user.dir");
    File gitCopyFolder = new File(currentDirectory, ".gitcopy");
    if (gitCopyFolder.exists()) {
      return true;
    }
    return false;
  }

}