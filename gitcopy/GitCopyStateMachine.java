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
      if (currState == GitCopyStates.UNSTAGED) {
        addFileAndStateToMachine(filename, GitCopyStates.STAGED);
      } else {
        throw new IllegalArgumentException(
            "The file has to be in an unstaged state to move into staged.");
      }
    } else if (input == "commit") {
      if (currState == GitCopyStates.STAGED) {
        addFileAndStateToMachine(filename, GitCopyStates.COMMITTED);
      } else {
        throw new IllegalArgumentException(
            "File must be in a staged state to be committed. Exceptions only for init.");
      }
    }
  }

}
