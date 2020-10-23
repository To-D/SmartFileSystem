package Block;

import Exceptions.ErrorCode;
import interfaces.BlockManager;

import java.io.File;
import java.io.IOException;

public class BlockManagerSet {
    private static BlockManagerSet instance = new BlockManagerSet();
    public static BlockManagerSet getInstance(){return instance;}

    private final int MANAGE_SIZE = 20;
    private final BlockManager[] blockManagers = new MyBlockManager[MANAGE_SIZE];
    private final String path = "C:\\Users\\12444\\Desktop\\CSE LAB1\\SmartFileSystem\\BM\\";

    private BlockManagerSet(){
        for (int i = 1; i <= MANAGE_SIZE; i++) {
            String BMDir = path + "BM-" + i + "\\";
            String metaDir = BMDir + "meta";
            String dataDir = BMDir + "data";
            File meta = new File(metaDir);
            if (!meta.exists()) {
                if (!meta.mkdirs()) {
                    throw new ErrorCode(ErrorCode.CREATE_DIRECTORY_OR_FILE_FAILED);
                }
            } else { // meta存在但是是文件
                if (meta.isFile()) {
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }
            }

            File data = new File(dataDir);
            if (!data.exists()) {
                if (!data.mkdirs()) {
                    throw new ErrorCode(ErrorCode.CREATE_DIRECTORY_OR_FILE_FAILED);
                }
            } else {
                if (data.isFile()) {
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }
            }

            MyBlockManager blockManager = null;
            try {
                blockManager = new MyBlockManager(BMDir,i);
            } catch (IOException ioException) {
                throw new ErrorCode(ErrorCode.IO_EXCEPTION);
            }
            blockManagers[i-1] = blockManager;
        }
    }


    public BlockManager getBlockManager(int id){
        return this.blockManagers[id - 1];
    }

    public BlockManager getRandomBlockManager(){
        int freeNum = 20;
        for (BlockManager blockManager : this.blockManagers) {
            if (((MyBlockManager) blockManager).isFull()) {
                freeNum--;
            }
        }
        if(freeNum == 0){
            throw new ErrorCode(ErrorCode.NO_MORE_SPACE);
        }

        int index = (int)(Math.random()*MANAGE_SIZE);
        while( ((MyBlockManager) blockManagers[index]).isFull() ){
            index = (int)(Math.random()*MANAGE_SIZE);
        }
        return blockManagers[index];
    }
}
