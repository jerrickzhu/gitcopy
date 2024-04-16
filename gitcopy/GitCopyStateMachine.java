package gitcopy;

import java.io.IOException;
import java.io.Serializable;

public class GitCopyStateMachine extends StateMachine implements Serializable {

  /**
   * Initialize the state machine. Data contained in hashmap
   * where the key is the file name and value is the state.
   * The initial key in the hashmap is "REPO" which is the repository.
   * It starts as an uninitialized state and transitions later on.
   */
  public GitCopyStateMachine() {
    super();
  }

  /**
   * Transitions state depending on the input given and the current state.
   * 
   * @param input
   * @param filename
   * @throws IOException
   */
  public void transitionState(String input, String filename) throws IOException {
    GitCopyStates currState = this.getCurrentStateOfFile(filename);
    switch (input) {
      case "add":
        if (currState == GitCopyStates.UNSTAGED) {
          this.updateFileAndStateToMachine(filename, GitCopyStates.STAGED, false);
          break;
        }
      case "commit":
        if (currState == GitCopyStates.STAGED) {
          this.updateFileAndStateToMachine(filename, GitCopyStates.UNSTAGED, false);
          break;
        }
      case "remove":
        if (currState == GitCopyStates.STAGED || currState == GitCopyStates.UNSTAGED) {
          this.updateFileAndStateToMachine(filename, GitCopyStates.UNSTAGED, true);
          break;
        }
    }
  }
}
