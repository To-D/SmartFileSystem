package Utils;

import Exceptions.ErrorCode;
import File.*;
import interfaces.Block;
import interfaces.File;
import interfaces.FileManager;
import java.math.BigInteger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Util {
    /**
     * Read all contents in a file and print out.
     * @param fileName - Name of the file to read.
     * @param fileManager - The file manager that manager the file
     * @throws ErrorCode - IOException occurs when reading.
     */
    public static void smart_cat(String fileName, FileManager fileManager) throws ErrorCode {
        // get file
        File file = null;
        try {
            file = getFile(fileName, fileManager);
        }catch (ErrorCode fileNotFound){
            System.out.println(fileNotFound.getMessage());
        }
        // get content
        if(file != null){
            byte[] content = new byte[0];
            try {
                content = file.read((int) (file.size()-file.pos()));//从cursor位置读到最后
            } catch (ErrorCode errorCode) {
                System.out.println(errorCode.getMessage());
            }
            for (byte b : content) {
                System.out.print((char) b);
            }
            System.out.println();
        }
    }

    public static void smart_hex(Block block){
        byte[] content = block.read();
        String hex = new BigInteger(1, content).toString(16);
        System.out.println(hex);
    }

    // write进去的具体内容通过console输入，应该实现插入而不是覆盖！

    /**
     * @throws ErrorCode - FILE_NOT_FOUND/
     */
    public static void smart_write(String fileName, int index,FileManager fileManager){
        MyFile file = (MyFile)getFile(fileName, fileManager); // FILE_NOT_FOUND
        if(index > file.size()){
            file.setSize(index);// 如果在size之外的地方写了，默认中间插入0
        }

        if(file.getCursor() != index){
            file.move(index,File.MOVE_HEAD); // CURSOR_OUT_OF_RANGE
        }

        System.out.print("Please input:");
        Scanner in = new Scanner(System.in);
        byte[] content = in.nextLine().getBytes();

        try {
            file.write(content);
        } catch (ErrorCode errorCode) {
            file.move(index,File.MOVE_HEAD);// 恢复cursor
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
//        file.move(index+ content.length,File.MOVE_HEAD); // 消除因为block的不可修改导致的，即使微小的修改，指针也始终在末尾的影响
    }

    public static void smart_copy(String from, String to, FileManager fileManagerFrom, FileManager fileManagerTo){
        File fromFile = fromFile = getFile(from, fileManagerFrom); // file_not_found
        File toFile = toFile = getFile(to, fileManagerTo); // file_not_found
        byte[] content = fromFile.read((int) fromFile.size());  // 报各种错
        toFile.write(content);
    }

    private static File getFile(String fileName, FileManager fileManager) {
        FileId fileId = new FileId(fileName);
        return fileManager.getFile(fileId);
    }
}
