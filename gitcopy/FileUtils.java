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
public class FileUtils {

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
   * General abstracted method to save objects to a file using
   * other utility methods in this class. Starts by creating file blueprint
   * in that current directory, creates the file, then writes it to disk.
   * 
   * @param filename      - String type that takes in the file name you want to
   *                      give;
   *                      should be a SHA1
   * @param directoryPath - String type that is the directory path to create file
   * @param classInstance - Serializable type of the instance of the class you
   *                      pass in to serialize
   * 
   */
  public static void saveObjectToFileDisk(String filename, String directoryPath, Serializable classInstance)
      throws IOException {
    File file = createFileInCurrentDirectory(directoryPath, filename);
    file.createNewFile();
    writeSerializedObjectToFile(file, classInstance);
    System.out.println("Successfully wrote object to disk");
  }

  /**
   * General abstract method to load objects.
   * 
   * @param objectType    - Type Class<T> generic. This should be a class object.
   *                      E.g., if it's a Repo, this parameter should be
   *                      Repo.class
   * @param filename      - filename of the file from which the object will be
   *                      deserialized. It's a string that specifies the name of
   *                      the file containing the serialized object.
   * @param directoryPath - Path of the file.
   * @return - Casts the deserialized object to the type specified by objectType.
   *         The cast method is called on the objectType Class object, and it
   *         ensures that the deserialized object is of the correct type T.
   *         Finally, the deserialized and casted object is returned from the
   *         method.
   * @throws IllegalArgumentException
   */
  public static <T extends Serializable> T loadObject(Class<T> objectType, String filename, String directoryPath)
      throws IllegalArgumentException {
    File file = new File(directoryPath, filename);
    return objectType.cast(deserialize(file, objectType));
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

  public static boolean deleteFile(File file) {
    if (file.isDirectory()) {
      System.out.println("You can't delete a directory");
      return false;
    }
    return file.delete();
  }

  public static boolean isFileInCurrDirectory(File file) {
    return file.exists() && !file.isDirectory();
  }

  public static boolean isGitCopyDirectory(File directory) {
    File gitCopyFolder = new File(directory, ".gitcopy");
    return gitCopyFolder.exists();
  }

  public static File findGitCopyRootDirectory() {
    File currDirectory = new File(System.getProperty("user.dir"));
    while (currDirectory != null && !isGitCopyDirectory(currDirectory)) {
      File parentDirectory = currDirectory.getParentFile();
      // if the parent directory is null, we haven't found the root, so return
      // current working directory
      if (parentDirectory == null) {
        return new File(System.getProperty("user.dir"));
      }
      currDirectory = parentDirectory;
    }
    return currDirectory;
  }

  public static boolean validateGitCopyExists() {
    File gitCopy = findGitCopyRootDirectory();
    return isGitCopyDirectory(gitCopy);
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
          throw new IllegalArgumentException("Improper type to sha1");
        }
      }
      Formatter result = new Formatter();
      for (byte b : md.digest()) {
        result.format("%02x", b);
      }
      return result.toString();
    } catch (NoSuchAlgorithmException excp) {
      throw new IllegalArgumentException("System doesn't support SHA-1");
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
      throw new Error("Error serializing");
    }
  }

  /**
   * Deserializes files. Usually used on reload the program in commands like add
   * or commit.
   * 
   * @param File - File object to deserialize
   * @return Deserialized object
   */
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
