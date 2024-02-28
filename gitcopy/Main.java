package gitcopy;

public class Main {
  public static void main(String[] args) {
    boolean isValid = Utils.validateArgs(args);
    if (!isValid) {
      System.out.println("You've entered an incorrect command. Please try again.");
      System.exit(0);
    } else {
      String command = args[0];
      switch (command) {
        case "init":
      }
    }
  }

}