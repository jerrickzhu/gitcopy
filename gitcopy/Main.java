package gitcopy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

  public static Repo newRepo;
  public final static String REPO_DIRECTORY = FileUtils.findGitCopyRootDirectory().getAbsolutePath() + File.separator
      + ".gitcopy";

  public static void main(String[] args) throws IOException {
    boolean isValid = validateArgs(args);
    if (!isValid) {
      System.out.println("You've entered an incorrect command. Please try again.");
      System.exit(0);
    }
    String command = args[0];
    if (command.equals("init")) {
      handleInit();
    } else if (!FileUtils.validateGitCopyExists()) {
      System.out.println("A repository does not exist here.");
      return;
    } else {
      loadRepoFromDisk();
      switch (command) {
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
          handleBranch(args);
          break;
        case "checkout":
          determineCheckout(args);
          break;
        case "merge":
          handleMerge(args);
          break;
        case "log":
          handleLog(args);
          break;
      }
    }
    saveRepoToDisk();
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
      case "checkout":
      case "merge":
      case "log":
        valid = true;
        break;
      default:
        valid = false;
    }
    return valid;
  }

  private static void handleInit() throws IOException {
    newRepo = new Repo();
    newRepo.initializeRepo();
  }

  private static void handleAdd(String[] args) throws IOException {
    // Take the second argument. Will need to handle other strings thereafter.
    String[] files = Arrays.copyOfRange(args, 1, args.length);
    newRepo.add(files);
  }

  private static void handleRemove(String[] args) throws IOException {
    // Take the second argument. Will need to handle other strings thereafter, like
    // *.
    String[] files = Arrays.copyOfRange(args, 1, args.length);
    newRepo.remove(files);

  }

  private static void handleCommit(String[] args) throws IOException {
    String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
    newRepo.commit(message);
  }

  private static void handleBranch(String[] args) throws IOException {
    newRepo.branch(args[1]);
  }

  private static void handleCheckoutBranch(String[] args) throws IOException {
    newRepo.checkoutBranch(args[1]);
  }

  private static void handleCheckoutCommit(String[] args) throws IOException {
    newRepo.checkoutCommit(args[1]);
  }

  private static void determineCheckout(String[] args) throws IOException {
    String commitHashOrBranch = args[1];
    if (isSHA1(commitHashOrBranch)) {
      handleCheckoutCommit(args);
    } else {
      handleCheckoutBranch(args);
    }
  }

  private static void handleMerge(String[] args) throws IOException {
    String[] branches = Arrays.copyOfRange(args, 1, args.length);
    newRepo.merge(branches);
  }

  private static void handleLog(String[] args) throws IOException {
    return;
  }

  /** Checks if the input given is a SHA1 */
  private static boolean isSHA1(String input) {
    String sha1Regex = "[0-9a-fA-F]{40}";
    Pattern pattern = Pattern.compile(sha1Regex);
    Matcher matcher = pattern.matcher(input);
    return matcher.matches();
  }

  private static void saveRepoToDisk() throws IOException {
    FileUtils.saveObjectToFileDisk(Repo.DEFAULT_SHA1, REPO_DIRECTORY, newRepo);
  }

  private static void loadRepoFromDisk() throws IOException {
    newRepo = FileUtils.loadObject(Repo.class, Repo.DEFAULT_SHA1, REPO_DIRECTORY);

  }

}