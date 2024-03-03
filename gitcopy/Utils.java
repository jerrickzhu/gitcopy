package gitcopy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;

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

  /**
   * Concatenates file path components into a file object.
   * 
   * @param directoryPath
   * @param filename
   * @return
   */
  public static File createFileInCurrentDirectory(
      String directoryPath, String... filename) {
    Path constructPath = Paths.get(directoryPath, filename);
    return constructPath.toFile();
  }

  /**
   * Calculates the SHA-1 hash of the concatenation of the provided objects
   * whether they are byte arrays or strings.
   * Returns the hash as a hexadecimal string
   * 
   * @param vals
   * @return
   */
  public static String sha1(Object... vals) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      for (Object val : vals) {
        if (val instanceof byte[]) {
          md.update((byte[]) val);
        } else if (val instanceof String) {
          md.update(((String) val).getBytes(StandardCharsets.UTF_8));
        } else {
          throw new IllegalArgumentException("improper type to sha1");
        }
      }
      Formatter result = new Formatter();
      for (byte b : md.digest()) {
        result.format("%02x", b);
      }
      return result.toString();
    } catch (NoSuchAlgorithmException excp) {
      throw new IllegalArgumentException("System does not support SHA-1");
    }
  }

  /**
   * Converts a List<Object> to an array and then calculates
   * the SHA-1 hash of the concatenation of the elements in the list.
   * Returns the hash as a hexadecimal string.
   */
  static String sha1(List<Object> vals) {
    return sha1(vals.toArray(new Object[vals.size()]));
  }
}
