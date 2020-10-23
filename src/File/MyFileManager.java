package File;
import Exceptions.ErrorCode;
import interfaces.FileManager;
import interfaces.Id;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyFileManager implements FileManager {
//    private final Map<String,Integer> name2Id = new HashMap<>(); // fileName -> fileId
    private final Map<Integer,interfaces.File> map = new HashMap<>(); // fileId -> file
    private final String path;

    public MyFileManager(String path) {
        this.path = path;
        File dir = new File(path);
        File[] fileNames= dir.listFiles();
        if(fileNames != null && fileNames.length > 0) {
            for (File file : fileNames) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    fileName = fileName.substring(1, fileName.lastIndexOf("."));
                    FileId fileId = new FileId(Integer.parseInt(fileName));
                    map.put(fileId.getId(), new MyFile(this, fileId, file.getAbsolutePath()));
                }
            }
        }
    }


    @Override
    public interfaces.File getFile(Id fileId) {
        int numId = ((FileId) fileId).getId();
        if(map.containsKey(numId)){
            return map.get(numId);
        }else{
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public interfaces.File newFile(Id fileId) {
        int numId = ((FileId) fileId).getId();
        if(map.containsKey(numId)){
            throw new ErrorCode(ErrorCode.DUPLICATED_ID);
        }else{
            // 创建文件实体
            String filePath = this.path + "f" + numId + ".meta";
            File file = new File(filePath);
            if(!file.exists()){
                try{
                    if(!file.createNewFile()){
                        throw new ErrorCode(ErrorCode.CREATE_DIRECTORY_OR_FILE_FAILED);
                    }
                }catch (IOException e){
                    throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                }
            }else{
                throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
            }

            // 创建数据对象
            MyFile newFile = new MyFile(this,fileId,filePath);
            try{
                newFile.updateMeta();
            }catch (IOException e){
                file.delete();
                throw new ErrorCode(ErrorCode.IO_EXCEPTION);
            }

            map.put(numId,newFile);
            return newFile;
        }
    }
}
