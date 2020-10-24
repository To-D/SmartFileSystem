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

    private BlockManagerSet(){
        for (int i = 1; i <= MANAGE_SIZE; i++) {
            // 创建实体文件夹
            String path = "C:\\Users\\12444\\Desktop\\CSE LAB1\\SmartFileSystem\\BM\\";
            String BMDir = path + "BM-" + i + "\\";
            File bm = new File(BMDir);
            if (!bm.exists()) {
                if (!bm.mkdirs()) {
                    throw new ErrorCode(ErrorCode.CREATE_DIRECTORY_OR_FILE_FAILED);
                }
            } else { // meta存在但是是文件
                if (bm.isFile()) {
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }
            }

            // 创建内存对象
            MyBlockManager blockManager = new MyBlockManager(BMDir,i);
            blockManagers[i-1] = blockManager;
        }
    }

    /**
     * 返回指定的file manager
     */
    public BlockManager getBlockManager(int id){
        if(id <= 0 || id > MANAGE_SIZE){
            throw new ErrorCode(ErrorCode.INVALID_ID,Integer.toString(id));
        }
        return this.blockManagers[id - 1];
    }

    public int getSize(){
        return MANAGE_SIZE;
    }

    /**
     * 返回一个随机的file manager
     */
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
