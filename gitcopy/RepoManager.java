package gitcopy;

import java.io.File;
import java.io.IOException;

/**
 * This class manages the saving and loading of serialized repositories.
 * Abstracts further to include better clarity and readability for code
 * instead of just Utils everywhere.
 */
public class RepoManager {
  private static final String REPO_DIRECTORY = System.getProperty("user.dir") + File.separator + ".gitcopy";

  /** Save our repo and write to disk using utilities. */
  public static void saveRepo(Repo repo, String filename) throws IOException {
    File repoFile = Utils.createFileInCurrentDirectory("./.gitcopy", Repo.DEFAULT_SHA1);
    repoFile.createNewFile();
    System.out.println("test");
    Utils.writeSerializedObjectToFile(repoFile, repo);
    System.out.println("success writing");
  }

  /** Load in our repo */
  public static Repo loadRepo() throws IllegalArgumentException {
    File repoFile = new File(REPO_DIRECTORY, Repo.DEFAULT_SHA1);
    return Utils.deserialize(repoFile, Repo.class);
  }
}
