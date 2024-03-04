package gitcopy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GitCopyStateMachine implements Serializable {
  private Map<String, GitCopyState> currentStates;

  public GitCopyStateMachine() {
    this.currentStates = new HashMap<>();
    this.currentStates.put("REPO", new UninitializedState());
  }

  public GitCopyState getCurrentState(String filename) {
    return this.currentStates.get(filename);
  }

  public void addFileAndStateToMachine(String filename, GitCopyState state) {
    this.currentStates.put(filename, state);
  }

  public void transitionState(GitCopyState currState, String input) {
    if (input == "init") {
      if (currState instanceof UninitializedState) {

      }
    }
  }
}
