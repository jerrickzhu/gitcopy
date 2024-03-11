package gitcopy;

import java.io.Serializable;

public enum GitCopyStates implements Serializable {
  UNINITIALIZED,
  INITIALIZED,
  UNSTAGED,
  STAGED,
  COMMITTED
}