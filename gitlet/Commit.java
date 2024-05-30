package gitlet;

// TODO: any imports you need here

// import org.graalvm.compiler.core.common.util.Util;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author QIU JINHANG
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    private String message;
    private String id;
    private Date currentTime;
    private String timestamp;
    private List<String> parent;
    private Map<String, String> blobRef;
    private File commitSave;

    public Commit(String message, Map<String,String> blobRef, List<String> parent) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
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
     * Generates a File object for saving, based on the file name.
     *
     * <p>This method creates a File object by joining the OBJECT_DIR with the
     * provided file id</p>
     *
     * @return a File object representing the save file
     */
    private File generateSaveFile() {
        return join(OBJECT_DIR, id);
    }

    /**
     * Saves the current state of the object by writing it to a file.
     *
     * <p>Note: The current state of the object must be properly initialized and
     * set before calling this method.</p>
     */
    public void save() {
        writeObject(commitSave, this);
    }

    /**
     * Get Blob reference implemented by HashMap
     * @return Blob Reference implemented using a hash map
     */
    public Map<String, String> getBlobRef() {
        return blobRef;
    }

    /**
     * Get a List of blob of current commit
     * @param 
     * */

    /**
     * Find whether the commit contains file with target file path
     * @param filePath of target file
     * @return boolean value
     */
    public boolean contains(String filePath) {
        return this.blobRef.containsKey(filePath);
    }

    public List<String> getParentId() {
        return this.parent;
    }

}
