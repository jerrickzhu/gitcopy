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

    if (input == "add") {
      if (currState == GitCopyStates.UNSTAGED) {
        this.updateFileAndStateToMachine(filename, GitCopyStates.STAGED, false);
      } else {
        throw new IllegalArgumentException(
            "The file has to be in an unstaged state to move into staged.");
      }
    } else if (input == "commit") {
      if (currState == GitCopyStates.STAGED) {
        this.updateFileAndStateToMachine(filename, GitCopyStates.UNSTAGED, false);
      } else {
        throw new IllegalArgumentException(
            "File must be in a staged state to be committed. Exceptions only for init.");
      }
    } else if (input == "rm") {
      if (currState == GitCopyStates.STAGED || currState == GitCopyStates.UNSTAGED) {
        this.updateFileAndStateToMachine(filename, GitCopyStates.UNSTAGED, true);
      } else {
        throw new IOException("Problem in removing file. File is not in an unstaged state to be removed.");
      }
    } else {
      throw new IllegalArgumentException(
          "Cannot transition a file to unstaged if it's not in staged state.");
    }
  }
}
