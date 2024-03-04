package gitcopy.state_machine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import gitcopy.state_machine.states.GitCopyState;
import gitcopy.state_machine.states.InitializedState;
import gitcopy.state_machine.states.UninitializedState;

public class GitCopyStateMachine implements Serializable {
  private Map<String, GitCopyState> currentStates;

  /**
   * Initialize the state machine. Data contained in hashmap
   * where the key is the file name and value is the state.
   * The initial key in the hashmap is "REPO" which is the repository.
   * It starts as an uninitialized state and transitions later on.
   */
  public GitCopyStateMachine() {
    this.currentStates = new HashMap<>();
    this.currentStates.put("REPO", new UninitializedState());
  }

  public GitCopyState getCurrentStateOfFile(String filename) {
    return this.currentStates.get(filename);
  }

  public void addFileAndStateToMachine(String filename, GitCopyState state) {
    this.currentStates.put(filename, state);
  }

  public void transitionState(String input, String filename) {
    GitCopyState currState = getCurrentStateOfFile(filename);
    if (input == "init") {
      if (currState instanceof UninitializedState) {
        addFileAndStateToMachine("REPO", new InitializedState());
      } else {
        throw new IllegalArgumentException(
            "The repository must be in an uninitialized state if you use the init command.");
      }
    } else if (input == "add") {
      // check if the filename already exists in our statemachine hashmap
      // if it does not, then we may add it in.
      // otherwise, notify that we already have this file and do not allow duplicate
      // copies of files
      return;
    }
  }

  public void processStateCommand(String filename) {
    GitCopyState currFileState = getCurrentStateOfFile(filename);
    currFileState.processCommand();
  }
}
