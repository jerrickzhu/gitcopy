package gitcopy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class StateMachine implements Serializable {
  protected Map<String, GitCopyStates> currentStates;

  public StateMachine() {
    this.currentStates = new HashMap<>();
  }

  /**
   * Add new files or update files that are tracked in state machine.
   * 
   * @param filename
   * @param state    - The State you need to add in for this file
   */
  public void updateFileAndStateToMachine(String filename, GitCopyStates state, boolean isDelete) {
    if (!isDelete) {
      this.currentStates.put(filename, state);
    } else {
      if (fileInStateMachine(filename)) {
        removeFile(filename);
      }
    }
  }

  /** Get files in state machine. */
  public Map<String, GitCopyStates> getFiles() {
    return this.currentStates;
  }

  /** Removes file from the hashmap in state machine. */
  protected void removeFile(String filename) {
    if (fileInStateMachine(filename)) {
      this.currentStates.remove(filename);
    }
  }

  /**
   * Checks if the file is tracked.
   * 
   * @param filename
   * @return returns true or false if file is tracked
   */
  public boolean fileInStateMachine(String filename) {
    return this.currentStates.containsKey(filename);
  }

  /**
   * Retrieves the state of a file.
   * 
   * @param filename
   * @return GitCopyStates, the state of the file.
   */
  public GitCopyStates getCurrentStateOfFile(String filename) {
    return this.currentStates.get(filename);
  }

}
