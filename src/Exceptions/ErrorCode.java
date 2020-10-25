package Exceptions;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException{
    //System error
    public static final int IO_EXCEPTION = 0;
    public static final int FILE_NOT_FOUND = 1;
    public static final int FILE_NAME_OCCUPIED= 2;
    public static final int CREATE_DIRECTORY_OR_FILE_FAILED = 4;
    public static final int DATA_FILE_LOST = 5;
    public static final int META_FILE_LOST = 6;
    public static final int INITIAL_FILE_FAILED = 7;
    public static final int BLOCK_BROKEN = 8;
    public static final int NO_MORE_SPACE = 9;


    // Console error
    public static final int WRONG_INSTRUCTION = 3;
    public static final int WRONG_FILE_MANAGER_NAME = 11;
    public static final int PASSIVE_SIZE = 12;
    public static final int INVALID_ID = 13;
    public static final int DUPLICATED_ID = 14;
    public static final int FILE_ALREADY_USED = 15;
    public static final int CURSOR_OUT_OF_RANGE = 16;

    //... and more
    public static final int UNKNOWN = 1000;

    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();

    static {
        ErrorCodeMap.put(IO_EXCEPTION, "An IO_EXCEPTION happened");
        ErrorCodeMap.put(FILE_NOT_FOUND, "Cannot find the file");
        ErrorCodeMap.put(INITIAL_FILE_FAILED, "Fail to initial the file. It may caused by the broken mata-data");
        ErrorCodeMap.put(WRONG_INSTRUCTION, "You input a wrong instruction");
        ErrorCodeMap.put(WRONG_FILE_MANAGER_NAME, "You input a wrong fileManager name");
        ErrorCodeMap.put(FILE_ALREADY_USED, "The file you want to copy into has been used");


        ErrorCodeMap.put(UNKNOWN, "unknown");
        ErrorCodeMap.put(CREATE_DIRECTORY_OR_FILE_FAILED, "fail to create new directories");
        ErrorCodeMap.put(INVALID_ID, "You use an invalid id which is out of range");
        ErrorCodeMap.put(DUPLICATED_ID, "Duplicated file name is used");
        ErrorCodeMap.put(FILE_NAME_OCCUPIED, "file name for system use is occupied");

        ErrorCodeMap.put(DATA_FILE_LOST, "one of the .data files is lost");
        ErrorCodeMap.put(META_FILE_LOST, "one of the .meta files is lost");
        ErrorCodeMap.put(CURSOR_OUT_OF_RANGE, "the cursor is out of range");
        ErrorCodeMap.put(PASSIVE_SIZE, "new size is passive");
        ErrorCodeMap.put(BLOCK_BROKEN, "Your data has been broken");
        ErrorCodeMap.put(NO_MORE_SPACE, "no more blocks can be used");
    }

    public static String getErrorText(int errorCode){
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }

    private int errorCode;

    public ErrorCode(int errorCode){
        super(String.format("[ERROR]: \"%s\"", getErrorText(errorCode)));
        this.errorCode = errorCode;
    }

    public ErrorCode(int errorCode,String more){
        super(String.format("[ERROR]: \"%s\" : %s", getErrorText(errorCode), more));
        this.errorCode = errorCode;
    }


    public int getErrorCode(){
        return errorCode;
    }

}


