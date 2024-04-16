package gitcopy;

import java.io.IOException;

public class RepoStateMachine extends StateMachine {

  public RepoStateMachine() {
    super();
    initializeRepo();
  }

  private void initializeRepo() {
    this.currentStates.put("REPO", GitCopyStates.UNINITIALIZED);
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
    switch (input) {
      case "init":
        if (currState == GitCopyStates.UNINITIALIZED) {
          updateFileAndStateToMachine("REPO", GitCopyStates.INITIALIZED, false);
          break;
        }
    }
  }

}
