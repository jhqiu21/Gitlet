package gitlet;

import javax.net.ssl.StandardConstants;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *
 *  Structure
 *
 *
 * .gitlet
 *    |-- objects: Store commit and blob using hash value
 *    |      |-- commit
 *    |      |-- blob
 *    |-- refs
 *    |    |-- heads
 *    |         |-- master: "main" branch
 *    |         |-- branch #
 *    |-- HEAD_FILE: contains a branch name(String) Point to the current branch
 *    |-- add_stage
 *    |-- remove_stage
 *
 *
 *
 *  @author QIU JINHANG
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File REF_DIR = join(GITLET_DIR, "ref");
    public static final File HEADS_DIR = join(REF_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File ADDSTAGE_FILE = join(GITLET_DIR, "add_stage");
    public static final File REMOVESTAGE_FILE = join(GITLET_DIR, "remove_stage");

    public static Commit commit;
    public static Stage addStage = new Stage();
    public static Stage removeStage = new Stage();


    /* init command */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        REF_DIR.mkdir();
        HEADS_DIR.mkdir();
        initCommit();
        initHead();
        initHeadPointer();
    }

    public static void checkInit() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * init and save a commit in the data base
     */
    private static void initCommit() {
        Commit initcommit = new Commit();
        commit = initcommit;
        initcommit.save();
    }

    /**
     * initialize a head pointer
     */
    private static void initHead() {
        writeObject(HEADS_DIR, "master");
        writeContents(HEAD_FILE, commit.getId());
    }

    /**
     * initialize a head pointer to the master branch
     */
    private static void initHeadPointer() {
        writeObject(HEAD_FILE, "master");
    }

    /**
     * Adds a file to the version control system by creating a Blob object and storing it.
     *
     * <p>This method performs the following operations:
     * <ul>
     *   <li>Gets the File object for the specified file path.</li>
     *   <li>Checks if the file exists. If not, prints an error message and exits.</li>
     *   <li>Creates a Blob object from the file.</li>
     *   <li>Stores the Blob using the storeBlob method.</li>
     * </ul>
     * </p>
     *
     * @param file the path of the file to be added
     */
    public static void add(String file) {
        File fileToAdd = getFile(file);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(fileToAdd);
        storeBlob(blob);
    }

    /**
     * Converts a file path string into a File object, handling both absolute and relative paths.
     *
     * <p> Checks if the provided file path is absolute.
     * If it is, it initializes and returns a File object using the absolute path.
     * If it is relative, it joins the current working directory with the file path
     * to create and return a File object
     * </p>
     *
     * @param file the path of the file
     * @return the File object representing the specified file path
     */
    public static File getFile(String file) {
        return Paths.get(file).isAbsolute()
                ? new File(file)
                : join(CWD, file);
    }

    /**
     * Stores a blob in the appropriate stage area based on its current state.
     * @param blob the {@code Blob} object to be stored
     */
    public static void storeBlob(Blob blob) {
        addStage = readAddStage();
        removeStage = readRemoveStage();
        commit = readCommit();
        if (!commit.getBlobRef().containsKey(blob.getBlobPath()) || !removeStage.containsBlob(blob)) {
            if (!addStage.containsBlob(blob) && !removeStage.containsBlob(blob)) {
                blob.save();
            }
            if (addStage.containsFilePath(blob.getBlobPath())) {
                addStage.delete(blob);
            }
            addStage.add(blob);
            addStage.saveAddStage();
        } else {
            removeStage.delete(blob);
            removeStage.saveRemoveStage();
        }
    }

    /**
     * Checks if the add stage file exists. If not, returns a new Stage object.
     * Otherwise, reads the add stage from the file and returns it.
     * @return Add Stage
     */
    private static Stage readAddStage() {
        if (!ADDSTAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(ADDSTAGE_FILE, Stage.class);
    }

    /**
     * Checks if the remove stage file exists. If not, returns a new Stage object.
     * Otherwise, reads the remove stage from the file and returns it.
     * @return Remove Stage
     */
    private static Stage readRemoveStage() {
        if (!REMOVESTAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(REMOVESTAGE_FILE, Stage.class);
    }

    /**
     * Gets the current commit ID and constructs the file path for the current commit.
     * Reads the commit object from the file and returns it.
     * @return Current Commit
     */
    private static Commit readCommit() {
        String currCommitId = getCurrCommitId();
        File CURR_COMMIT_FILE = join(OBJECT_DIR, currCommitId);
        return readObject(CURR_COMMIT_FILE, Commit.class);
    }

    /**
     * Gets the current branch name and constructs the file path for the
     * head pointer file of the current branch. Reads the content of the
     * head pointer file (which contains the current commit ID) and returns it.
     * @return Current Commit Id in current branch
     */
    private static String getCurrCommitId() {
        String currBranch = getCurrBranch();
        File HEAD_POINT_FILE = join(HEADS_DIR, currBranch);
        return readContentsAsString(HEAD_POINT_FILE);
    }

    /**
     * Get Current Branch
     * @return current branch name
     */
    private static String getCurrBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    /**
     * Commit command
     * @param message Every commit must have a non-blank message
     */
    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = createCommit(message);
    }

    /**
     * Creat a new commit with a non-blank message
     * @param message of new commit
     */
    private static Commit createCommit(String message) {
        Map<String, String> addStageBlob = getAddStageBlobMap();
        Map<String, String> removeStageBlob = getRemoveStageBlobMap();
        checkIfStageEmpty(addStageBlob, removeStageBlob);
        commit = readCommit();
        Map<String, String> newBlobMap = commit.getBlobRef();
        newBlobMap = createBlobMap(newBlobMap, addStageBlob, removeStageBlob);
        List<String> parents = findParents();
        return new Commit(message, newBlobMap, parents);
    }

    /**
     * Save the new commit created to the tree
     * @param newCommit to save
     */
    private static void saveCommit(Commit newCommit) {
        newCommit.save();
        addStage.clear();
        addStage.saveAddStage();
        removeStage.clear();
        removeStage.saveRemoveStage();

    }

    /**
     * Get a Map of Blob in add stage
     * @return blob in add stage
     */
    private static Map<String, String> getAddStageBlobMap() {
        Map<String, String> addStageBlobMap = new HashMap<String, String>();
        addStage = readAddStage();
        List<Blob> addBlobList = addStage.getBlobList();
        for (Blob blob : addBlobList) {
            addStageBlobMap.put(blob.getBlobPath(), blob.getId());
        }
        return addStageBlobMap;
    }

    /**
     * Get a Map of Blob in remove stage
     * @return blob in remove stage
     */
    private static Map<String, String> getRemoveStageBlobMap() {
        Map<String, String> removeStageBlobMap = new HashMap<String, String>();
        removeStage = readRemoveStage();
        List<Blob> removeBlobList = removeStage.getBlobList();
        for (Blob blob : removeBlobList) {
            removeStageBlobMap.put(blob.getBlobPath(), blob.getId());
        }
        return removeStageBlobMap;
    }

    /**
     * Check if the blob in the stage or not
     * <p>If no files have been staged, abort. Print the message No changes added to the commit.</p>
     * @param addStageBlobMap of current add stage
     * @param removeStageBlobMap of current remove stage
     */
    private static void checkIfStageEmpty(Map<String,String> addStageBlobMap,
                                          Map<String,String> removeStageBlobMap) {
        if (addStageBlobMap.isEmpty() && removeStageBlobMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    /**
     * Find parents id of current commits
     * @return List of parents of current commits
     */
    private static List<String> findParents() {
        List<String> parents = new ArrayList<String>();
        commit = readCommit();
        parents.add(commit.getId());
        return parents;
    }

    /**
     * Create blob map for current commits, add all blob in add stage
     * and remove stage to the copy of blob of current commit
     *
     * @param newBlobMap blob map of new commit
     * @param addBlobMap blob map of add stage
     * @param removeBlobMap blob map of remove stage
     */
    private static Map<String,String> createBlobMap(Map<String,String> newBlobMap,
                                                    Map<String,String> addBlobMap,
                                                    Map<String,String> removeBlobMap) {
        if (!addBlobMap.isEmpty()) {
            for (String path : addBlobMap.keySet()) {
                newBlobMap.put(path, addBlobMap.get(path));
            }
        }

        if (!removeBlobMap.isEmpty()) {
            for (String path : removeBlobMap.keySet()) {
                newBlobMap.put(path, removeBlobMap.get(path));
            }
        }
        return newBlobMap;
    }

    /**
     * Save new commit as the head of current branch and update
     * current Commit and HEAD_FILE
     * @param newCommit to save
     */
    private static void saveHead(Commit newCommit) {
        commit = newCommit;
        String currentBranch = readCurrentBranch();
        File HEADS_FILE = join(HEADS_DIR, currentBranch);
        writeContents(HEADS_FILE, commit.getId());
    }

    /**
     * Get current branch (from HEAD_FILE)
     * @return current branch
     */
    private static String readCurrentBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    /**
     * Implement rm command, remove the target file
     * @param fileName of target file to remove
     */
    public static void rm(String fileName) {
        File file = getFile(fileName);
        String filePath = file.getPath();
        addStage = readAddStage();
        commit = readCommit();

        if (addStage.contains(filePath)) {
            addStage.delete(filePath);
            addStage.saveAddStage();
        } else if (commit.contains(filePath)){
            removeStage = readRemoveStage();
            Blob removeBlob = getBlobFromPath(filePath, commit);
            removeStage.add(removeBlob);
            removeStage.saveRemoveStage();
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /**
     * Get target blob through blob path in the target commit
     * @param filePath of target blob
     * @param currCommit target commit
     */
    private static Blob getBlobFromPath(String filePath, Commit currCommit) {
        String blobID = currCommit.getBlobRef().get(filePath);
        return getBlobFromId(blobID);
    }

    /**
     * TODO: Can we move this method to the subclass
     * Get target blob file through id
     * @param blobId of target blob
     * @return target blob
     */
    public static Blob getBlobFromId(String blobId) {
        File BLOB_FILE = join(OBJECT_DIR, blobId);
        return readObject(BLOB_FILE, Blob.class);
    }

    /**
     * Implement log command, print the log of commit tree
     */
    public static void log() {
        commit = readCommit();
        while (!commit.getParentId().isEmpty()) {
            if (isMergedCommit(commit)) {
                printMergedCommit(commit);
            } else {
                printCommit(commit);
            }
            commit = getCommitFromId(commit.getParentId().get(0));
        }
        printCommit(commit);
    }

    /**
     * Determine whether the commit is a merge commit
     * a.k.a it has two parents
     * @param commit to verify
     * @return boolean value
     */
    private static boolean isMergedCommit(Commit commit) {
        return commit.getParentId().size() == 2;
    }

    /**
     * Print info of target commit
     * @param commit target commit
     */
    private static void printCommit(Commit commit) {
        System.out.println("===");
        printCommitID(commit);
        printCommitDate(commit);
        printCommitMessage(commit);
    }

    /**
     * Print info of target merged commit
     * @param commit target merged commit
     */
    private static void printMergedCommit(Commit commit) {
        System.out.println("===");
        printCommitID(commit);
        printMergeMark(commit);
        printCommitDate(commit);
        printCommitMessage(commit);
    }

    /**
     * Print commit id in log
     * @param commit to be print in log
     */
    private static void printCommitID(Commit commit) {
        System.out.println("commit " + commit.getId());
    }

    /**
     * Print commit date in log
     * @param commit to be print in log
     */
    private static void printCommitDate(Commit commit) {
        System.out.println("Date: " + commit.getTimestamp());
    }

    /**
     * Print commit message in log with a empty line
     * @param commit to be print in log
     */
    private static void printCommitMessage(Commit commit) {
        System.out.println(commit.getMessage() + "\n");
    }

    /**
     * Print merge mark in log, list parents of merged commit consist of
     * the first seven digits of the first and second parents’ commit ids.
     * The first parent is the branch you were on when you did the merge;
     * The second is that of the merged-in branch.
     *
     * @param commit to be print in log
     */
    private static void printMergeMark(Commit commit) {
        List<String> parentsList = commit.getParentId();
        String p1 = parentsList.get(0).substring(0, 7);
        String p2 = parentsList.get(1).substring(0, 7);
        System.out.println("Merge: " + p1 + " " + p2);
    }

    /**
     * Find target commit through commit id
     * Note that a convenient feature of real Git is that one can abbreviate
     * commits with a unique prefix, thus, there will be id whose length is less
     * than 40, for this situation, we just traverse id of all file and find the file
     * which has a prefix same with this id
     *
     * @param commitId id of target commit
     * @return target commit
     */
    private static Commit getCommitFromId(String commitId) {
        if (commitId.length() == 40) {
            File file = join(OBJECT_DIR, commitId);
            return file.exists()
                    ? readObject(file, Commit.class)
                    : null;
        } else {
            List<String> idList = plainFilenamesIn(OBJECT_DIR);
            for (String obj : idList) {
                if (commitId.equals(obj.substring(0, commitId.length()))) {
                    File file = join(OBJECT_DIR, obj);
                    return readObject(file, Commit.class);
                }
            }
            return null;
        }
    }

    /**
     * Implement global-log command, list all commit history
     */
    public static void global_log() {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        for (String id : commitList) {
            Commit curr = getCommitFromId(id);
            if (isMergedCommit(curr)) {
                printMergedCommit(curr);
            } else {
                printCommit(curr);
            }
        }
    }

    /**
     * Implement find command
     * Prints out the ids of all commits that have the given commit message
     * If there are multiple such commits, it prints the ids out on separate lines.
     *
     * @param message of target commits
     */
    public static void find(String message) {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        List<String> idList = new ArrayList<String>();
        for (String id : commitList) {
            Commit curr = getCommitFromId(id);
            if (message.equals(curr.getMessage())) {
                idList.add(id);
            }
        }

        if (idList.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String id : idList) {
                System.out.println(id);
            }
        }
    }

    /**
     * Implement status command
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     */
    public static void status() {
        printBranches();
        printStagedFiles();
        printRemovedFiles();
        printModificationsNotStaged();
        printUntrackedFiles();
    }

    /**
     * Displays all branches and marks the current branch with a '*'
     */
    private static void printBranches() {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        String currentBranch = getCurrBranch();
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch);
        if (branchList.size() == 1) {
            return;
        }
        for (String branch : branchList) {
            if (!branch.equals(currentBranch)) {
                System.out.println(branch);
            }
        }
        System.out.println();
    }

    /**
     * Displays what files have been staged for addition
     */
    private static void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        addStage = readAddStage();
        for (Blob blob : addStage.getBlobList()) {
            System.out.println(blob.getFileName());
        }
        System.out.println();
    }

    /**
     * Displays what files have been staged for removal
     */
    private static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        removeStage = readRemoveStage();
        for (Blob blob : removeStage.getBlobList()) {
            System.out.println(blob.getFileName());
        }
        System.out.println();
    }

    /**
     * Displays file in the working directory, which is “modified but not staged”
     *
     * <p>A file is modified but not staged if it is</p>
     * - Tracked in the current commit, changed in the working directory, but not staged; or
     * - Staged for addition, but with different contents than in the working directory; or
     * - Staged for addition, but deleted in the working directory; or
     * - Not staged for removal, but tracked in the current commit and deleted from the working directory.
     */
    private static void printModificationsNotStaged() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        // TODO: Bonus Part
        System.out.println();
    }

    /**
     * Displays files present in the working directory but neither staged for addition nor tracked
     *
     */
    private static void printUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        // TODO: Bonus Part
        System.out.println();
    }

    /**
     * Implement checkout command
     * Checkout is a kind of general command that can do a few different
     * things depending on what its arguments are.
     *
     * <p>Usages:</p>
     * <ul>
     *     <li> checkout -- [file name] </li>
     *     <li> checkout [commit id] -- [file name] </li>
     *     <li> checkout [branch name] </li>
     * </ul>
     *
     */

    /**
     * Case 1: checkout -- [file name]
     * Takes the version of the file as it exists in the head commit and puts it
     * in the working directory, overwriting the version of the file that’s already
     * there if there is one. The new version of the file is not staged.
     *
     * @param fileName to operate
     */
    public static void checkout(String fileName) {
        commit = readCommit();
        List<String> fileNameList = commit.getFileNameList();
        if (fileNameList.contains(fileName)) {
            Blob blob = commit.getBlobFromFileName(fileName);
            putBlobInCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /**
     * write blob to CWD, use writeContents method to write byte array
     * to the blob file in CWD
     * @param blob to write
     */
    private static void putBlobInCWD(Blob blob) {
        File file = join(CWD, blob.getFileName());
        byte[] byteCode = blob.getBytes();
        writeContents(file, new String(byteCode));
    }

    /**
     * Case 2: checkout [commit id] -- [file name]
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file that’s
     * already there if there is one. The new version of the file is not staged.
     *
     * @param id of target file
     * @param fileName of target file
     */
    public static void checkout(String id, String fileName) {
        commit = getCommitFromId(id);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        List<String> fileNameList = commit.getFileNameList();
        if (fileNameList.contains(fileName)) {
            Blob blob = commit.getBlobFromFileName(fileName);
            putBlobInCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /**
     * Case 3: checkout [branch name]
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files
     * that are already there if they exist. Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the
     * checked-out branch are deleted. The staging area is cleared,
     * unless the checked-out branch is the current branch
     *
     * <ul>
     * <li>If a working file is untracked in the current branch and would be overwritten by the checkout,
     * print There is an untracked file in the way; delete it, or add and commit it first. and exit;</li> TODO
     * <li>perform this check before doing anything else. Do not change the CWD.</li>
     * </ul>
     * @param branch given branch
     */
    public static void checkoutInBranch(String branch) {
        checkBranchExist(branch);
        checkCurrentBranch(branch);
        Commit newCommit = getCommitFromBranchName(branch);
        switchToNewCommit(newCommit);
        switchToNewBranch(branch);
    }

    /**
     * Check if the branch with given branch name exist in HEAD_DIR
     * If no branch with that name exists, print No such branch exists.
     *
     * @param branchName to check
     */
    private static void checkBranchExist(String branchName) {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (!branchList.contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
    }

    /**
     * Check if the branch with given branch name is current branch
     * If that branch is the current branch, print No need to checkout the current branch.
     *
     * @param branchName to check
     */
    private static void checkCurrentBranch(String branchName) {
        String currentBranch = readCurrentBranch();
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
    }

    /**
     * Get the latest commit of the given branch name
     * @param branchName of target branch
     * @return Commit
     */
    private static Commit getCommitFromBranchName(String branchName) {
        File branchNameFile = join(HEADS_DIR, branchName);
        String id = readContentsAsString(branchNameFile);
        return getCommitFromId(id);
    }

    /**
     * TODO
     *
     */
    private static void switchToNewCommit(Commit commit) {

    }

    /**
     * TODO
     *
     */
    private static void switchToNewBranch(String branchName) {

    }

    /**
     * Implement branch command
     *
     * Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch called “master”.
     *
     * @param branchName of new branch to create
     */
    public static void branch(String branchName) {
        List<String> allBranches = plainFilenamesIn(HEADS_DIR);
        if (allBranches.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File newBranch = join(HEADS_DIR, branchName);
        writeContents(newBranch, getCurrCommitId());
    }

    /**
     * Implement rm-branch command
     *
     * <p>Deletes the branch with the given name.</p>
     * Delete the pointer associated with the branch; it does not mean to delete all commits
     * that were created under the branch, or anything like that.
     *
     * <p>Note that</p>
     * Use isDictionary Method in Jave File class to check if the brach name file is a
     * dictionary, if not delete this file in the HEAD_DIR
     *
     * @param branchName to be removed
     */
    public static void rm_branch(String branchName) {
        String currentBranch = getCurrBranch();
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        if (!branchList.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File file = join(HEADS_DIR, branchName);
        if (!file.isDirectory()) {
            file.delete();
        }
    }

    /**
     * Implement reset command
     *
     * Checks out all the files tracked by the given commit. Removes tracked files
     * that are not present in that commit. Also moves the current branch’s head to
     * that commit node. See the intro for an example of what happens to the head pointer
     * after using reset. The [commit id] may be abbreviated as for checkout.
     * The staging area is cleared. The command is essentially checkout of an arbitrary commit
     * that also changes the current branch head.
     *
     * @param commitId of given commit
     */
    public static void reset(String commitId) {

    }
}
