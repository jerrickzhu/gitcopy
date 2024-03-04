package gitcopy;

import java.io.File;

import gitcopy.state_machine.GitCopyStateMachine;

public class Repo {
  public GitCopyStateMachine STATE_MACHINE;

  public void initializeRepo() {

    STATE_MACHINE = new GitCopyStateMachine();
    STATE_MACHINE.transitionState("init", "REPO");
    STATE_MACHINE.processStateCommand("REPO");

    // save initial commit
    return;
  }

}
