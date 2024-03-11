package gitcopy;

import java.io.Serializable;
import java.io.File;

public class Blob implements Serializable {
  private String fileName;
  private byte[] fileContent;
  private String blobSHA1;

  public Blob(String filename) {
    this.fileName = filename;
    File file = FileUtils.createFileInCurrentDirectory(".", filename);
    this.fileContent = FileUtils.convertFileToBytes(file);
    this.blobSHA1 = FileUtils.sha1(this.fileContent);
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
