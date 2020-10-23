package Exceptions;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException{
    //System error
    public static final int IO_EXCEPTION = 1;
    public static final int CREATE_DIRECTORY_OR_FILE_FAILED = 3;
    public static final int FILE_NAME_OCCUPIED= 7;

    // relate to BLOCK
    public static final int CHECKSUM_CHECK_FAILED = 2;
    public static final int BYTE_ARRAY_TOO_LARGE = 8;
    public static final int DATA_FILE_LOST = 9;
    public static final int META_FILE_LOST = 10;
    public static final int CURSOR_OUT_OF_RANGE = 11;
    public static final int PASSIVE_SIZE = 12;
    public static final int BLOCK_BROKEN = 13;
    public static final int NO_MORE_SPACE = 14;
    
    // relate to FILE
    public static final int FILE_NOT_FOUND = 4;
    


    // relate to ID
    public static final int INVALID_ID = 5;
    public static final int DUPLICATED_ID = 6;

    //... and more
    public static final int UNKNOWN = 1000;

    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();

    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO exception");
        ErrorCodeMap.put(CHECKSUM_CHECK_FAILED, "block checksum check failed");
        ErrorCodeMap.put(UNKNOWN, "unknown");
        ErrorCodeMap.put(CREATE_DIRECTORY_OR_FILE_FAILED, "fail to create new directories");
        ErrorCodeMap.put(FILE_NOT_FOUND, "fail to find this file");
        ErrorCodeMap.put(INVALID_ID, "invalid id");
        ErrorCodeMap.put(DUPLICATED_ID, "duplicated id is used");
        ErrorCodeMap.put(FILE_NAME_OCCUPIED, "file name for system use is occupied");
        ErrorCodeMap.put(BYTE_ARRAY_TOO_LARGE, "byte array ready to written in block is too large");
        ErrorCodeMap.put(DATA_FILE_LOST, "one of the .data files is lost");
        ErrorCodeMap.put(META_FILE_LOST, "one of the .meta files is lost");
        ErrorCodeMap.put(CURSOR_OUT_OF_RANGE, "the cursor is out of range");
        ErrorCodeMap.put(PASSIVE_SIZE, "new size is passive");
        ErrorCodeMap.put(BLOCK_BROKEN, "one of the blocks you access has been broken");
        ErrorCodeMap.put(NO_MORE_SPACE, "no more blocks can be used");
    }

    public static String getErrorText(int errorCode){
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }

    private int errorCode;

    public ErrorCode(int errorCode){
        super(String.format("error code '%d' \"%s\"", errorCode, getErrorText(errorCode)));
        this.errorCode = errorCode;
    }

    public int getErrorCode(){
        return errorCode;
    }

}


