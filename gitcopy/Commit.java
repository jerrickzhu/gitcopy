package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.time.LocalDateTime;

public class Commit implements Serializable {

  private String commitSHA1;
  private String time;
  private String commitMessage;
  private ArrayList<String> commitParents = new ArrayList<>();
  private Map<String, String> snapshot;
  private final String COMMIT_DIRECTORY = System.getProperty("user.dir") + File.separator + ".gitcopy" + File.separator
      + ".commits";

  /** Constructors */

  /**
   * This constructor is only for initializations.
   * 
   */
  public Commit(String message, Map<String, String> snapMap) {
    this.commitMessage = message;
    this.snapshot = snapMap;
    this.time = LocalDateTime.now().toString();
    this.commitSHA1 = FileUtils.sha1(message);
    this.commitParents.add(Repo.DEFAULT_SHA1);

  }

  /**
   * This constructor is for anything EXCEPT for initializations.
   * 
   */
  public Commit(String message, Map<String, String> snapMap, String parent) {
    this.commitMessage = message;
    this.snapshot = snapMap;
    this.time = LocalDateTime.now().toString();
    this.commitSHA1 = FileUtils.sha1(snapMap + message);
    this.commitParents.add(parent);

  }

  /** Return sha1 of the commit instance. */
  public String getSHA1() {
    return this.commitSHA1;
  }

  /**
   * SAVE METHODS: Encapsulating save methods to their distinct behaviors below.
   * 
   *
   */

  public void saveCommit() {
    try {
      FileUtils.saveObjectToFileDisk(this.getSHA1(), COMMIT_DIRECTORY, this);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
