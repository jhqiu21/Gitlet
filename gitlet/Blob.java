package gitlet;

import java.io.Serializable;
import java.io.File;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

/**
 * General Class implement a Blob
 * @author QIU JINHANG
 */
public class Blob implements Serializable {
    private byte[] bytes;
    private String id;
    private String blobPath;
    private File src;
    private File blobSaveFileName;

    public Blob(File src) {
        this.src = src;
        this.bytes = readContents(src);
        this.id = sha1(blobPath, bytes);
        this.blobSaveFileName = join(OBJECT_DIR, id);
        this.blobPath = src.getPath();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getBlobPath() {
        return blobPath;
    }

    public String getId() {
        return id;
    }

    public File getBlobSaveFileName() {
        return blobSaveFileName;
    }

    public String getFileName() {
        return this.blobSaveFileName.getName();
    }

    /**
     * Saves the content of the blob to the designated file.
     * It ensures that any changes made to the blob are persisted and can be retrieved later.
     */
    public void save() {
        writeObject(blobSaveFileName, this);
    }


}


