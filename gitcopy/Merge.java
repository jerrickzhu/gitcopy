package gitcopy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Merge {

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
        latestAncestor = FileUtils.loadObject(Commit.class, ancestor, Repo.COMMIT_DIRECTORY);
        break;
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
