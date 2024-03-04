package gitcopy.state_machine.states;

public class UninitializedState extends GitCopyState {

  @Override
  public void processCommand() {
    /**
     * Left intentionally empty as there should be nothing done
     * during an uninitialized state; therefore, there is no processing.
     */
  }
}
