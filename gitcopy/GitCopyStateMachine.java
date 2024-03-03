package gitcopy;

public class GitCopyStateMachine {
  private GitCopyState currentState;

  public GitCopyStateMachine() {
    GitCopyState currentState = new UninitializedState();

  }

  public GitCopyState getCurrentState() {
    return currentState;

  }
}
