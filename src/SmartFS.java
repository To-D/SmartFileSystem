import Block.BlockManagerSet;
import File.FileManagerSet;
import interfaces.FileManager;
import java.io.IOException;
import java.util.Scanner;

public class SmartFS {
    private static SmartFS instance = new SmartFS();
    public static SmartFS getInstance(){return instance;}
    private SmartFS(){}

    public void process() throws IOException {
        for(;;){
            printPrompt();
            // waiting for input
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] instr = input.split(" ");
            switch(instr[0]){
                case "help":
                    printHelpInformation();
                    break;
                case "create":
                    break;
//                case "create":
//                    break;

            }
        }
    }

    private void printPrompt(){
        System.out.println("SmartFS >>");
    }

    private void printHelpInformation(){
        System.out.println("[commands list]");
        System.out.println("    help   - get the help information");
        System.out.println("    create - 'create fileId' create a new file with the given id, return the file name");
        System.out.println("    write  - 'write filename content', write the content to the file related to the filename");
        System.out.println("    move   - 'move filename where offset'");

    }
}
