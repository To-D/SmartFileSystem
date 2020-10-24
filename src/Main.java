import File.FileId;
import File.FileManagerSet;
import File.MyFileManager;
import Utils.Util;
import interfaces.File;

public class Main {
    public static void main(String[] args){
        SmartFS.getInstance().process();
//        MyFileManager fileManager = (MyFileManager)FileManagerSet.getInstance().getFileManager(2);
//        File file2 =fileManager.getFile(new FileId("file2"));
//        file2.move(1,File.MOVE_HEAD);
//        Util.smart_cat("file2",fileManager);

    }


}
