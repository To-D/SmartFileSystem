package interfaces;

import java.io.IOException;

public interface File {
    int MOVE_CURR = 0; // 文件中光标的位置
    int MOVE_HEAD = 1; // 文件开头，1无实际意义
    int MOVE_TAIL = 2; // 文件结尾，2无实际意义

    Id getFileId();
    FileManager getFileManager();
    byte[] read(int length) throws IOException;
    void write(byte[] b) throws IOException;
    default long pos(){
        return move(0, MOVE_CURR);
    }
    long move(long offset, int where);

    // 使用buffer需实现
    void close();
    long size();
    void setSize(long newSize) throws IOException;
}
