package gitcopy;

/**
 * Base class for construts that represent States in this State Machine.
 */
public abstract class GitCopyState {

  /**
   * This function directs each distinct state to have its own processing.
   * The state processes its own commands.
   */
  public abstract void processCommand();

}
