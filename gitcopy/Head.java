package gitcopy;

import java.io.File;
import java.io.IOException;

public class Head {

  private static String GITCOPY_FOLDER = FileUtils.findGitCopyRootDirectory().getAbsolutePath() + File.separator
      + ".gitcopy";
  private static String BRANCHES_FOLDER = GITCOPY_FOLDER + File.separator + ".branches";

  public static void setGlobalHead(String branchName, Commit commit) throws IOException {
    Branch branch = new Branch(branchName, commit);
    FileUtils.saveObjectToFileDisk("HEAD", GITCOPY_FOLDER, branch);
  }

  public static void setBranchHead(String branchName, Commit commit) throws IOException {
    Branch branch = new Branch(branchName, commit);
    String branchFolderPath = GITCOPY_FOLDER + File.separator + ".branches";
    FileUtils.saveObjectToFileDisk(branchName, branchFolderPath, branch);
  }

  public static String getGlobalHeadCommitSHA1() {
    Branch branch = FileUtils.loadObject(Branch.class, "HEAD", GITCOPY_FOLDER);
    return branch.getCommitHeadSHA1();
  }

  public static Commit getGlobalHeadCommit() {
    Branch branch = FileUtils.loadObject(Branch.class, "HEAD", GITCOPY_FOLDER);
    return branch.getHead();
  }

  public static Commit getBranchHeadCommit(String branchName) {
    Branch branch = FileUtils.loadObject(Branch.class, branchName, BRANCHES_FOLDER);
    return branch.getHead();
  }

  public static Branch getBranch(String branchName) {
    return FileUtils.loadObject(Branch.class, branchName, BRANCHES_FOLDER);
  }

}
