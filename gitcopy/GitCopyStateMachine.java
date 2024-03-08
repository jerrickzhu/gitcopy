package gitcopy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GitCopyStateMachine implements Serializable {
  private Map<String, GitCopyStates> currentStates;

  /**
   * Initialize the state machine. Data contained in hashmap
   * where the key is the file name and value is the state.
   * The initial key in the hashmap is "REPO" which is the repository.
   * It starts as an uninitialized state and transitions later on.
   */
  public GitCopyStateMachine() {
    this.currentStates = new HashMap<>();
    GitCopyStates repoState = GitCopyStates.UNINITIALIZED;
    this.currentStates.put("REPO", repoState);

  }

  public GitCopyStates getCurrentStateOfFile(String filename) {
    return this.currentStates.get(filename);
  }

  public boolean fileInStateMachine(String filename) {
    if (this.currentStates.containsKey(filename)) {
      return true;
    }
    return false;
  }

  public void addFileAndStateToMachine(String filename, GitCopyStates state) {
    this.currentStates.put(filename, state);
  }

  public void transitionState(String input, String filename) {
    GitCopyStates currState = getCurrentStateOfFile(filename);

    if (input == "init") {
      if (currState == GitCopyStates.UNINITIALIZED) {
        // This line updates the REPO key to initialized state.
        addFileAndStateToMachine("REPO", GitCopyStates.INITIALIZED);
      } else {
        throw new IllegalArgumentException(
            "The repository must be in an uninitialized state if you use the init command.");
      }
    } else if (input == "add") {

      // otherwise, notify that we already have this file and do not allow duplicate
      // copies of files
      return;
    } else if (input == "commit") {
      if (currState == GitCopyStates.STAGED) {
        // to do: commit actions
      }
      // check the state of files to see if they are a StagedState.
      // if so, we may proceed.
      // if not, we cannot proceed.
    }
  }

}
