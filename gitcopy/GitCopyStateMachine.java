package gitcopy;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GitCopyStateMachine implements Serializable {
  private Map<String, GitCopyStates> currentStates;

  /**
   * Initialize the state machine. Data contained in hashmap
   * where the key is the file name and value is the state.
   * The initial key in the hashmap is "REPO" which is the repository.
   * It starts as an uninitialized state and transitions later on.
   */
  public GitCopyStateMachine() {
    this.currentStates = new HashMap<>();
    GitCopyStates repoState = GitCopyStates.UNINITIALIZED;
    this.currentStates.put("REPO", repoState);
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

  /** Get files in state machine. */
  public Map<String, GitCopyStates> getFiles() {
    return this.currentStates;
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

  /** Removes file from the hashmap in state machine. */
  private void removeFile(String filename) {
    if (fileInStateMachine(filename)) {
      this.currentStates.remove(filename);
    }
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
    } else if (input == "add") {
      if (currState == GitCopyStates.UNSTAGED || currState == GitCopyStates.COMMITTED) {
        updateFileAndStateToMachine(filename, GitCopyStates.STAGED, false);
      } else {
        throw new IllegalArgumentException(
            "The file has to be in an unstaged state to move into staged.");
      }
    } else if (input == "commit") {
      if (currState == GitCopyStates.STAGED) {
        updateFileAndStateToMachine(filename, GitCopyStates.COMMITTED, false);
      } else {
        throw new IllegalArgumentException(
            "File must be in a staged state to be committed. Exceptions only for init.");
      }
    } else if (input == "rm") {
      if (currState == GitCopyStates.STAGED) {
        updateFileAndStateToMachine(filename, GitCopyStates.UNSTAGED, true);
      } else {
        throw new IOException("Problem in removing file. File is not in an unstaged state to be rmoved.");
      }
    } else {
      throw new IllegalArgumentException(
          "Cannot transition a file to unstaged if it's not in staged state.");
    }
  }
}
