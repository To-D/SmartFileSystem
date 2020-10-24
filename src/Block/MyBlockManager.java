package Block;
import Exceptions.ErrorCode;
import interfaces.Block;
import interfaces.Id;
import java.io.*;

/**
 * 主要负责
 * 1. block实体文件的创建和删除
 * 2. block对象的创建、删除和索引
 */

public class MyBlockManager implements interfaces.BlockManager {
    private final int id;
    private final String path;
    private static final int MANAGER_SIZE = 20;
    private final Block[] blocks = new Block[MANAGER_SIZE];
    private int firstFreeBlockIndex = 0;

    public MyBlockManager(String bmPath,int id) {
        this.path = bmPath;
        this.id = id;

        // 创建meta和data文件夹
        String metaDir = bmPath + "meta";
        String dataDir = bmPath + "data";
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

        // 扫描data和meta文件
        for(int i = 1; i <= MANAGER_SIZE; i++){
            String metaPath = bmPath + "meta\\b" + i + ".meta";
            String dataPath = bmPath + "data\\b" + i + ".data";
            File metaFile = new File(metaPath);
            File dataFile = new File(dataPath);

            // data和meta文件应该成对出现，不然判定为文件缺失
            if(!metaFile.exists() && dataFile.exists()){
                if(dataFile.isDirectory()){
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                } else {
                    throw new ErrorCode(ErrorCode.META_FILE_LOST);
                }
            }

            if(metaFile.exists() && !dataFile.exists()){
                if(metaFile.isDirectory()){
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }else{
                    throw new ErrorCode(ErrorCode.DATA_FILE_LOST);
                }
            }

            // 持久化
            if(metaFile.exists() && dataFile.exists()){
                if(metaFile.isDirectory() || dataFile.isDirectory()){ // 存在但不是文件
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }else{ // 存在且是文件,读meta信息
                    BlockId blockId = new BlockId(i);
                    Block newBlock = new MyBlock(this, blockId);
                    this.blocks[i-1] = newBlock;
                    findFirstFreeBlockIndex();
                }
            }
        }
    }

    @Override
    public Block getBlock(Id indexId) {
        int index = ((BlockId) indexId).getId() - 1;
        if(index < 0 || index >=MANAGER_SIZE){
            throw new ErrorCode(ErrorCode.INVALID_ID,Integer.toString(index+1));
        }
        return this.blocks[index];
    }

    @Override
    public Block newBlock(byte[] b) {
        // 创建新文件
        BlockId blockId = new BlockId(this.firstFreeBlockIndex + 1);
        String dataPath = this.path + "data\\b" + blockId.getId() + ".data";
        String metaPath = this.path + "meta\\b" + blockId.getId() + ".meta";
        File data = null;
        File meta = null;

        // 创建文件对象
        try {
            data = createFile(dataPath);
            meta = createFile(metaPath);
            Block block = new MyBlock(this, blockId, b);
            // 异常一定是在上面出现，所以不会加入blocks
            this.blocks[blockId.getId()-1] = block;
            findFirstFreeBlockIndex();
            return block;
        }catch (ErrorCode createBlockFailed){
            // 创建失败，把创建的文件删除，并将异常上抛
            if(data != null)
                data.delete();
            if(meta != null)
                meta.delete();
            throw createBlockFailed;
        }
    }

    private File createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
        } else {
            try{
                file.createNewFile();
                return file;
            } catch (IOException ioException) {
                throw new ErrorCode(ErrorCode.IO_EXCEPTION);
            }
        }
    }

    public boolean isFull(){
        return this.firstFreeBlockIndex == -1;
    }

    public void removeBlock(Id blockId){
        // 删除内存对象
        int blockIdNum  =((BlockId) blockId).getId();
        int blockIndex = blockIdNum - 1;
        this.blocks[blockIndex] = null;

        // 删除实体文件
        String metaPath = this.path +"meta\\b"+blockIdNum+".meta";
        String dataPath = this.path +"data\\b"+blockIdNum+".data";
        File meta = new File(metaPath);
        File data = new File(dataPath);
        if(meta.exists()){
            meta.delete();
        }
        if(data.exists()){
            data.delete();
        }
    }

    public int getId(){
        return this.id;
    }

    public String getPath(){
        return this.path;
    }

    private void findFirstFreeBlockIndex(){
        this.firstFreeBlockIndex = -1;
        for(int i = 0; i < MANAGER_SIZE; i++){
            if(blocks[i] == null){
                this.firstFreeBlockIndex = i;
            }
        }
    }

}
