package Block;

public class Buffer {
    private final byte[] writeBuffer;
    private int size;

    public Buffer() {
        this.writeBuffer = new byte[MyBlock.getCapability()];
        size = 0;
    }

    // 取出buffer中的数据
    public byte[] copy(){
        byte[] data = new byte[size];
        if (size >= 0) System.arraycopy(writeBuffer, 0, data, 0, size);
        return data;
    }

    // 写入一个字节到buffer中
    public boolean write(byte b){
        if(size == writeBuffer.length)
            return false;
        writeBuffer[size] = b;
        size++;
        return true;
    }

    public void clear(){
        size = 0;
    }
    public int getSize() {
        return size;
    }
}
