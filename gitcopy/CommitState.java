package gitcopy;

import java.io.Serializable;
import java.util.HashMap;

public class CommitState extends GitCopyState implements Serializable {

  private String typeOfCommit;

  public CommitState(String commit) {
    this.typeOfCommit = commit;
  }

  @Override
  public void processCommand() {
    if (this.typeOfCommit == "init") {
      makeInitialCommit();
    } else {
      // run other commits here based on the typeOfCommit
    }
  }

  private void makeInitialCommit() {
    Commit initialCommit = new Commit("Initialization commit", new HashMap<>());
    initialCommit.saveInitialCommit();
  }

}
