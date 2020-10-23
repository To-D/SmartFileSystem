package Block;

import Exceptions.ErrorCode;
import Utils.MD5Util;
import interfaces.Block;
import interfaces.Id;
import java.io.*;

public class MyBlockManager implements interfaces.BlockManager {
    private static final int MANAGER_SIZE = 20;
    private static final int BLOCK_SIZE = 512;
    private int firstFreeBlockIndex = 0;
    private final Block[] blocks = new Block[MANAGER_SIZE];

    private final String path;
    private final int id;

    public MyBlockManager(String bmPath,int id) throws IOException {
        this.path = bmPath;
        this.id = id;
        for(int i = 1; i <= MANAGER_SIZE; i++){
            String metaPath = bmPath + "meta\\b" + i + ".meta";
            String dataPath = bmPath + "data\\b" + i + ".data";
            File metaFile = new File(metaPath);
            File dataFile = new File(dataPath);

            // 持久化
            if(metaFile.exists() && dataFile.exists()){
                if(metaFile.isDirectory() || dataFile.isDirectory()){ // 存在但不是文件
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }else{ // 存在且是文件,读meta信息
                    BlockId blockId = new BlockId(i);
                    Block newBlock = new MyBlock(this.path,this,blockId);
                    this.blocks[i-1] = newBlock;
                }
            }

            // data和meta文件应该成对出现，不然判定为文件缺失
            if(!metaFile.exists() && dataFile.exists()){
                if(dataFile.isDirectory()){
                    throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
                }else{
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
        }
        findFirstFreeBlockIndex();
    }

    @Override
    public Block getBlock(Id indexId) {
        int index = ((BlockId) indexId).getId() - 1;
        return this.blocks[index];
    }

    @Override
    public Block newBlock(byte[] b) throws IOException {
        // 1. 对数组长度作正确性检查
        if(b.length == 0){
            return newEmptyBlock(0);
        }
        if(b.length > BLOCK_SIZE){
            throw new ErrorCode(ErrorCode.BYTE_ARRAY_TOO_LARGE);
        }

        // 2. 0< b.length <=BLOCK_SIZE,写入数据
        // 创建文件
        BlockId blockId = new BlockId(this.firstFreeBlockIndex + 1);
        String dataPath = this.path + "data\\b" + blockId.getId() + ".data";
        String metaPath = this.path + "meta\\b" + blockId.getId() + ".meta";
        createFile(dataPath);
        createFile(metaPath);

        // 写入data
        try (OutputStream dataOS = new FileOutputStream(dataPath)) {
            dataOS.write(b);
        } catch (FileNotFoundException e) {
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        // 写入meta
        String checksum = MD5Util.byteEncode(b);
        Writer writer = null;
        BufferedWriter bw = null;
        try{
            writer = new FileWriter(metaPath);
            bw = new BufferedWriter(writer);
            bw.write(Integer.toString(b.length));
            bw.newLine();
            bw.write(checksum);
            bw.newLine();
            bw.flush();
        }finally{
            if(writer != null)
                writer.close();
            if(bw!=null)
                bw.close();
        }

        // 3. 创建对应的block对象并归入blockManager的管理
        Block block = new MyBlock(this.path, this, blockId, b.length,checksum);
        this.blocks[blockId.getId()-1] = block;

        // 4. 更新firstFreeBlockIndex
        findFirstFreeBlockIndex();
        return block;
    }

    private void createFile(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
        } else {
            if (!file.createNewFile()) {
                throw new ErrorCode(ErrorCode.CREATE_DIRECTORY_OR_FILE_FAILED);
            }
        }
    }

    public boolean isFull(){
        return this.firstFreeBlockIndex == -1;
    }

    public void removeBlock(Id blockId){
        int blockIdNum  =((BlockId) blockId).getId();
        int blockIndex = blockIdNum - 1;
        this.blocks[blockIndex] = null;

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

    private void findFirstFreeBlockIndex(){
        this.firstFreeBlockIndex = -1;
        for(int i = 0; i < MANAGER_SIZE; i++){
            if(blocks[i] == null){
                this.firstFreeBlockIndex = i;
            }
        }
    }

}
