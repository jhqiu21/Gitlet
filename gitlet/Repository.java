package gitlet;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *
 *  Structure
 *
 *
 * .gitlet
 *    |-- objects
 *    |      |-- commit/blob
 *    |-- refs
 *    |    |-- heads
 *    |         |-- master
 *    |         |-- branch #
 *    |-- HEAD
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
    /* TODO: fill in the rest of this class. */

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

    private static void initCommit() {
        /**
         * init and save a commit in the data base
         * */
        Commit initcommit = new Commit();
        commit = initcommit;
        initcommit.save();
    }

    private static void initHead() {
        /**
         * initialize a head pointer
         * */
        writeObject(HEADS_DIR, "master");
        writeContents(HEAD_FILE, commit.getId());
    }

    private static void initHeadPointer() {
        /**
         * initialize a head pointer to the master branch
         * */
        writeObject(HEAD_FILE, "master");
    }

    public static void add(String file) {
        File fileToAdd = getFile(file);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(fileToAdd);
        storeBlob(blob);
    }

    public static File getFile(String file) {
        return Paths.get(file).isAbsolute()
                ? new File(file)
                : join(CWD, file);
    }

    public static void storeBlob(Blob blob) {

    }

}
