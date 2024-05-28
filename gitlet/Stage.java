package gitlet;

import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.writeObject;

public class Stage implements Serializable {
    /**
     * Implement a HashMap to store blob reference
     * Key: path, Value: blobId
     * */
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
     * Save Add Stage
     * TODO: Make up the javadoc of the method
     */
    public void saveAddStage() {
        writeObject(Repository.ADDSTAGE_FILE, this);
    }

    /**
     * Save Remove Stage
     * TODO: Make up the javadoc of the method
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
