package gitcopy;

import java.io.Serializable;

public class UninitializedState extends GitCopyState implements Serializable {

  @Override
  public void processCommand() {
    /**
     * Left intentionally empty as there should be nothing done
     * during an uninitialized state; therefore, there is no processing.
     */
  }
}
