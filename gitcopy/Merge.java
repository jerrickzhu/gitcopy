package gitcopy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Merge {

  /**
   * Function that checks conditions of if the given branch and curr branch have
   * been modded.
   * 1. Checks if modifications are the same. If so, take either commit.
   * 1a. Checks if modifications if given and curr are BOTH delete.
   * 2. Checks if the modifications are different. If so, conflict.
   * 
   * @throws IOException
   */
  public static void isModdedGivenAndCurr(Map<String, String> LCASnapShot, Map<String, String> givenBranchSnapShot,
      Map<String, String> currBranchSnapShot, Map<String, String> mergeSnapShot, GitCopyStateMachine stateMachine,
      Map<String, Blob> fileBlobs) throws IOException {
    for (Map.Entry<String, String> entry : LCASnapShot.entrySet()) {
      String LCAFileName = entry.getKey();
      boolean fileInGivenBranch = givenBranchSnapShot.containsKey(LCAFileName);
      boolean fileInCurrBranch = currBranchSnapShot.containsKey(LCAFileName);
      String LCAFileBlobSHA1 = entry.getValue();
      String givenFileBlobSHA1 = givenBranchSnapShot.get(LCAFileName);
      String currFileBlobSHA1 = currBranchSnapShot.get(LCAFileName);

      // Checks the first condition in comments -- same modifications in branches
      if (fileInCurrBranch && fileInGivenBranch) {
        if (currFileBlobSHA1.equals(givenFileBlobSHA1) && !currFileBlobSHA1.equals(LCAFileBlobSHA1)
            && !givenFileBlobSHA1.equals(LCAFileBlobSHA1)) {
          mergeSnapShot.put(LCAFileName, currFileBlobSHA1);
          // update state machine and fileblobs map. Don't need to update anything because
          // the file should already be in the current branch's state machine, so just
          // need to transition state
          stateMachine.transitionState("add", LCAFileName);
          fileBlobs.put(LCAFileName, FileUtils.loadObject(Blob.class, givenFileBlobSHA1, Repo.BLOB_DIRECTORY));
        }
      }

      // Checks condition 1a -- deleted same file in both branches
      if (!fileInCurrBranch && !fileInGivenBranch) {
        mergeSnapShot.remove(LCAFileName);
      }

      // Checks condition 2 -- if both files in branches differ in modifications
      if (fileInCurrBranch && fileInGivenBranch) {
        if (!givenFileBlobSHA1.equals(LCAFileBlobSHA1) && !givenFileBlobSHA1.equals(currFileBlobSHA1)
            && !currFileBlobSHA1.equals(LCAFileBlobSHA1)) {
          System.out.println("Conflict between branches in " + LCAFileName + ". Please resolve them.");
          mergeSnapShot.remove(LCAFileName);
        }
      }
    }
  }

  public static void oneBranchChangesOnly(Map<String, String> LCASnapShot,
      Map<String, String> givenBranchSnapShot, Map<String, String> currBranchSnapShot,
      Map<String, String> mergeSnapShot, GitCopyStateMachine stateMachine, Map<String, Blob> fileBlobs)
      throws IOException {

    Map<String, String> notModifiedMap = new HashMap<>();
    Map<String, String> modifiedMap = new HashMap<>();

    findUnmoddedFilesSinceLCA(LCASnapShot, currBranchSnapShot, notModifiedMap);
    findModdedFilesSinceLCA(LCASnapShot, givenBranchSnapShot, modifiedMap);
    moddedGivenButUnchangedCurr(notModifiedMap, modifiedMap, mergeSnapShot, stateMachine,
        fileBlobs);

  }

  private static void moddedGivenButUnchangedCurr(Map<String, String> unmodded, Map<String, String> modded,
      Map<String, String> mergeSnapShot, GitCopyStateMachine stateMachine, Map<String, Blob> fileBlobs)
      throws IOException {
    for (Map.Entry<String, String> unmoddedEntry : unmodded.entrySet()) {
      String unmoddedFileName = unmoddedEntry.getKey();
      for (Map.Entry<String, String> moddedEntry : modded.entrySet()) {
        String moddedFileName = moddedEntry.getKey();
        String moddedBlobSHA1 = moddedEntry.getValue();
        // check if file hasn't been modded in curr branch but has been modded in given
        // branch
        if (unmoddedFileName.equals(moddedFileName)) {
          // add into your mergeSnapShot so we know what to commit later
          mergeSnapShot.put(moddedFileName, moddedBlobSHA1);
          // change state machine. This should change the current branch state machine so
          // we now have the proper states to commit later
          stateMachine.updateFileAndStateToMachine(moddedFileName, GitCopyStates.UNSTAGED, false);
          stateMachine.transitionState("add", moddedFileName);
          fileBlobs.put(moddedFileName, FileUtils.loadObject(Blob.class, moddedBlobSHA1, Repo.BLOB_DIRECTORY));
        }
      }
    }
  }

  /** Helper function to find files we do need to merge */
  private static void findModdedFilesSinceLCA(Map<String, String> LCASnapShot,
      Map<String, String> branchToCheck,
      Map<String, String> map) throws IOException {
    for (Map.Entry<String, String> LCAEntry : LCASnapShot.entrySet()) {
      String LCAFileName = LCAEntry.getKey();
      String LCABlobSHA1 = LCAEntry.getValue();
      for (Map.Entry<String, String> entry : branchToCheck.entrySet()) {
        String fileName = entry.getKey();
        String blobSHA1 = entry.getValue();
        if (LCAFileName.equals(fileName) && !LCABlobSHA1.equals(blobSHA1)) {
          map.put(fileName, blobSHA1);
        }
      }
    }

  }

  /** Helper function to find files we do not need to merge */
  private static void findUnmoddedFilesSinceLCA(Map<String, String> LCASnapShot,
      Map<String, String> branchToCheck,
      Map<String, String> map) throws IOException {
    for (Map.Entry<String, String> LCAEntry : LCASnapShot.entrySet()) {
      String LCAFileName = LCAEntry.getKey();
      String LCABlobSHA1 = LCAEntry.getValue();
      for (Map.Entry<String, String> entry : branchToCheck.entrySet()) {
        String fileName = entry.getKey();
        String blobSHA1 = entry.getValue();
        if (LCAFileName.equals(fileName) && LCABlobSHA1.equals(blobSHA1)) {
          map.put(fileName, blobSHA1);
        }
      }
    }
  }

  /**
   * Finds the latest common ancestor commit to enable merging.
   * 
   * @param commits - An array list containing all the commits you intend to find
   *                the LCA for.
   * @return - ArrayList<Commit>, which should only contain one Commit object: the
   *         LCA of all branch commits
   */
  public static ArrayList<Commit> findLatestCommonAncestor(ArrayList<Commit> commits) {
    if (commits.size() <= 1) {
      return commits;
    }
    // appender holds Commits we need to find the LCA for. Max of 2 always.
    ArrayList<Commit> appender = new ArrayList<>();
    ArrayList<Commit> res = new ArrayList<>();
    // Iterate through the entire array list of commits
    for (int index = 0; index < commits.size(); index++) {
      int pairCount = 0;
      int indexMover = index;
      // We need to find the LCA of each pair, so this limits our appender size to
      // stop at the pair
      while (pairCount != 2 && indexMover < commits.size()) {
        appender.add(commits.get(indexMover));
        pairCount++;
        indexMover++;
      }
      // Once you have a pair in appender, we can go ahead and find its LCA.
      // Append LCA result to the res arraylist.
      if (appender.size() > 1) {
        Commit firstCommit = appender.get(0);
        Commit secondCommit = appender.get(1);
        Commit commonAncestorOfPair = findLatestCommonAncestorOfPair(firstCommit, secondCommit);
        res.add(commonAncestorOfPair);

      }
      appender = new ArrayList<>();
    }
    // With the res array list, we recursively look for its LCA again.
    return findLatestCommonAncestor(res);
  }

  /** Finds the LCA of the pair of commits */
  private static Commit findLatestCommonAncestorOfPair(Commit firstCommit, Commit secondCommit) {

    // Gather the ancestors of each commit. LinkedHashSet should provide a
    // deterministic way to iterate through the ancestors in the order SHA1's are
    // added to them.
    Set<String> ancestor1 = new LinkedHashSet<>();
    findAncestors(firstCommit, ancestor1, new HashSet<>());
    Set<String> ancestor2 = new LinkedHashSet<>();
    findAncestors(secondCommit, ancestor2, new HashSet<>());

    // Find the latest common ancestor by comparing ancestors. Iterate through
    // ancestor1, the linked hashset. The first commit object found is the latest
    // common ancestor.
    Commit latestAncestor = null;
    for (String ancestor : ancestor1) {
      if (ancestor2.contains(ancestor)) {
        return FileUtils.loadObject(Commit.class, ancestor, Repo.COMMIT_DIRECTORY);
      }
    }
    return latestAncestor;
  }

  /**
   * Inputs SHA1 of commit ancestors by recursively going through parents of
   * commits and adding their SHA1's to ancestor
   * 
   * @param commit    - The commit you want to find the ancestors of (parents,
   *                  parents of parents, etc.)
   * @param ancestors - A LinkedHashSet of all the ancestors SHA1s. Order of this
   *                  set is maintained to preserve integrity of commit lineage
   * @param visited   - A hashset to contain all visited parents.
   */
  private static void findAncestors(Commit commit, Set<String> ancestors, Set<String> visited) {
    // Base case. If we have both the INIT SHA1 in visited and we are at the commit
    // node, we may return
    if (commit.getSHA1().equals(Repo.COMMIT_INIT_SHA1) && visited.contains(Repo.COMMIT_INIT_SHA1)) {
      return;
    }
    visited.add(commit.getSHA1());
    ancestors.add(commit.getSHA1());
    for (String commitParent : commit.getParents()) {
      Commit commitAncestor = FileUtils.loadObject(Commit.class, commitParent, Repo.COMMIT_DIRECTORY);
      findAncestors(commitAncestor, ancestors, visited);
    }
  }

}
