package File;
import Exceptions.ErrorCode;
import interfaces.FileManager;
import interfaces.Id;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyFileManager implements FileManager {
    private final Map<String,interfaces.File> map = new HashMap<>(); // fileId -> file
    private final String path;

    public MyFileManager(String path) {
        // 文件系统持久化，恢复上次运行结果
        this.path = path;
        File dir = new File(path);
        File[] fileNames= dir.listFiles();
        if(fileNames != null && fileNames.length > 0) {
            for (File file : fileNames) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    FileId fileId = new FileId(fileName);
                    map.put(fileId.getId(), new MyFile(this, fileId, file.getAbsolutePath()));
                }
            }
        }
    }


    @Override
    public interfaces.File getFile(Id fileId) throws ErrorCode{
        String id = ((FileId) fileId).getId();
        if(map.containsKey(id)){
            return map.get(id);
        }else{
            throw new ErrorCode(ErrorCode.FILE_NOT_FOUND,id);
        }
    }

    @Override
    public interfaces.File newFile(Id fileId) {
        String id = ((FileId) fileId).getId();
        if(map.containsKey(id)){
            throw new ErrorCode(ErrorCode.DUPLICATED_ID);
        }else{
            // 创建文件实体
            String filePath = this.path + id;
            File file = new File(filePath);
            if(!file.exists()){
                try{
                    file.createNewFile();
                }catch (IOException e){
                    throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                }
            }else{
                throw new ErrorCode(ErrorCode.FILE_NAME_OCCUPIED);
            }

            // 创建数据对象
            MyFile newFile = new MyFile(this,fileId,filePath);

            try{
                newFile.writeMeta();
            }catch (ErrorCode e){
                file.delete();
                throw new ErrorCode(ErrorCode.IO_EXCEPTION);
            }

            map.put(id,newFile);
            return newFile;
        }
    }
}
