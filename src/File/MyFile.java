package File;

import Block.BlockId;
import Block.BlockManagerSet;
import Block.MyBlock;
import Block.MyBlockManager;
import Exceptions.ErrorCode;
import Utils.MD5Util;
import interfaces.Block;
import interfaces.FileManager;
import interfaces.Id;

import java.io.*;
import java.util.ArrayList;

public class MyFile implements interfaces.File {
    private final FileManager fileManager;
    private final String path;
    private long size; //size在运行时候需要读取，不能写死
    private long cursor; // 下一个读写的字节的地址，也是之前字节的总数,取值范围为0~size
    private Id fileId; // fileId暂存在文件名里

    // 从meta文件里获取的信息
    private final ArrayList<int[]> storeBlocks = new ArrayList<>();
    private final int LOGIC_BLOCK_NUM = 3;

    public MyFile(FileManager fileManager, Id fileId, String path) {
        this.fileManager = fileManager;
        this.fileId = fileId;
        this.path = path;
        try{
            readMeta();
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    @Override
    public Id getFileId() {
        return this.fileId;
    }

    @Override
    public FileManager getFileManager() {
        return this.fileManager;
    }

    @Override
    // 从当前位置开始读取length长度的数据，可以跨block
    public byte[] read(int length) throws IOException {
        // 对length做正确性检查
        if(length <= 0){
            return new byte[0];
        }
        if(this.cursor + length > this.size){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

        byte[] content = new byte[length];
        int currEnd = 0; // content下一个可填充字节的索引

        // 读取游标所在的block的数据
        int blockCapability = MyBlock.getCapability();
        int blockIndex = (int) (this.cursor / blockCapability);// no need to add 1 since index begins with 0
        int pointer = (int) (this.cursor % MyBlock.getCapability());
        int access = Math.min(length,blockCapability - pointer);
        while(length > 0){
            byte[] partResult = read(pointer,access,blockIndex);
            System.arraycopy(partResult,0,content,currEnd,access);
            length -= access;
            currEnd += access;
            // 读取下一个块
            blockIndex++;
            access = Math.min(blockCapability,length);
            pointer = 0;
        }
        this.cursor += length;
        return content;
    }

    @Override
    // 从当前光标位置插入b字节数据,不可重写
    public void write(byte[] b) throws IOException {
        // 1. 内容整合
        // 读取光标后面的字节tailcontent和受影响的第一个block在光标前的字节headcontent
        // b为insertcontent，组合三者到一个content里
        int blockSize = MyBlock.getCapability();
        int pointer = (int) this.cursor % blockSize;
        int blocksIndex = (int)this.cursor / blockSize;
        // 从index的开头读influenceContentLength大小的数据
        byte[] headContent = read(0,pointer,blocksIndex);
        int tailContentLength = (int)(this.size - this.cursor);
        byte[] tailContent = read(tailContentLength);
        byte[] newContent = new byte[headContent.length + b.length + tailContentLength];
        System.arraycopy(headContent,0,newContent,0,pointer);
        System.arraycopy(b,0,newContent,pointer,b.length);
        System.arraycopy(tailContent,0,newContent,pointer+b.length,tailContentLength);

        // 2. 开始写入
        // 随机选择一个blockManager，新建一个block（2个duplication），写入数据，不够再建
        int newContentLength = newContent.length;
        int start = 0;
        BlockManagerSet blockManagerSet = BlockManagerSet.getInstance();

        while(newContentLength>0){
            // 原先的block删除腾出空间
            if(blocksIndex < this.storeBlocks.size()){
                int[] oldBlocks = this.storeBlocks.get(blocksIndex);
                for(int i =0; i <oldBlocks.length; i+=2 ){
                    MyBlockManager blockManager = (MyBlockManager) blockManagerSet.getBlockManager(oldBlocks[i]);
                    BlockId blockId = new BlockId(oldBlocks[i+1]);
                    blockManager.removeBlock(blockId);
                }
            }

            // 获取data
            byte[] data;
            int min = Math.min(blockSize,newContentLength);
            data = new byte[min];
            System.arraycopy(newContent,start,data,0,min);

            // 写入block
            int[] blocks = new int[LOGIC_BLOCK_NUM*2];
            for(int i =0;i <LOGIC_BLOCK_NUM*2; i += 2){
                MyBlockManager blockManager = (MyBlockManager)BlockManagerSet.getInstance().getRandomBlockManager();
                Block block = blockManager.newBlock(data);
                blocks[i] = blockManager.getId();
                blocks[i+1] = ((BlockId) block.getIndexId()).getId();
            }

            if(blocksIndex < this.storeBlocks.size())
                this.storeBlocks.remove(blocksIndex);

            this.storeBlocks.add(blocksIndex,blocks);

            newContentLength -= min;
            start += min;
            blocksIndex++;
        }

        this.size += b.length;
        this.cursor += b.length;
        // 3. 更新meta
        updateMeta();
    }

    @Override
    // 把指针移到距where位置offset(offset可正可负)处
    public long move(long offset, int where) {
        long newCursor = -1;
        switch(where){
            case MOVE_CURR:
                newCursor = this.cursor + offset;
                break;
            case MOVE_HEAD:
                newCursor = offset;
                break;
            case MOVE_TAIL:
                newCursor = this.size - 1+ offset;
                break;
        }
        if(newCursor >= this.size && newCursor < 0){
            throw new ErrorCode(ErrorCode.CURSOR_OUT_OF_RANGE);
        }
        this.cursor = newCursor;
        return newCursor;
    }

    @Override
    public void close() {

    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public void setSize(long newSize) throws IOException {
        if(newSize < 0){
            throw new ErrorCode(ErrorCode.PASSIVE_SIZE);
        }
        if(newSize <= this.size){
            this.size = newSize;
        }else{
            // 后面填充0
            int len = (int)(newSize-this.size);
            byte[] newContent = new byte[len];
            this.cursor = move(0,MOVE_TAIL) + 1;
            write(newContent);
            this.size = newSize;
        }
        updateMeta();
    }

    // 从一个block的where处开始，取length长度的数据出来
    private byte[] read(int where, int length, int blockIndex) throws IOException {
        // 不能跨block
        if(where + length > MyBlock.getCapability()){
            throw new ErrorCode(ErrorCode.CURSOR_OUT_OF_RANGE);
        }

        byte[] result= new byte[length];
        if(blockIndex < this.storeBlocks.size()) {
            int[] blocks = this.storeBlocks.get(blockIndex);
            int len = blocks.length;
            // 从logic block中找一个能用的
            for (int i = 0; i < len; i += 2) {
                int BMId = blocks[i];
                int blockId = blocks[i + 1];
                MyBlockManager blockManager = (MyBlockManager) BlockManagerSet.getInstance().getBlockManager(BMId);
                MyBlock block = ((MyBlock) blockManager.getBlock(new BlockId(blockId)));
                byte[] blockData = block.read();
                String verification = MD5Util.byteEncode(blockData);
                if (verification.equals(block.getChecksum())) {   // checksum校验成功
                    System.arraycopy(blockData, where, result, 0, length);
                    return result;
                } else { // checksum校验失败，更换logic block
                    if (i == len - 2)
                        throw new ErrorCode(ErrorCode.BLOCK_BROKEN);
                }
            }
        }
        return result;
    }

    // 读取并更新file的meta信息
    private void readMeta() throws IOException {
        File file = new File(this.path);
        if(!file.exists() || file.isDirectory()){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        Reader reader = null;
        BufferedReader br = null;
        try{
            reader = new FileReader(file);
            br = new BufferedReader(reader);
            // 读取 fileId 和 filesize
            String next = br.readLine();
            if(next != null){ // 文件非空
                this.fileId = new FileId(Integer.parseInt(next));
                this.size = Integer.parseInt(br.readLine());
            }

            // 读取block块
            next = br.readLine();
            while (next != null){
                String[] tmp = next.split(" ");
                int[] blocks = new int[LOGIC_BLOCK_NUM*2];
                for(int i = 0; i<tmp.length; i++){
                    blocks[i] = Integer.parseInt(tmp[i]);
                }
                this.storeBlocks.add(blocks);
                next = br.readLine();
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

    // 更新文件中的meta信息
    public void updateMeta() throws IOException {
        File file = new File(this.path);
        if(!file.exists() || file.isDirectory()){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        Writer writer = null;
        BufferedWriter bw = null;
        try{
            writer = new FileWriter(file);
            bw = new BufferedWriter(writer);
            int fileId = ((FileId) this.fileId).getId();
            bw.write(Integer.toString(fileId));
            bw.newLine();
            bw.write(Integer.toString((int)this.size));
            bw.newLine();

            int len = this.storeBlocks.size();
            int[] blocks;
            for(int i = 0 ; i < len; i++){
                StringBuilder sb = new StringBuilder();
                blocks = this.storeBlocks.get(i);
                for(int num : blocks){
                    sb.append(num);
                    sb.append(" ");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.flush();
        }finally{
            if(writer != null)
                writer.close();
            if(bw!=null)
                bw.close();
        }

    }
}
