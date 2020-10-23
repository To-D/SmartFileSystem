package Block;

import Exceptions.ErrorCode;
import interfaces.BlockManager;
import interfaces.Id;

import java.io.*;

public class MyBlock implements interfaces.Block {
    private static final int CAPABILITY = 512; // bytes
    private final String metaPath;
    private final String dataPath;
    private Id indexId;
    private BlockManager blockManager;
    // block meta
    private int size;
    private String checksum;

    public MyBlock(String bmPath, BlockManager blockManager, Id indexId,int size,String checksum) {
        this.metaPath = bmPath +"meta\\b" + ((BlockId) indexId).getId() + ".meta";
        this.dataPath = bmPath + "data\\b" + ((BlockId) indexId).getId() + ".data";
        this.blockManager = blockManager;
        this.indexId = indexId;
        this.size = size; // size从.meta中获取
        this.checksum = checksum;
    }

    public MyBlock(String bmPath, BlockManager blockManager,Id indexId) throws IOException {
        this.metaPath = bmPath +"meta\\b" + ((BlockId) indexId).getId() + ".meta";
        this.dataPath = bmPath + "data\\b" + ((BlockId) indexId).getId() + ".data";
        this.blockManager = blockManager;
        this.indexId = indexId;
        readMeta();
    }

    @Override
    public Id getIndexId() {
        return this.indexId;
    }

    @Override
    public BlockManager getBlockManager() {
        return this.blockManager;
    }

    @Override
    // 读取本block里的所有有效数据
    public byte[] read() throws IOException{
        if(this.size < 0){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

        byte[] bytes = new byte[this.size];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(this.dataPath);
            fileInputStream.read(bytes);
        }catch(FileNotFoundException e){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        } finally {
            if(fileInputStream != null)
                fileInputStream.close();
        }
        return bytes;
    }

    @Override
    public int blockSize() {
        return CAPABILITY;
    }
    public static int getCapability(){
        return CAPABILITY;
    }
    public String getChecksum(){
        return this.checksum;
    };
    private void readMeta() throws IOException{
        File meta = new File(this.metaPath);
        if(!meta.exists() || meta.isDirectory()){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        Reader reader = null;
        BufferedReader br = null;
        try{
            reader = new FileReader(meta);
            br = new BufferedReader(reader);
            // 读blockId、size和checksum
            String next = br.readLine();
            if(next != null){ // 文件非空
             this.size = Integer.parseInt(next);
             this.checksum = br.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }finally {
            if(reader != null)
                reader.close();
            if(br != null)
                br.close();
        }
    }
}
