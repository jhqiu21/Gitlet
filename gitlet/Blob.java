package gitlet;

import java.io.Serializable;
import java.io.File;

/**
 * General Class implement a Blob
 * @author QIU JINHANG
 */
public class Blob implements Serializable {
    private byte[] bytes;
    private String id;
    private String fileRef;
    private File src;
    private File blobSaveFileName;

    public Blob(File blobSaveFileName) {
        this.blobSaveFileName = blobSaveFileName;
    }



}


