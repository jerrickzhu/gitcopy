package gitcopy;

public class Utils {

  /**
   * Function that validates if the arguments (commands entered) are valid. Valid
   * commands include: init, add, commit
   * 
   * @param args
   * @return boolean
   */
  public static boolean validateArgs(String[] args) {
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
