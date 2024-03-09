package gitcopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

public class Commit implements Serializable {

  private String commitSHA1;
  private LocalDateTime time;
  private String commitMessage;
  private ArrayList<String> commitParents;
  private Map<String, String> snapshot;
  private final String COMMIT_DIRECTORY = System.getProperty("user.dir") + File.separator + ".gitcopy" + File.separator
      + ".commits";

  /** Constructors */

  /** This constructor is only for initializations. */
  public Commit(String message, Map<String, String> snapMap) {
    this.commitMessage = message;
    this.snapshot = snapMap;
    this.commitSHA1 = Utils.sha1(message);
    this.commitParents = new ArrayList<>();
    this.commitParents.add(Repo.DEFAULT_SHA1);
    this.time = LocalDateTime.now();
  }

  /** This constructor is for anything EXCEPT for initializations. */
  public Commit() {

  }

  public String getSHA1() {
    return this.commitSHA1;
  }

  /**
   * SAVE METHODS: Encapsulating save methods to their distinct behaviors below.
   */

  public void saveInitialCommit() {
    try {
      // Creates the first commit for initialization using the init constructor
      Commit commit = new Commit("Initial Commit for initialization", new HashMap<>());

      // Writes initial commit to disk in commits folder
      Utils.saveObjectToFileDisk(commit.getSHA1(), COMMIT_DIRECTORY, commit);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
