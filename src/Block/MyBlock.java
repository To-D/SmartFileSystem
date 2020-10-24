package Block;

import Exceptions.ErrorCode;
import Utils.MD5Util;
import interfaces.BlockManager;
import interfaces.Id;

import java.io.*;

/**
 * 仅负责内存上的控制，实体文件交由BlockManager控制
 */
public class MyBlock implements interfaces.Block {
    private static final int CAPABILITY = 512; // bytes
    private final String metaPath;
    private final String dataPath;
    private final Id indexId;
    private final BlockManager blockManager;

    // block meta
    private int size;
    private String checksum;

    // 新建block, 写入数据出错就扔一个
    public MyBlock(BlockManager blockManager, Id indexId,byte[] data){
        this.metaPath = ((MyBlockManager) blockManager).getPath() +"meta\\b" + ((BlockId) indexId).getId() + ".meta";
        this.dataPath = ((MyBlockManager) blockManager).getPath() + "data\\b" + ((BlockId) indexId).getId() + ".data";
        this.blockManager = blockManager;
        this.indexId = indexId;
        createBlock(data);
    }

    // 已经存在的block
    public MyBlock(BlockManager blockManager, Id indexId){
        this.metaPath = ((MyBlockManager) blockManager).getPath() +"meta\\b" + ((BlockId) indexId).getId() + ".meta";
        this.dataPath = ((MyBlockManager) blockManager).getPath() + "data\\b" + ((BlockId) indexId).getId() + ".data";
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
    public byte[] read() throws ErrorCode{
        if(this.size < 0){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

        byte[] bytes = new byte[this.size];
        try {
            try (FileInputStream fileInputStream = new FileInputStream(this.dataPath)) {
                fileInputStream.read(bytes);
            }
        }catch(FileNotFoundException e){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }catch(IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
        return bytes;
    }

    // read block meta when initialing the SmartFS
    private void readMeta() {
        File meta = new File(this.metaPath);
        if(!meta.exists() || meta.isDirectory()){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        Reader reader = null;
        BufferedReader br = null;
        try{
            try{
                reader = new FileReader(meta);
                br = new BufferedReader(reader);
                // 读blockId、size和checksum
                String next = br.readLine();
                if(next != null){ // 文件非空
                    this.size = Integer.parseInt(next);
                    this.checksum = br.readLine();
                }
            }finally {
                if(reader != null)
                    reader.close();
                if(br != null)
                    br.close();
            }
        } catch (FileNotFoundException e) {
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        } catch( IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    // write data and meta
    public void createBlock(byte[] data) throws ErrorCode{
        if(data.length > CAPABILITY){
            throw new ErrorCode(ErrorCode.BYTE_ARRAY_TOO_LARGE);
        }

        if(data.length == 0){
            this.size = 0;
            return;
        }

        // 写入data
        try (OutputStream dataOS = new FileOutputStream(this.dataPath)) {
            dataOS.write(data);
        } catch (FileNotFoundException e) {
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        } catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

        // 写入meta
        String checksum = MD5Util.byteEncode(data);
        try (Writer writer = new FileWriter(this.metaPath); BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(Integer.toString(data.length));
            bw.newLine();
            bw.write(checksum);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

        this.size = data.length;
        this.checksum = checksum;
    }

    public static int getCapability(){
        return CAPABILITY;
    }

    public boolean isBroken(){
        byte[] content = read();
        return !MD5Util.byteEncode(content).equals(this.checksum);
    }

    @Override
    public int blockSize() {
        return this.size;
    }



}
