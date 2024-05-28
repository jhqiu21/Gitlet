package gitlet;

// TODO: any imports you need here

// import org.graalvm.compiler.core.common.util.Util;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.join;
import static gitlet.Utils.writeObject;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author QIU JINHANG
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String id;
    private Date currentTime;
    private String timestamp;
    private List<String> parent;
    private HashMap<String, String> blobRef;
    private File commitSave;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, HashMap<String,String> blobRef, List<String> parent) {
        this.currentTime = new Date();
        this.message = message;
        this.blobRef = blobRef;
        this.parent = parent;
        this.timestamp = dateToString(this.currentTime);
        this.id = this.getId();
        this.commitSave = generateSaveFile();
    }

    public Commit() {
        this.currentTime = new Date(0);
        this.parent = new ArrayList<String>();
        this.blobRef = new HashMap<String, String>();
        this.timestamp = dateToString(this.currentTime);
        this.message = "init commit";
        this.id = this.getId();
        this.commitSave = generateSaveFile();
    }

    /**
     * Generate date of String Type using DateFormat Class
     * @return date
     */
    private String dateToString(Date currentTime) {
        DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return df.format(this.currentTime);
    }

    /**
     * Each commit is identified by its SHA-1 id, which must include the file (blob)
     * references of its files, parent reference, log message, and commit time.
     * @return commit id
     */
    public String getId() {
        return Utils.sha1(this.blobRef, this.parent, this.message, this.timestamp);
    }

    /**
     * TODO: Make up the javadoc
     * @return TODO:
     */
    private File generateSaveFile() {
        return join(OBJECT_DIR, id);
    }

    public void save() {
        writeObject(commitSave, this);
    }

    /**
     * @return Blob Reference implemented using a hash map
     */
    public HashMap<String, String> getBlobRef() {
        return blobRef;
    }
}
