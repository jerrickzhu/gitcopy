package gitcopy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.io.File;

public class Blob implements Serializable, Cloneable {
  private String fileName;
  private byte[] fileContent;
  private String blobSHA1;
  private String time;

  public Blob(String filename) {
    this.fileName = filename;
    File file = FileUtils.createFileInCurrentDirectory(".", filename);
    this.fileContent = FileUtils.convertFileToBytes(file);
    this.time = LocalDateTime.now().toString();
    this.blobSHA1 = FileUtils.sha1(this.fileContent);
  }

  @Override
  public Blob clone() {
    try {
      Blob clone = (Blob) super.clone();
      clone.fileContent = Arrays.copyOf(this.fileContent, this.fileContent.length);
      return clone;
    } catch (CloneNotSupportedException excp) {
      throw new AssertionError();
    }
  }

  public String getBlobSHA1() {
    return this.blobSHA1;
  }

  public String getFileName() {
    return this.fileName;
  }

  public byte[] getFileContent() {
    return this.fileContent;
  }

}
