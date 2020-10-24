package File;

import Block.BlockId;
import Block.BlockManagerSet;
import Block.MyBlock;
import Block.MyBlockManager;
import Exceptions.ErrorCode;
import interfaces.Block;
import interfaces.BlockManager;
import interfaces.FileManager;
import interfaces.Id;

import java.io.*;
import java.util.ArrayList;

public class MyFile implements interfaces.File {
    private final FileManager fileManager;
    private final String path;
    private long size; //size在运行时候需要读取，不能写死
    private long cursor; // 下一个读写的字节的地址，也是之前字节的总数,取值范围为0~size

    private Buffer buffer;
    private final int LOGIC_BLOCK_NUM = 3;

    // 从meta文件里获取的信息
    private Id fileId;
    private final ArrayList<int[]> storeBlocks = new ArrayList<>();

    public MyFile(FileManager fileManager, Id fileId, String path) {
        this.fileManager = fileManager;
        this.fileId = fileId;
        this.path = path;
        try{
            readMeta();
        }catch (ErrorCode e){
            throw new ErrorCode(ErrorCode.INITIAL_FILE_FAILED);
        }
        this.cursor = 0;
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
    /**
     * @throws ErrorCode - CURSOR_OUT_OF_RANGE,
     */
    public byte[] read(int length) throws ErrorCode {
        // 对length做正确性检查
        if(length <= 0){
            return new byte[0];
        }
        if(this.cursor + length > this.size){
            throw new ErrorCode(ErrorCode.CURSOR_OUT_OF_RANGE);
        }

        byte[] content = new byte[length];
        int currEnd = 0; // content下一个可填充字节的索引
        // 读取游标所在的block的数据
        int blockCapability = MyBlock.getCapability();
        int blockIndex = (int) (this.cursor / blockCapability);// no need to add 1 since index begins with 0
        int pointer = (int) (this.cursor % MyBlock.getCapability());
        int access = Math.min(length,blockCapability - pointer);
        int tmp = length;
        while(tmp > 0){
            byte[] partResult = readBlock(pointer, access, blockIndex);
            System.arraycopy(partResult,0,content,currEnd,access);
            tmp -= access;
            currEnd += access;
            // 读取下一个块
            blockIndex++;
            access = Math.min(blockCapability,tmp);
            pointer = 0;
        }
        this.cursor += length;
        return content;
    }

    @Override
    // 从当前光标位置插入b字节数据,不可重写
    public void write(byte[] b){
        // 1. 内容整合
        int blockSize = MyBlock.getCapability();
        int pointer = (int) this.cursor % blockSize;
        int blocksIndex = (int)this.cursor / blockSize;
        // 从blockIndex的开头读influenceContentLength大小的数据
        byte[] headContent = readBlock(0,pointer,blocksIndex);
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
                Block block = blockManager.newBlock(data); // 可能会扔block create error
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
        writeMeta();
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
                newCursor = this.size + offset;
                break;
        }
        if(newCursor > this.size || newCursor < 0){
            throw new ErrorCode(ErrorCode.CURSOR_OUT_OF_RANGE);//报错, cursor不更新
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
    public void setSize(long newSize){
        if(newSize < 0){
            throw new ErrorCode(ErrorCode.PASSIVE_SIZE);
        }
        // 删除
        // 3. 新建一个Block存放数据尾巴，然后存到storeBlocks中
        if(newSize < this.size){
            // 1. 计算删除后的尾部，并把数据尾巴读出来
            int newEnd = (int) newSize;
            int influenceBlockIndex = newEnd / MyBlock.getCapability();// 第一个受影响的block
            int pointer = newEnd % MyBlock.getCapability();
            byte[] remain = readBlock(0,pointer,influenceBlockIndex); // 把受影响的小尾巴取出来

            // 2. 如果还有尾巴上的数据,新建block保存小尾巴
            if(remain.length >0){
                int[] newBlock = new int[LOGIC_BLOCK_NUM*2];
                for(int i =0; i<LOGIC_BLOCK_NUM*2; i+=2){
                    MyBlockManager blockManager = ((MyBlockManager) BlockManagerSet.getInstance().getRandomBlockManager());
                    Block block = blockManager.newBlock(remain);
                    newBlock[i] = blockManager.getId();
                    newBlock[i+1] = ((BlockId) block.getIndexId()).getId();
                }
                this.storeBlocks.add(influenceBlockIndex,newBlock);
            }

            // 3. 把受影响的block都remove掉，并从storeBlocks里清除
            int size = this.storeBlocks.size();
            for(int i = influenceBlockIndex + 1; i < size; i++){
                int[] oldBlocks = this.storeBlocks.get(influenceBlockIndex + 1);
                for(int j =0; j <oldBlocks.length; j+=2 ){
                    MyBlockManager blockManager = (MyBlockManager) BlockManagerSet.getInstance().getBlockManager(oldBlocks[j]);
                    BlockId blockId = new BlockId(oldBlocks[j+1]);
                    blockManager.removeBlock(blockId);
                }
                this.storeBlocks.remove(influenceBlockIndex + 1);
            }
            this.size = newSize;
        }else{
            // 后面填充0，属于重写不受影响
            int len = (int)(newSize-this.size);
            byte[] newContent = new byte[len];
            this.cursor = move(0,MOVE_TAIL);
            write(newContent);
            this.size = newSize;
        }

        move(0,MOVE_TAIL);
        writeMeta();
    }

    /**
     * @throws ErrorCode - IO_Exception,  BLOCK_BROKEN
     */
    // 从一个block的where处开始，取length长度的数据出来
    private byte[] readBlock(int where, int length, int blockIndex) throws ErrorCode {
        if(where + length > MyBlock.getCapability()){// 不能跨block
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
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
                if(!block.isBroken()){
                    byte[] blockData = block.read();
                    System.arraycopy(blockData, where, result, 0, length);
                    return result;
                }else{
                    if (i == len - 2)
                        throw new ErrorCode(ErrorCode.BLOCK_BROKEN);
                }
            }
        }
        return result;
    }

    // 读取并更新file的meta信息
    private void readMeta() {
        File file = new File(this.path);
        if(!file.exists() || file.isDirectory()){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        try (Reader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
            String next = br.readLine();
            if (next != null) { // 文件非空
                // 读取filesize
                this.size = Integer.parseInt(next);
                // 读取block块
                next = br.readLine();
                while (next != null) {
                    String[] tmp = next.split(" ");
                    int[] blocks = new int[LOGIC_BLOCK_NUM * 2];
                    for (int i = 0; i < tmp.length; i++) {
                        blocks[i] = Integer.parseInt(tmp[i]);
                    }
                    this.storeBlocks.add(blocks);
                    next = br.readLine();
                }
            }
        } catch (FileNotFoundException e) {
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        } catch (IOException ioException) {
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }

    // 更新文件中的meta信息
    public void writeMeta(){
        File file = new File(this.path);
        if(!file.exists() || file.isDirectory()){
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }

        try (Writer writer = new FileWriter(file); BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(Integer.toString((int) this.size));
            bw.newLine();

            int len = this.storeBlocks.size();
            int[] blocks;
            for (int i = 0; i < len; i++) {
                StringBuilder sb = new StringBuilder();
                blocks = this.storeBlocks.get(i);
                for (int num : blocks) {
                    sb.append(num);
                    sb.append(" ");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.flush();
        }catch(IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

    }
}
