package gitcopy;

import java.io.Serializable;

public class Repo implements Serializable {

  public GitCopyStateMachine STATE_MACHINE;
  static final String DEFAULT_SHA1 = "0000000000000000000000000000000000000000";

  public Repo() {
    // Initialization of a GitCopyStateMachine also creates the first entry
    // in its hashmap attribute with key "REPO" with a key of UninitializedState
    STATE_MACHINE = new GitCopyStateMachine();
  }

  public void initializeRepo() {

    // Since we already have a REPO key, we can now transition its state to
    // InitializedState via transitionState
    STATE_MACHINE.transitionState("init", "REPO");

    // Process that REPO key's value of IniitalizedState.
    STATE_MACHINE.processStateCommand("REPO");

    // We add in a new entry to have a parent commit, which is our
    // initial commit.
    GitCopyState initCommitState = new CommitState("init");
    STATE_MACHINE.addFileAndStateToMachine("INITIAL_COMMIT", initCommitState);

    // Process INITIAL_COMMIT key to save files to disk.
    STATE_MACHINE.processStateCommand("INITIAL_COMMIT");
    return;
  }
}
