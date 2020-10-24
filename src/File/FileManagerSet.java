package File;

import Block.BlockManagerSet;
import Block.MyBlockManager;
import Exceptions.ErrorCode;
import interfaces.BlockManager;
import interfaces.FileManager;

import java.io.File;

public class FileManagerSet {
    private static FileManagerSet instance = new FileManagerSet();
    public static FileManagerSet getInstance(){return instance;}

    private final int FILE_MANAGER_NUM = 5;
    private final String path = "C:\\Users\\12444\\Desktop\\CSE LAB1\\SmartFileSystem\\FM\\";
    private FileManager[] fileManagers = new FileManager[FILE_MANAGER_NUM];

    private FileManagerSet() {
        for (int i = 1; i <= FILE_MANAGER_NUM; i++) {
            String FMDir = path + "FM-" + i + "\\";
            File fm = new File(FMDir);
            if (!fm.exists())
                if (!fm.mkdirs()){
                    throw new ErrorCode(ErrorCode.CREATE_DIRECTORY_OR_FILE_FAILED);
                }
            FileManager fileManager = new MyFileManager(FMDir);
            fileManagers[i-1] = fileManager;
        }
    }

    public FileManager getRandomFileManager(){
        int index = (int)(Math.random()*FILE_MANAGER_NUM);
        return fileManagers[index];
    }

    public FileManager getFileManager(int id){
        return fileManagers[id - 1];
    }

    public int getSize(){return FILE_MANAGER_NUM;}
}
