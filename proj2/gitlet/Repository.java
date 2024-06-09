package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.GitUtils.*;

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

    private static Commit commit;
    private static Stage addStage = new Stage();
    private static Stage removeStage = new Stage();


    /* init command */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(OBJECT_DIR);
        mkdir(REF_DIR);
        mkdir(HEADS_DIR);
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
     * init and save a commit in the database
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
        File headsFile = join(HEADS_DIR, "master");
        writeContents(headsFile, commit.getId());
    }

    /**
     * initialize a head pointer to the master branch
     */
    private static void initHeadPointer() {
        writeContents(HEAD_FILE, "master");
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
        if (!commit.getBlobRef().containsValue(blob.getId()) || removeStage.containsBlob(blob)) {
            if (!addStage.containsBlob(blob)) {
                if (!removeStage.containsBlob(blob)) {
                    blob.save();
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
        File currCommitFile = join(OBJECT_DIR, currCommitId);
        return readObject(currCommitFile, Commit.class);
    }

    /**
     * Gets the current branch name and constructs the file path for the
     * head pointer file of the current branch. Reads the content of the
     * head pointer file (which contains the current commit ID) and returns it.
     * @return Current Commit id in current branch
     */
    private static String getCurrCommitId() {
        String currBranch = getCurrBranch();
        File headFile = join(HEADS_DIR, currBranch);
        return readContentsAsString(headFile);
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
        saveCommit(newCommit);
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
        saveHead(newCommit);
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
    private static void checkIfStageEmpty(Map<String, String> addStageBlobMap,
                                          Map<String, String> removeStageBlobMap) {
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
    private static Map<String, String> createBlobMap(Map<String, String> newBlobMap,
                                                    Map<String, String> addBlobMap,
                                                    Map<String, String> removeBlobMap) {
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
        File headFile = join(HEADS_DIR, currentBranch);
        writeContents(headFile, commit.getId());
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
        } else if (commit.contains(filePath)) {
            removeStage = readRemoveStage();
            Blob removeBlob = getBlobFromPath(filePath, commit);
            removeStage.add(removeBlob);
            removeStage.saveRemoveStage();
            deleteFile(file);
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
     * Get target blob file through id
     * @param blobId of target blob
     * @return target blob
     */
    public static Blob getBlobFromId(String blobId) {
        File blobFile = join(OBJECT_DIR, blobId);
        return readObject(blobFile, Blob.class);
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
     * a.k.a. it has two parents
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
     * Print commit message in log with an empty line
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
    public static void globalLog() {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        for (String id : commitList) {
            try {
                Commit curr = getCommitFromId(id);
                if (isMergedCommit(curr)) {
                    printMergedCommit(curr);
                } else {
                    printCommit(curr);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid commit id: " + e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("Null reference: " + e.getMessage());
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
            try {
                Commit curr = getCommitFromId(id);
                if (message.equals(curr.getMessage())) {
                    idList.add(id);
                }
            } catch (IllegalArgumentException e) {
                //System.out.println(e.getMessage());
                //System.exit(0);
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
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
        if (branchList.size() > 1) {
            for (String branch : branchList) {
                if (!branch.equals(currentBranch)) {
                    System.out.println(branch);
                }
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
     * - Not staged for removal, but tracked in the current commit and deleted from the
     *   working directory.
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
        writeContents(file, new String(byteCode, StandardCharsets.UTF_8));
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
     * <li>If a working file is untracked in the current branch and would be overwritten
     * by the checkout, print There is an untracked file in the way; delete it, or add and
     * commit it first. and exit;</li> TODO
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
     * If that branch is the current branch, print No need to check out the current branch.
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
     * Puts all files tracked by new commit in the working directory, overwriting
     * the versions of the files that are already there if they exist.(tracked by both commit)
     * Delete all files only tracked by current commit
     * Clear all files in stage
     * @param newCommit to check out
     */
    private static void switchToNewCommit(Commit newCommit) {
        List<String> filesTrackedByCurrentCommit = getFilesTrackedByCurrentCommit(newCommit);
        List<String> filesTrackedByNewCommit = getFilesTrackedByNewCommit(newCommit);
        List<String> filesTrackedByBothCommit = getFilesTrackedByBothCommit(newCommit);
        deleteAllFiles(filesTrackedByCurrentCommit);
        overwriteFiles(filesTrackedByBothCommit, newCommit);
        writeFiles(filesTrackedByNewCommit, newCommit);
        clearStage();
    }

    /**
     * Get files that are only tracked in the current commit
     * @param newCommit to check out
     * @return list of files
     */
    private static List<String> getFilesTrackedByCurrentCommit(Commit newCommit) {
        List<String> checkoutCommitFile = newCommit.getFileNameList();
        List<String> currentCommitFile = readCommit().getFileNameList();
        for (String fileName : checkoutCommitFile) {
            if (currentCommitFile.contains(fileName)) {
                currentCommitFile.remove(fileName);
            }
        }
        return currentCommitFile;
    }


    /**
     * Get files that are only tracked in the checked-out commit
     * @param newCommit to check out
     * @return list of files
     */
    private static List<String> getFilesTrackedByNewCommit(Commit newCommit) {
        List<String> checkoutCommitFile = newCommit.getFileNameList();
        List<String> currentCommitFile = readCommit().getFileNameList();
        for (String fileName : currentCommitFile) {
            if (checkoutCommitFile.contains(fileName)) {
                checkoutCommitFile.remove(fileName);
            }
        }
        return checkoutCommitFile;
    }

    /**
     * Any files that are tracked in both current branch and checked-out commit
     * @param newCommit to check out
     * @return list of files
     */
    private static List<String> getFilesTrackedByBothCommit(Commit newCommit) {
        List<String> checkoutCommitFile = newCommit.getFileNameList();
        List<String> currentCommitFile = readCommit().getFileNameList();
        List<String> unionCommit = new ArrayList<String>();
        for (String fileName : currentCommitFile) {
            if (checkoutCommitFile.contains(fileName)) {
                unionCommit.add(fileName);
            }
        }
        return unionCommit;
    }

    /**
     * Delete files tracked by current commit
     * @param filesToDelete files to be deleted
     */
    private static void deleteAllFiles(List<String> filesToDelete) {
        if (filesToDelete.isEmpty()) {
            return;
        }
        for (String fileName : filesToDelete) {
            File file = join(CWD, fileName);
            restrictedDelete(file);
        }
    }

    /**
     * Overwrite all files tracked by both commit and put
     * the files in the CWD
     * @param filesToOverwrite files to be over write
     * @param newCommit to be operated
     */
    private static void overwriteFiles(List<String> filesToOverwrite, Commit newCommit) {
        if (filesToOverwrite.isEmpty()) {
            return;
        }
        for (String fileName : filesToOverwrite) {
            Blob blob = newCommit.getBlobFromFileName(fileName);
            putBlobInCWD(blob);
        }
    }

    /**
     * Write all files tracked only by new commit and put them in CWD
     * check ff a working file is untracked in the current branch and
     * would be overwritten by the checkout before overwriting
     * @param filesToWrite files tracked only by new commit
     * @param newCommit to overwrite
     */
    private static void writeFiles(List<String> filesToWrite, Commit newCommit) {
        if (filesToWrite.isEmpty()) {
            return;
        }
        for (String fileName : filesToWrite) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        overwriteFiles(filesToWrite, newCommit);
    }

    /**
     * Clear stage area
     */
    private static void clearStage() {
        addStage = readAddStage();
        addStage.clear();
        addStage.saveAddStage();
        removeStage = readRemoveStage();
        removeStage.clear();
        removeStage.saveRemoveStage();
    }

    /**
     * Set given branch be the current branch (HEAD).
     * @param branchName given branch name of target branch
     */
    private static void switchToNewBranch(String branchName) {
        writeContents(HEAD_FILE, branchName);
    }

    /**
     * Implement branch command
     *
     * Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (an SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch
     * called “master”.
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
     * Use isDictionary Method in Java File class to check if the branch name file is a
     * dictionary, if not delete this file in the HEAD_DIR
     *
     * @param branchName to be removed
     */
    public static void rmBranch(String branchName) {
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
        Commit newCommit = getCommitFromId(commitId);
        if (newCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit currentCommit = readCommit();
        switchToNewCommit(newCommit);
        File branchFile = join(HEADS_DIR, getCurrBranch());
        writeContents(branchFile, commitId);
    }

    /**
     * Implement merge command, check the exceptions first and perform
     * merge operation by invoking mergeToNewCommit method
     *
     * <p>Note</p>
     * We construct a temp commit using current blob list in order to
     * compare the current blob with given branch commit blob to decide
     * whether write, overwrite or delete
     * We also update the message of the temp commit to indicate that it is
     * a merged commit
     * This method call mergeToNewCommit method and use check*() method to
     * filter some failure cases
     *
     * @param targetBranch to merge
     */
    public static void merge(String targetBranch) {
        checkUncommitedChanges();
        checkTargetBranch(targetBranch);
        checkMergeWithItself(targetBranch);
        String currentBranch = getCurrBranch();
        commit = readCommit();
        Commit mergeCommit = getCommitFromBranchName(targetBranch);
        Commit split = getSplitPoint(commit, mergeCommit);
        checkIfInCurrBranch(split);
        checkIfInGivenBranch(split, targetBranch);

        /* Get construct new merged commit */
        Map<String, String> currentBlobList = commit.getBlobRef();
        String message = "Merged " + targetBranch + " into " + currentBranch + ".";
        String currCommitParent = getCommitFromBranchName(currentBranch).getId();
        String mergeCommitParent = getCommitFromBranchName(targetBranch).getId();
        List<String> parent = new ArrayList<String>(List.of(currCommitParent, mergeCommitParent));
        Commit tmp = new Commit(message, currentBlobList, parent);

        Commit mergedCommit = mergeToNewCommit(split, tmp, mergeCommit);

        saveCommit(mergedCommit);
    }

    /**
     * check if there is any files in add/removal stage
     */
    private static void checkUncommitedChanges() {
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (!(addStage.isEmpty() && removeStage.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private static void checkTargetBranch(String branchName) {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (!branchList.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /**
     * check if attempting to merge a branch with itself
     * @param branchName given branch name
     */
    private static void checkMergeWithItself(String branchName) {
        if (getCurrBranch().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**
     * Get split point(merged commit) of two given commit
     * @param commit1
     * @param commit2
     * @return split point
     */
    private static Commit getSplitPoint(Commit commit1, Commit commit2) {
        Map<String, Integer> commitMap1 = getCommitMap(commit1, 0);
        Map<String, Integer> commitMap2 = getCommitMap(commit2, 0);
        return getSplitPointFromMap(commitMap1, commitMap2);
    }

    /**
     * Find a map of commit of a given commit
     * Traverse all parents of current commit and put their parents recursively
     * @param commit to search
     * @return commit map
     */
    private static Map<String, Integer> getCommitMap(Commit commit, int count) {
        Map<String, Integer> commitMap = new HashMap<String, Integer>();
        if (commit.getParentId().isEmpty()) {
            commitMap.put(commit.getId(), count);
            return commitMap;
        }
        commitMap.put(commit.getId(), count);
        count++;
        for (String id : commit.getParentId()) {
            Commit parent = getCommitFromId(id);
            commitMap.putAll(getCommitMap(parent, count));
        }
        return commitMap;
    }

    /**
     * Find the split point of given two commit
     * Traverse two map and find the split point, which is a commit in both
     * branch and has minimum length when encounter this commit
     * @param commitMap1 commit map 1
     * @param commitMap2 commit map 2
     * @return split point of two map
     */
    private static Commit getSplitPointFromMap(Map<String, Integer> commitMap1,
                                               Map<String, Integer> commitMap2) {
        int length = Integer.MAX_VALUE;
        String unionId = "";
        for (String id : commitMap1.keySet()) {
            if (commitMap2.containsKey(id) && commitMap2.get(id) < length) {
                length = commitMap2.get(id);
                unionId = id;
            }
        }
        return getCommitFromId(unionId);
    }

    /**
     * If the split point is same with the head of given branch
     * Then, the given branch and current branch is the same branch, but
     * the given branch "left behind" by the current branch
     * i.e. HEAD is on the left hand side of the given branch
     * ==============================
     *   * -- * -- * -- * -- ...
     *  HEAD     given
     *           Split
     * ==============================
     *
     * @param split split node of two branch
     * @param newBranch given branch to merge
     */
    private static void checkIfInGivenBranch(Commit split, String newBranch) {
        if (split.getId().equals(getCommitFromBranchName(newBranch).getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
    }

    /**
     * If the split point is same with the HEAD point
     * Then, the current and given branch is the same branch but
     * the current branch is "left behind" by the given branch
     * i.e. the head of given branch is on the left hand side of current branch
     * ==============================
     *   * -- * -- * -- * -- ...
     * given      HEAD
     *           Split
     * ==============================
     *
     * @param split split node of two branch
     */
    private static void checkIfInCurrBranch(Commit split) {
        if (split.getId().equals(readCommit().getId())) {
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    /**
     * Merge Files to the given commit.
     *
     * @param split split point of two branch
     * @param tmpCommit new commit contains blobs of current commit
     * @param mergeCommit commit of the given branch to merge
     *
     * @return commit to be Merged Commit in the branch
     */
    private static Commit mergeToNewCommit(Commit split, Commit tmpCommit, Commit mergeCommit) {
        List<String> allFiles = getAllFiles(split, tmpCommit, mergeCommit);
        List<String> filesToWrite = getWriteFiles(split, tmpCommit, mergeCommit);
        List<String> filesToOverWrite = getOverWriteFiles(split, tmpCommit, mergeCommit);
        List<String> filesToDelete = getDeleteFiles(split, tmpCommit, mergeCommit);
        overwriteFiles(getFileNameFromBlobId(filesToOverWrite), mergeCommit);
        writeFiles(getFileNameFromBlobId(filesToWrite), mergeCommit);
        deleteAllFiles(getFileNameFromBlobId(filesToDelete));
        checkIfConflict(allFiles, split, tmpCommit, mergeCommit);
        return getMergedCommit(tmpCommit, filesToWrite, filesToOverWrite, filesToDelete);
    }

    /**
     * check if the commits are "conflict"
     *
     * <p>Note that</p>
     * Any files modified in different ways in the current and given branches are in conflict.
     * “Modified in different ways” can mean that
     * the contents of both are changed and different from other,
     * or the contents of one are changed and the other file is deleted,
     * or the file was absent at the split point and has different contents in the given and
     * current branches.
     * In this case, replace the contents of the conflicted file with
     *
     * @param allFiles file list
     * @param split split point of two branch
     * @param tmpCommit new commit contains blobs of current commit
     * @param mergeCommit commit of the given branch to merge
     */
    private static void checkIfConflict(List<String> allFiles,
                                        Commit split, Commit tmpCommit, Commit mergeCommit) {
        boolean isConflict = false;
        Map<String, String> splitBlobRef = split.getBlobRef();
        Map<String, String> currentBlobRef = tmpCommit.getBlobRef();
        Map<String, String> mergeBlobRef = mergeCommit.getBlobRef();

        for (String blobId : allFiles) {
            String path = getBlobFromId(blobId).getBlobPath();
            boolean sc = splitBlobRef.containsKey(path);
            boolean cc = currentBlobRef.containsKey(path);
            boolean mc = mergeBlobRef.containsKey(path);
            if ((sc && cc && !splitBlobRef.get(path).equals(currentBlobRef.get(path)))
                    || (sc && mc && !splitBlobRef.get(path).equals(mergeBlobRef.get(path)))
                    || (cc && mc && !currentBlobRef.get(path).equals(mergeBlobRef.get(path)))
                    || (sc && cc && mc
                        && (!splitBlobRef.get(path).equals(currentBlobRef.get(path)))
                        && (!currentBlobRef.get(path).equals(mergeBlobRef.get(path)))
                        && (!splitBlobRef.get(path).equals(mergeBlobRef.get(path))))) {
                isConflict = true;
                String currentContent = "";
                if (currentBlobRef.containsKey(path)) {
                    Blob currentBlob = getBlobFromId(currentBlobRef.get(path));
                    currentContent = new String(currentBlob.getBytes(), StandardCharsets.UTF_8);
                }

                String mergeContent = "";
                if (mergeBlobRef.containsKey(path)) {
                    Blob mergeBlob = getBlobFromId(mergeBlobRef.get(path));
                    mergeContent = new String(mergeBlob.getBytes(), StandardCharsets.UTF_8);
                }

                String contents = "<<<<<<< HEAD\n" + currentContent + "=======\n"
                        + mergeContent + ">>>>>>>\n";
                String fileName = getBlobFromId(blobId).getFileName();
                File conflictFile = join(CWD, fileName);
                writeContents(conflictFile, contents);
            }
        }

        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Get all files from split node, master branch commit and given branch commit
     * Helper Method to classify the files into different kinds of operations
     *
     * <p>Note that</p>
     * Blobs are content addressable, hence we're simply using blob id to keep track
     * Implement a hash set to make sure every file only appears once in list
     *
     * @param split Split point
     * @param tmpCommit tempo commit construct using current blob list
     * @param mergeCommit HEAD of given branch
     * @return List of files
     */
    private static List<String> getAllFiles(Commit split, Commit tmpCommit, Commit mergeCommit) {
        List<String> allFiles = new ArrayList<String>();
        allFiles.addAll(split.getBlobIdList());
        allFiles.addAll(tmpCommit.getBlobIdList());
        allFiles.addAll(mergeCommit.getBlobIdList());
        Set<String> tmp = new HashSet<String>(allFiles);
        allFiles.clear();
        allFiles.addAll(tmp);
        return allFiles;
    }

    /**
     * Get a list of files that only appear in merge commit
     * But do not exist in the current commit and split point
     * These files are supposed to write in the merged commit
     *
     * @param split Split point
     * @param tmpCommit tempo commit construct using current blob list
     * @param mergeCommit HEAD of given branch
     * @return List of files need to write into merged commit
     */
    private static List<String> getWriteFiles(Commit split, Commit tmpCommit, Commit mergeCommit) {
        Map<String, String> splitBlobRef = split.getBlobRef();
        Map<String, String> currentBlobRef = tmpCommit.getBlobRef();
        Map<String, String> mergeBlobRef = mergeCommit.getBlobRef();
        List<String> filesToWrite = new ArrayList<String>();
        for (String path : mergeBlobRef.keySet()) {
            if ((!splitBlobRef.containsKey(path)) && (!currentBlobRef.containsKey(path))) {
                filesToWrite.add(mergeBlobRef.get(path));
            }
        }
        return filesToWrite;
    }

    /**
     * Get a list of files that only appear in merge commit current commit and split point
     * But the version of these files in given branch are different with those in
     * current branch and split point
     * These files are supposed to be overwritten in the merged commit
     *
     * @param split Split point
     * @param tmpCommit tempo commit construct using current blob list
     * @param mergeCommit HEAD of given branch
     * @return List of files need to be overwritten in merged commit
     */
    private static List<String> getOverWriteFiles(Commit split, Commit tmpCommit,
                                                  Commit mergeCommit) {
        Map<String, String> splitBlobRef = split.getBlobRef();
        Map<String, String> currentBlobRef = tmpCommit.getBlobRef();
        Map<String, String> mergeBlobRef = mergeCommit.getBlobRef();
        List<String> filesToOverWrite = new ArrayList<String>();
        for (String path : splitBlobRef.keySet()) {
            if (currentBlobRef.containsKey(path) && mergeBlobRef.containsKey(path)) {
                if (splitBlobRef.get(path).equals(currentBlobRef.get(path))
                        && !splitBlobRef.get(path).equals(mergeBlobRef.get(path))) {
                    filesToOverWrite.add(mergeBlobRef.get(path));
                }
            }
        }
        return filesToOverWrite;
    }

    /**
     * Get a list of files that only appear in current commit and split point
     * But the version of these files do not exist in given branch
     * These files are supposed to be deleted in the merged commit
     *
     * @param split Split point
     * @param tmpCommit tempo commit construct using current blob list
     * @param mergeCommit HEAD of given branch
     * @return List of files need to be deleted in merged commit
     */
    private static List<String> getDeleteFiles(Commit split, Commit tmpCommit,
                                               Commit mergeCommit) {
        Map<String, String> splitBlobRef = split.getBlobRef();
        Map<String, String> currentBlobRef = tmpCommit.getBlobRef();
        Map<String, String> mergeBlobRef = mergeCommit.getBlobRef();
        List<String> filesToDelete = new ArrayList<String>();
        for (String path : splitBlobRef.keySet()) {
            if (currentBlobRef.containsKey(path) && !mergeBlobRef.containsKey(path)) {
                filesToDelete.add(currentBlobRef.get(path));
            }
        }
        return filesToDelete;
    }

    /**
     * Get a list of files bond with co-respondent blob
     *
     * @param blobIdList given blob id list
     * @return a list of files
     */
    private static List<String> getFileNameFromBlobId(List<String> blobIdList) {
        List<String> fileName = new ArrayList<String>();
        for (String id : blobIdList) {
            Blob blob = getBlobFromId(id);
            fileName.add(blob.getFileName());
        }
        return fileName;
    }

    /**
     * Construct final merged commit after updating its blobs
     * @param mergedCommit temp commit construct before
     * @param writeFiles list of files to be written in the temp blob list
     * @param overwriteFiles list of files to be overwritten in the temp blob list
     * @param deleteFiles list of files to be deleted in the temp blob list
     * @return merged commit
     */
    private static Commit getMergedCommit(Commit mergedCommit,
                                          List<String> writeFiles,
                                          List<String> overwriteFiles,
                                          List<String> deleteFiles) {
        Map<String, String> mergedFiles = mergedCommit.getBlobRef();

        if (!overwriteFiles.isEmpty()) {
            for (String id : overwriteFiles) {
                mergedFiles.put(getBlobFromId(id).getBlobPath(), id);
            }
        }

        if (!writeFiles.isEmpty()) {
            for (String id : writeFiles) {
                mergedFiles.put(getBlobFromId(id).getBlobPath(), id);
            }
        }

        if (!deleteFiles.isEmpty()) {
            for (String id : overwriteFiles) {
                mergedFiles.remove(getBlobFromId(id).getBlobPath());
            }
        }

        return new Commit(mergedCommit.getMessage(), mergedFiles, mergedCommit.getParentId());
    }

}
