package gitcopy;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
  public static String sha1(List<Object> vals) {
    return sha1(vals.toArray(new Object[vals.size()]));
  }

  public static void writeSerializedObjectToFile(File file, Serializable object) {
    writeContentsToFile(file, serialize(object));
  }

  public static byte[] serialize(Serializable object) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
      objectStream.writeObject(object);
      objectStream.close();
      return outputStream.toByteArray();
    } catch (IOException exception) {
      throw new Error("Error serializiing");
    }
  }

  public static <T extends Serializable> T deserialize(File file,
      Class<T> expectedClass) {
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
      T result = expectedClass.cast(in.readObject());
      in.close();
      return result;
    } catch (IOException | ClassCastException
        | ClassNotFoundException excp) {
      throw new IllegalArgumentException(excp.getMessage());
    }
  }

  public static void writeContentsToFile(File file, Object... content) {
    try {
      validateFile(file);
      BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(file.toPath()));
      writeContentsToStream(outputStream, content);
      outputStream.close();
    } catch (IOException | ClassCastException exception) {
      throw new IllegalArgumentException(
          "There was an error in writing the contents to file: " + exception.getMessage());
    }
  }

  private static void validateFile(File file) {
    if (file.isDirectory()) {
      throw new IllegalArgumentException("Ensure this is a file and not a directory");
    }
  }

  private static void writeContentsToStream(BufferedOutputStream outputStream, Object... contents) throws IOException {
    for (Object content : contents) {
      byte[] bytes = (content instanceof byte[]) ? (byte[]) content
          : content.toString().getBytes(StandardCharsets.UTF_8);
      outputStream.write(bytes);
    }
  }
}
