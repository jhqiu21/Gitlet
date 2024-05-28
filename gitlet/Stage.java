package gitlet;

import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.writeObject;

public class Stage implements Serializable {
    /**
     * Implement a HashMap to store blob reference
     * Key: path, Value: blobId
     */
    private HashMap<String, String> blobRef = new HashMap<String, String>();

    /**
     * Determine whether the stage have specific blob
     * @param blob Target blob
     * @return boolean
     */
    public boolean containsBlob(Blob blob) {
        return blobRef.containsValue(blob.getId());
    }

    /**
     * Determine whether the stage have blob with specific path
     * @param path Target path
     * @return boolean
     */
    public boolean containsFilePath(String path) {
        return blobRef.containsKey(path);
    }

    /**
     * Saves the current state of the Add Stage to the designated file.
     * This method serializes the current Add Stage object and writes it to the file
     * specified by {@code Repository.ADDSTAGE_FILE}. It ensures that any changes made
     * to the Add Stage are persisted and can be retrieved later.
     */
    public void saveAddStage() {
        writeObject(Repository.ADDSTAGE_FILE, this);
    }

    /**
     * Saves the current state of the Remove Stage to the designated file.
     * This method serializes the current Remove Stage object and writes it to the file
     * specified by {@code Repository.REMOVESTAGE_FILE}. It ensures that any changes made
     * to the Remove Stage are persisted and can be retrieved later.
     */
    public void saveRemoveStage() {
        writeObject(Repository.REMOVESTAGE_FILE, this);
    }

    /**
     * Remove target blob from the hash map
     * @param blob Target blob to remove
     */
    public void delete(Blob blob) {
        blobRef.remove(blob.getBlobPath());
    }

    /**
     * Remove blob with target path from the hash map
     * @param path Target path of the blob to remove
     */
    public void delete(String path) {
        blobRef.remove(path);
    }

    /**
     * Add blob to the Stage
     * @param blob Blob to add
     */
    public void add(Blob blob) {
        this.blobRef.put(blob.getBlobPath(), blob.getId());
    }


}
