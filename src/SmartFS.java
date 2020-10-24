import Block.BlockManagerSet;
import Block.BlockId;
import Exceptions.ErrorCode;
import File.FileId;
import File.FileManagerSet;
import Utils.Util;
import interfaces.Block;
import interfaces.BlockManager;
import interfaces.File;
import interfaces.FileManager;
import java.util.Scanner;

public class SmartFS {
    private static SmartFS instance = new SmartFS();
    public static SmartFS getInstance(){return instance;}
    private int fileManagerNum;
    private SmartFS(){
        this.fileManagerNum = 0;
    }

    public void process(){
        System.out.println("Welcome to the SmartFileSystem!");
        this.fileManagerNum = FileManagerSet.getInstance().getSize();
        System.out.println("You have "+this.fileManagerNum+
                " file managers to use, and you can refer them by fm1 - fm"+this.fileManagerNum);

        for(;;){
            printPrompt();
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] instr = input.split(" ");
            switch(instr[0]){
                case "help":
                    printHelpInformation();
                    break;
                case "create":
                    createNewFile(instr);
                    break;
                case "move-cursor":
                    moveCursor(instr);
                    break;
                case "get-cursor":
                    getCursor(instr);
                    break;
                case "set-size":
                    setSize(instr);
                    break;
                case "get-size":
                    getSize(instr);
                    break;
                case "smart-cat":
                    smartCat(instr);
                    break;
                case "smart-write":
                    smartWrite(instr);
                    break;
                case "smart-hex":
                    smartHex(instr);
                    break;
                case "smart-copy":
                    smartCopy(instr);
                    break;
                default:
                    System.out.println("[ERROR]: Invalid instruction");
                    printHelpInformation();
                    break;
            }
        }
    }

    private void getSize(String[] instr) {
        if(instr.length != 3){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION).getMessage());
            System.out.println("    get-size fileManager fileName");
            return;
        }
        FileManager fileManager = getFileManagerByName(instr[1]);
        if (fileManager == null) {
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
            return;
        }

        File file = null;
        try {
            file = fileManager.getFile(new FileId(instr[2]));
        } catch (ErrorCode fileNotFound) {
            System.out.println(fileNotFound.getMessage());
        }

        if(file != null)
            System.out.println(file.size());

    }

    private void getCursor(String[] instr) {
        if(instr.length != 3){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION).getMessage());
            System.out.println("    get-size fileManager fileName");
            return;
        }
        FileManager fileManager = getFileManagerByName(instr[1]);
        if (fileManager == null) {
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
            return;
        }

        File file = null;
        try {
            file = fileManager.getFile(new FileId(instr[2]));
        } catch (ErrorCode fileNotFound) {
            System.out.println(fileNotFound.getMessage());
        }

        if(file != null){
            try {
                System.out.println(file.pos());
            }catch(ErrorCode cursorOutOfRange){
                System.out.println(cursorOutOfRange.getMessage());
            }
        }
    }

    private  void setSize(String[] instr){
        if(instr.length != 4){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION).getMessage());
            System.out.println("    set-size fileManager fileName newSize");
            return;
        }

        FileManager fileManager = getFileManagerByName(instr[1]);
        if(fileManager == null){
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
            return;
        }

        File file = null;
        try {
            file = fileManager.getFile(new FileId(instr[2]));
        }catch (ErrorCode fileNotFound){
            System.out.println(fileNotFound.getMessage());
            return;
        }

        try {
            int size = Integer.parseInt(instr[3]);
            file.setSize(size);
        } catch (NumberFormatException numberFormatException) {
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION, instr[3]).getMessage());
        } catch(ErrorCode passiveSize){
            System.out.println(passiveSize.getMessage());
        }
    }

    private void createNewFile(String[] instr){
        if(instr.length != 3){
            System.out.println("[ERROR]: Please check your instruction format.");
            System.out.println("    create fileManager fileName");
            return;
        }

        FileManager fileManager = getFileManagerByName(instr[1]);
        if(fileManager == null){
            System.out.println("[ERROR]: No such fileManager: " + instr[1]);
            return;
        }

        try {
            fileManager.newFile(new FileId(instr[2]));
        } catch (ErrorCode errorCode) {
            System.out.println(errorCode.getMessage());
        }

    }

    private void smartCat(String[] instr){
        if(instr.length != 3){
            System.out.println("[ERROR]: Please check your instruction format:");
            System.out.println("    smart-cat fileManager fileName");
            return;
        }
        FileManager fileManager = getFileManagerByName(instr[1]);
        if (fileManager != null) {
            Util.smart_cat(instr[2],fileManager);
        }else{
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
        }
    }

    private void smartHex(String[] instr){
        if(instr.length != 3){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION).getMessage());
            System.out.println("    smart-hex blockManager blockId");
            return;
        }

        BlockManager blockManager = null;
        try {
            int bmId =Integer.parseInt(instr[1]);
            blockManager = BlockManagerSet.getInstance().getBlockManager(bmId);
        } catch (NumberFormatException numberFormatException) {
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION, instr[1]).getMessage());
        } catch(ErrorCode invalidId){
            System.out.println(invalidId.getMessage());
        }
        if(blockManager == null){
            return;
        }

        Block block = null;
        int isChange = 0;
        try {
            int blockId =Integer.parseInt(instr[2]);
            block = blockManager.getBlock(new BlockId(blockId));
            isChange = 1;
        } catch (NumberFormatException numberFormatException) {
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION, instr[2]).getMessage());
        } catch(ErrorCode invalidId){
            System.out.println(invalidId.getMessage());
        }
        // 这里block==null是因为读出来的就是空的
        if(block == null && isChange == 1){
            System.out.println("[Nothing here]");
            return;
        }

        // 这里block == null是因为报错
        if(block != null){
            Util.smart_hex(block);
        }

    }

    private void smartWrite(String[] instr){
        if(instr.length != 4){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION).getMessage());
            System.out.println("    smart-write fileManager fileName pos");
            return;
        }

        int pos = -1;
        try {
            pos = Integer.parseInt(instr[3]);
        } catch (NumberFormatException numberFormatException) {
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION, instr[3]).getMessage());
            return;
        }

        FileManager fileManager = getFileManagerByName(instr[1]);
        if (fileManager != null) {
            try {
                Util.smart_write(instr[2],pos,fileManager);
            } catch (ErrorCode errorCode) {
                errorCode.printStackTrace();
                System.out.println(new ErrorCode(ErrorCode.IO_EXCEPTION).getMessage());// 写的错误都包装成IO_Exception
            }
        }else{
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
        }

    }

    private void smartCopy(String[] instr){
        if(instr.length != 5){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION).getMessage());
            System.out.println("    smart-write fileManager fileName pos");
            return;
        }

        FileManager fromManager = getFileManagerByName(instr[1]);
        FileManager toManager = getFileManagerByName(instr[3]);
        if(fromManager == null){
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
            return;
        }
        if(toManager == null){
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[3]).getMessage());
            return;
        }

        File fileFrom = null;
        File fileTo = null;
        try{
            fileFrom = fromManager.getFile(new FileId(instr[2]));
            fileTo = toManager.getFile(new FileId(instr[4]));
        }catch (ErrorCode errorCode){
            System.out.println(errorCode.getMessage());
        }
        if(fileFrom == null || fileTo == null){
            return;
        }

        // 目标文件已经被使用
        if(fileTo.size() > 0){
            System.out.println(new ErrorCode(ErrorCode.FILE_ALREADY_USED,instr[4]).getMessage());
        }

        // 保存文件的当前光标，之后还可以恢复
        long tmpCursorFrom = fileFrom.pos();
        long tmpCursorTo = fileFrom.pos();

        fileFrom.move(0,File.MOVE_HEAD);
        fileTo.move(0,File.MOVE_HEAD);

        try {
            byte[] content = fileFrom.read((int) (fileFrom.size() - fileFrom.pos()));
            fileTo.write(content);
        }catch (ErrorCode errorCode){
            System.out.println(new ErrorCode(ErrorCode.IO_EXCEPTION).getMessage());
        }finally {
            // 恢复光标
            fileFrom.move(tmpCursorFrom,File.MOVE_HEAD);
            fileTo.move(tmpCursorTo,File.MOVE_HEAD);
        }

    }

    private void moveCursor(String[] instr){
        // 校验指令长度
        if(instr.length != 5){
            System.out.println("[ERROR]: Please check your instruction format:");
            System.out.println("            move fileManager fileName where offset");
            System.out.println("        e.g. move fm1 file1 head 5");
            return;
        }

        // 校验fm项
        FileManager fileManager = getFileManagerByName(instr[1]);
        if(fileManager == null){
            System.out.println(new ErrorCode(ErrorCode.WRONG_FILE_MANAGER_NAME, instr[1]).getMessage());
            return;
        }

        // 校验file项
        File file = null;
        try {
            file = fileManager.getFile(new FileId(instr[2]));
        } catch (ErrorCode fileNotFound) {
            System.out.println(fileNotFound.getMessage());
        }
        if(file == null){
            return;
        }

        // 校验where和offset项
        try{
            int offset = Integer.parseInt(instr[4]);
            switch(instr[3]){
                case "head":
                    file.move(offset,File.MOVE_HEAD);
                    break;
                case "curr":
                    file.move(offset,File.MOVE_CURR);
                    break;
                case "tail":
                    file.move(offset,File.MOVE_TAIL);
                    break;
                default:
                    System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION, instr[3]).getMessage());
            }
        }catch (NumberFormatException numberFormatException){
            System.out.println(new ErrorCode(ErrorCode.WRONG_INSTRUCTION, instr[4]).getMessage());
        }catch(ErrorCode cursorOutOfRange){
            System.out.println(cursorOutOfRange.getMessage());
        }

    }

    private void printPrompt(){
        System.out.print("SmartFS >> ");
    }

    private void printHelpInformation(){
        System.out.println("[commands list]");
        System.out.println("    help        - get the help information");
        System.out.println("    create      - 'create fileManager fileName' creates a new file with the given name in the fileManager");
        System.out.println("    move-cursor - 'move-cursor fileManager fileName where offset' move the cursor to where + offset, where can be head/curr/tail");
        System.out.println("    get-cursor  - 'get-cursor fileManager fileName' get the cursor of the file");
        System.out.println("    set-size    - 'set-size fileManager fileName newSize' reset the fileSize to new size, fill 0 if newSize is bigger than the old");
        System.out.println("    get-size    - 'get-size fileManager fileName' get the size of the file");
        System.out.println("    smart-cat   - 'smart-cat fileManager fileName' reads all content of the file");
        System.out.println("    smart-write - 'smart-write fileManager fileName pos' input some content and insert them into the file from pos");
        System.out.println("    smart-hex   - 'smart-hex blockManagerId blockId' read the data in the block and represent them in hexadecimal");
        System.out.println("    smart-copy  - 'smart-copy fileManagerFrom fileNameFrom fileManagerTo fileNameTo'copy the content from a file to another");
    }

    private FileManager getFileManagerByName(String fileManagerName){
        String comparedName = "";
        for(int i = 1; i <= fileManagerNum; i++){
            comparedName = "fm"+i;
            if(fileManagerName.equals(comparedName)){
                return FileManagerSet.getInstance().getFileManager(i);
            }
        }
        return null;
    }

}
