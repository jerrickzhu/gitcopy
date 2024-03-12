package gitcopy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main {

  public static Repo newRepo;

  public static void main(String[] args) throws IOException {
    boolean isValid = validateArgs(args);
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
            System.out.println("if added, problem here");
            newRepo = new Repo();
            newRepo.initializeRepo();
            try {
              // Saves the repository instance to disk.
              String repoDirectory = System.getProperty("user.dir") + File.separator + ".gitcopy";
              FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, repoDirectory, newRepo);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          break;
        case "add":
          if (!validateGitCopyExists()) {
            System.out.println("A repository does not exist here, so you cannot add anything to stage.");
          } else {
            // Reloads the repo instance.
            try {
              String repoDirectory = System.getProperty("user.dir") + File.separator + ".gitcopy";
              System.out.println(repoDirectory);
              newRepo = FileUtils.loadObject(Repo.class, Repo.DEFAULT_SHA1, repoDirectory);
              // Take the second argument. Will need to handle other strings thereafter.
              String[] files = Arrays.copyOfRange(args, 1, args.length);
              newRepo.add(files);
              FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, repoDirectory, newRepo);

            } catch (IOException e) {
              e.printStackTrace();
            }

          }
          break;
        case "log":

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

  /**
   * Function that validates if the arguments (commands entered) are valid. Valid
   * commands include: init, add, commit
   * 
   * @param args
   * @return boolean
   */
  private static boolean validateArgs(String[] args) {
    boolean valid;
    switch (args[0]) {
      case "init":
      case "add":
      case "commit":
        valid = true;
        break;
      default:
        valid = false;
    }
    return valid;
  }

}