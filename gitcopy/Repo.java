package gitcopy;

import java.io.Serializable;

public class Repo implements Serializable {
  public GitCopyStateMachine STATE_MACHINE;
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";

  public Repo() {
    STATE_MACHINE = new GitCopyStateMachine();
  }

  public void initializeRepo() {

    STATE_MACHINE.transitionState("init", "REPO");
    STATE_MACHINE.processStateCommand("REPO");

    // save initial commit
    return;
  }
}
