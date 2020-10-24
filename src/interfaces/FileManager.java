package interfaces;

import Exceptions.ErrorCode;

import java.io.FileNotFoundException;

public interface FileManager {

    File getFile(Id fileId);
    File newFile(Id fileId);
}
