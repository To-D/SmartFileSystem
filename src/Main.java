import Block.BlockManagerSet;
import File.*;
import interfaces.File;
import interfaces.FileManager;
import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        FileManager fileManager = FileManagerSet.getInstance().getFileManager(1);
        FileId fileId = new FileId();
        File file = fileManager.newFile(fileId);
        String tmp = "abcdefg";
        file.write(tmp.getBytes());

//        System.out.println();
//        byte[] newco = new byte[1];
//        file.move(1,File.MOVE_HEAD);
//        file.write(newco);
//        System.out.println(file.read(5).toString());

//
//        byte[] tmp = new byte[1];
//        file.move(3, File.MOVE_HEAD);
//        file.write(tmp);
//        byte[] result = file.read(10);
//        for(byte a:result){
//            System.out.println(a);
//        }


//        a.add(b[0]);
//        a.add(0,b[1]);
//        System.out.println(a.size());
//        for(int i =0; i<2;i++) {
//            System.out.println(a.get(0)[i]);
//        }


//        byte[]  a = new byte[];
//        for(int i =0; i<100; i++){
//            System.out.println(a[i]);
//        }
//        file.setSize(600);
//        byte[]  b = file.read(600);
//        for(int i =0; i<600; i++){
//            System.out.println(b[i]);
//        }
    }


}
