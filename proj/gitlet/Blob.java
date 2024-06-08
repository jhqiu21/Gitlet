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
        this.blobPath = src.getPath();
        this.id = generateBlobId();
        this.blobSaveFileName = join(OBJECT_DIR, id);
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

    public String getFileName() {
        return src.getName();
    }

    /**
     * Saves the content of the blob to the designated file.
     * It ensures that any changes made to the blob are persisted and can be retrieved later.
     */
    public void save() {
        writeObject(blobSaveFileName, this);
    }

    private String generateBlobId() {
        return sha1(blobPath, bytes);
    }

}


