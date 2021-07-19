import ftp.core.RemoteConnectionFactory;
import ftp.core.RemoteConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class FTPClient {

    private static Logger logger = LogManager.getLogger(FTPClient.class);

    public static void showOptions(){
        System.out.println("Select from Following options (Enter option number).\n" +
                "1. list directories & files on remote server\n" +
                "2. Get file from remote server\n" +
                "3. Get multiple file from remote server\n" +
                "4. list directories & files on local machine\n" +
                "5. Put file onto remote server\n" +
                "6. Put multiple files on remote server\n" +
                "7. Create New Directory on Remote Server\n" +
                "8. Delete directories from remote server\n" +
                "9. Change permissions on remote server\n" +
                "10. Rename file on remote server\n" +
                "11. Rename file on local machine\n" +
                "12. Copy directories on remote server\n" +
                "13. Delete file from remote server\n" +
                "14. Log off from the Server\n" +
                "\n" );
    }

    public static void main(String[] args) throws Exception {
        logger.debug("Main method Execution -> Starts");

        Scanner scan = new Scanner(System.in);
        String userOption;
        String userInput;
        boolean repeateProcess = true;

        System.out.println("HostName:");
//        String hostName = "127.0.0.1";
        String hostName = scan.nextLine();
        System.out.println("UserName:");
        String userName = scan.nextLine();
        System.out.println("Password:");
        String password = scan.nextLine();
        System.out.println("Select Protocol: 1. FTP \t 2. SFTP");
        String protocol = "";
        String protocolNum = scan.nextLine();
        if(protocolNum.equals("1"))
            protocol = "FTP";
        else
            protocol = "SFTP";

        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(protocol);

        boolean connected = remoteConnection.connect(hostName, userName, password);
        if(connected){

            System.out.println("\n--- Connected to Remote FTP Server ---\n");
            showOptions();

            while(repeateProcess){
                System.out.println("Choose your Option : ");
                userOption = scan.nextLine();

                switch (userOption){
                    case "1":
                        System.out.println("1. list directories & files on remote server\n");
                        System.out.println("coming soon ... \n");
                        break;

                    case "2":
                        System.out.println("2. Get file from remote server\n");
                        String remoteFileUserInput;
                        String localPathUserInput;
                        boolean promptForRemoteFile = true;
//                        boolean promptForLocalPath = true;

                        // Prompt user for remote file to be downloaded
                        do {
                            System.out.println("Enter File Name to download from Remote Server: \n");
                            remoteFileUserInput = scan.nextLine();
                            if (!remoteConnection.checkDirectoryExists(remoteFileUserInput)) {
                                System.out.println("-- Error: could not locate Directory with the name " + remoteFileUserInput +
                                        " in remote server --");
                                System.out.println("Enter File Name to download from Remote Server: \n");
                            }
                            else {
                                promptForRemoteFile = false;
                            }
                        } while(promptForRemoteFile);

                        // Prompt user for local absolute path
                        // Unsure how to verify user's absolute path. No re-prompting
                        System.out.println("Enter File Path to download to: \n");
                        localPathUserInput = scan.nextLine();


                        remoteConnection.getRemoteFile(remoteFileUserInput, localPathUserInput);

                        break;

                    case "3":
                        System.out.println("3. Get multiple file from remote server\n");
                        System.out.println("coming soon ... \n");
                        break;

                    case "4":
                        System.out.println("4. list directories & files on local machine\n");
                        System.out.println("coming soon ... \n");
                        break;

                    case "5":
                        System.out.println("5. Put file onto remote server\n");
                        System.out.println("coming soon ... \n");
                        break;

                    case "6":
                        System.out.println("6. Put multiple files on remote server\n");
                        System.out.println("coming soon ... \n");
                        break;

                    case "7":
                        System.out.println("7. Create New Directory on Remote Server\n");
                        boolean tryCreatingDirAgain = false;
                        do{
                            System.out.println("Enter Directory Name: ");
                            String dirName = scan.nextLine();
                            if(!remoteConnection.checkDirectoryExists(dirName)){
                                boolean newDirStatus = remoteConnection.createNewDirectory(dirName);
                                if(newDirStatus) {
                                    System.out.println("Directory created Successfully. \n");
                                }
                                else {
                                    System.out.println("-- Error: could not create New Directory in remote server --");
                                }
                                tryCreatingDirAgain = false;
                            } else {
                                System.out.println("Directory name already exists.\n" +
                                        "Do you want try again, using another name? (y/n)");
                                String tryAgain = scan.nextLine();
                                if(tryAgain.equals("y")){
                                    tryCreatingDirAgain = true;
                                }
                            }
                        } while(tryCreatingDirAgain);
                        break;

                    case "8":
                        System.out.println("8. Delete directories from remote server\n");
                        System.out.println("coming soon ... \n");
                        break;

                    case "14":
                        System.out.println("14. Log off from the Server\n");
                        remoteConnection.disconnect();
                        break;

                    default:
                        System.out.println("coming soon ... \n");
                }

                System.out.println("Do you want to choose other option? (y/n): ");
                String repeat = scan.nextLine();
                if (repeat.equals("n")) {
                    repeateProcess = false;
                }
            }
        }else {
            System.out.println("Error: Could not connect to the Server.");
            logger.info("Provide HostName, UserName, Password and select Protocol, when prompted.");
        }
        logger.debug("Main Method Execution -> Ends");
    }
}
