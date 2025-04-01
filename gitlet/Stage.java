package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    /**
     * Implement a HashMap to store blob reference
     * Key: path, Value: blobId
     */
    private Map<String, String> blobRef = new HashMap<String, String>();

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

    /**
     * clear blob in stage
     */
    public void clear() {
        blobRef.clear();
    }

    /**
     * Get a List of Blob
     */
    public List<Blob> getBlobList() {
        Blob blob;
        List<Blob> blobList = new ArrayList<Blob>();
        for (String id : blobRef.values()) {
            blob = getBlobByID(id);
            blobList.add(blob);
        }
        return blobList;
    }

    /**
     * Get target blob through target id
     * @param id of target blob
     * @return target blob
     */
    public static Blob getBlobByID(String id) {
        File blobFile = join(OBJECT_DIR, id);
        return readObject(blobFile, Blob.class);
    }

    /**
     * Get a Map of Blob Reference
     * @return blob reference implemented by hash map
     */
    public Map<String, String> getBlobMap() {
        return this.blobRef;
    }

    /**
     * Determine whether Stage contains target blob
     * @param fileName of target file
     * @return boolean value
     */
    public boolean contains(String fileName) {
        return getBlobMap().containsKey(fileName);
    }

    /**
     * If stage exists, determine whether the stage is empty
     * @return boolean value
     */
    public boolean isEmpty() {
        return this.blobRef.isEmpty();
    }



}
