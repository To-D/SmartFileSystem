package File;

import Exceptions.ErrorCode;

public class FileId implements interfaces.Id {
    private String id;

//    public FileId() {
//        this.id = nextFileId;
//        nextFileId++;
//    }

    public FileId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
