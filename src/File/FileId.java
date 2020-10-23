package File;

import Exceptions.ErrorCode;

public class FileId implements interfaces.Id {
    private static int nextFileId = 1;
    private int id;

    public FileId() {
        this.id = nextFileId;
        nextFileId++;
    }

    public FileId(int id) {
        if(id <0){
            throw new ErrorCode(ErrorCode.INVALID_ID);
        }
        this.id = id;
        if(this.id >= nextFileId){
            nextFileId = this.id + 1;
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
