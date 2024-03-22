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
          handleInit();
          break;
        case "add":
          handleAdd(args);
          break;
        case "rm":
          handleRemove(args);
          break;
        case "commit":
          handleCommit(args);
          break;
        case "branch":
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
      case "branch":
        valid = true;
        break;
      default:
        valid = false;
    }
    return valid;
  }

  private static void handleInit() throws IOException {
    if (FileUtils.validateGitCopyExists()) {
      System.out.println("A repository already is initialized in this directory");
    } else {
      newRepo = new Repo();
      newRepo.initializeRepo();
      saveRepoToDisk();
    }
  }

  private static void handleAdd(String[] args) throws IOException {
    if (!FileUtils.validateGitCopyExists()) {
      System.out.println("A repository does not exist here, so you cannot add anything to stage.");
    } else {
      loadRepoFromDisk();
      // Take the second argument. Will need to handle other strings thereafter.
      String[] files = Arrays.copyOfRange(args, 1, args.length);
      newRepo.add(files);
      saveRepoToDisk();
    }
  }

  private static void handleRemove(String[] args) throws IOException {
    if (!FileUtils.validateGitCopyExists()) {
      System.out.println("A repository doesn't exist, so we cannot remove anything.");
    } else {
      loadRepoFromDisk();
      // Take the second argument. Will need to handle other strings thereafter.
      String[] files = Arrays.copyOfRange(args, 1, args.length);
      newRepo.remove(files);

      saveRepoToDisk();
    }
  }

  private static void handleCommit(String[] args) throws IOException {
    if (!FileUtils.validateGitCopyExists()) {
      System.out.println("A repository doesn't exist. Cannot commit anything.");
    } else {
      loadRepoFromDisk();
      String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
      newRepo.commit(message);
      saveRepoToDisk();
    }
  }

  private static void saveRepoToDisk() throws IOException {
    FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, REPO_DIRECTORY, newRepo);
  }

  private static void loadRepoFromDisk() throws IOException {
    newRepo = FileUtils.loadObject(Repo.class, Repo.DEFAULT_SHA1, REPO_DIRECTORY);

  }

}