package gitcopy;

import java.io.IOException;

public class RepoStateMachine extends StateMachine {

  public RepoStateMachine() {
    super();
    initializeRepo();
  }

  /**
   * Transitions state depending on the input given and the current state.
   * 
   * @param input
   * @param filename
   * @throws IOException
   */
  public void transitionState(String input, String filename) throws IOException {
    GitCopyStates currState = getCurrentStateOfFile(filename);

    if (input == "init") {
      if (currState == GitCopyStates.UNINITIALIZED) {
        updateFileAndStateToMachine("REPO", GitCopyStates.INITIALIZED, false);
      } else {
        throw new IllegalArgumentException(
            "The repository must be in an uninitialized state if you use the init command.");
      }
    }
  }

}
