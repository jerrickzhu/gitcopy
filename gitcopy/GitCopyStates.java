package gitcopy;

import java.io.Serializable;

public enum GitCopyStates implements Serializable {
  UNINITIALIZED,
  INITIALIZED,
  STAGED,
  COMMITTED
}