package gitcopy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Main {

  public static Repo newRepo;
  public final static String REPO_DIRECTORY = FileUtils.findGitCopyRootDirectory().getAbsolutePath() + File.separator
      + ".gitcopy";

  public static void main(String[] args) throws IOException {
    boolean isValid = validateArgs(args);
    if (!isValid) {
      System.out.println("You've entered an incorrect command. Please try again.");
      System.exit(0);
    } else {
      String command = args[0];
      switch (command) {
        case "init":
          if (FileUtils.validateGitCopyExists()) {
            System.out.println("A repository already is initialized in this directory");
          } else {
            newRepo = new Repo();
            newRepo.initializeRepo();
            // Saves the repository instance to disk.
            FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, REPO_DIRECTORY, newRepo);
          }
          break;
        case "add":
          if (!FileUtils.validateGitCopyExists()) {
            System.out.println("A repository does not exist here, so you cannot add anything to stage.");
          } else {
            // Reloads the repo instance.
            newRepo = FileUtils.loadObject(Repo.class, Repo.DEFAULT_SHA1, REPO_DIRECTORY);
            // Take the second argument. Will need to handle other strings thereafter.
            String[] files = Arrays.copyOfRange(args, 1, args.length);
            newRepo.add(files);

            // Save repository instance ot disk
            FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, REPO_DIRECTORY, newRepo);
          }
          break;
        case "rm":
          if (!FileUtils.validateGitCopyExists()) {
            System.out.println("A repository doesn't exist, so we cannot remove anything.");
          } else {
            // Reloads the repo instance.
            newRepo = FileUtils.loadObject(Repo.class, Repo.DEFAULT_SHA1, REPO_DIRECTORY);
            // Take the second argument. Will need to handle other strings thereafter.
            String[] files = Arrays.copyOfRange(args, 1, args.length);
            newRepo.remove(files);

            // Save repository instance
            FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, REPO_DIRECTORY, newRepo);

          }
          break;
        case "commit":
          if (!FileUtils.validateGitCopyExists()) {
            System.out.println("A repository doesn't exist. Cannot commit anything.");
          } else {

            newRepo = FileUtils.loadObject(Repo.class, Repo.DEFAULT_SHA1, REPO_DIRECTORY);
            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            newRepo.commit(message);
            FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, REPO_DIRECTORY, newRepo);

          }
          break;
        case "log":

      }
    }
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
      case "rm":
        valid = true;
        break;
      default:
        valid = false;
    }
    return valid;
  }

}