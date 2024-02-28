package gitcopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class provides some functions considered as utilities for
 * streamlining the build of gitcopy.
 */
public class Utils {

  /**
   * Converts the file into an array of bytes.
   * 
   * @param file
   * @return byte[]
   */
  public static byte[] convertFileToBytes(File file) {
    if (file.isFile()) {
      try {
        Path pathOfFile = file.toPath();
        byte[] fileConvertedToBytes = Files.readAllBytes(pathOfFile);
        return fileConvertedToBytes;
      } catch (IOException exception) {
        throw new IllegalArgumentException(exception.getMessage());
      }
    } else {
      throw new IllegalArgumentException("This is not a file.");
    }
  }

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
