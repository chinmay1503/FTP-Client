import ftp.core.RemoteConnectionFactory;
import ftp.core.RemoteConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

public class FTPClient {

    private static final Logger logger = LogManager.getLogger(FTPClient.class);

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

        try (Scanner scan = new Scanner(System.in)) {
            String userOption;
            boolean repeatProcess = true;

            System.out.println("HostName: (Eg: 127.0.0.1 or www.yourServer.com)");
            String hostName = scan.nextLine();
            if (isNullOrEmpty(hostName))
                hostName = "127.0.0.1";
            String userName = getInputFromUser(scan, "UserName", "UserName");
            String password = getInputFromUser(scan, "Password", "Password");

            System.out.println("Select Protocol: 1. FTP \t 2. SFTP");
            String protocol;
            String protocolNum = scan.nextLine();
            if (protocolNum.equals("1"))
                protocol = "FTP";
            else
                protocol = "SFTP";

            RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
            RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(protocol);

            boolean connected = remoteConnection.connect(hostName, userName, password);
            if (connected) {

                System.out.println("\n--- Connected to Remote FTP Server ---\n");
                showOptions();

                while (repeatProcess) {
                    System.out.println("Choose your Option : ");
                    userOption = scan.nextLine();

                    switch (userOption) {
                        case "1":
                            remoteConnection.listCurrentDirectory();
                            break;

                        case "2":
                            System.out.println("2. Get file from remote server\n");
                            System.out.println("coming soon ... \n");
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

                            System.out.println("Enter Local file path, that you want to upload");
                            String localFilePath = scan.nextLine();

                            System.out.println("Enter Destination");
                            String remotePath = scan.nextLine();

                            remoteConnection.uploadSingleFile(localFilePath, remotePath);

                            break;

                        case "6":
                            System.out.println("6. Put multiple files on remote server\n");

                            System.out.println("Enter Destination");
                            String remote_Path = scan.nextLine();

                            Set uploadFilesSet = new HashSet<String>();
                            boolean uploadMore;

                            do {
                                uploadMore = false;
                                System.out.println("Enter Local file path, that you want to upload");
                                String local_Path = scan.nextLine();

                                uploadFilesSet.add(local_Path);

                                System.out.println("Do you want to upload another File ? (y/n)");
                                String uploadMoreFiles = scan.nextLine();
                                if (uploadMoreFiles.equals("y")) {
                                    uploadMore = true;
                                }
                            } while (uploadMore);
                            remoteConnection.uploadMultipleFiles(Arrays.copyOf(uploadFilesSet.toArray(), uploadFilesSet.toArray().length, String[].class), remote_Path);
                            break;

                        case "7":
                            System.out.println("7. Create New Directory on Remote Server\n");
                            boolean tryCreatingDirAgain;
                            do {
                                tryCreatingDirAgain = false;
                                System.out.println("Enter Directory Name: (relative path or absolute path)");
                                String dirName = scan.nextLine();
                                boolean newDirStatus = remoteConnection.createNewDirectory(dirName);
                                if (newDirStatus) {
                                    System.out.println("* Directory created Successfully. *\n");
                                } else {
                                    System.out.println("-- Error: could not create New Directory in remote server --\n" +
                                            "Directory may already exist. Do you want try creating Directory again ? (y/n)");
                                    String tryAgain = scan.nextLine();
                                    if (tryAgain.equals("y")) {
                                        tryCreatingDirAgain = true;
                                    }
                                }
                            } while (tryCreatingDirAgain);
                            break;

                        case "8":
                            System.out.println("8. Delete directories from remote server\n");
                            String dirPath = getInputFromUser(scan, "Please enter the path to the remote directory you would like to delete", "Path");
                            if (remoteConnection.deleteDirectory(dirPath)) {
                                System.out.println("Directory deleted Successfully. \n");
                            } else {
                                System.out.println("-- Error: could not delete New Directory in remote server --");
                            }
                            break;

                        case "10":
                            System.out.println("10. Rename file on remote server\n");
                            String oldName = getInputFromUser(scan, "Enter name of file to rename", "oldName");
                            String newName = getInputFromUser(scan, "Enter new name", "newName");
                            boolean success = remoteConnection.renameRemoteFile(oldName, newName);
                            if (success) {
                                System.out.println(oldName + " was renamed to: " + newName);
                            } else {
                                System.out.println("Failed to rename: " + oldName);
                            }
                            break;

                        case "12":
                            System.out.println("12. Copy directories on remote server\n");
                            String toCopy = getInputFromUser(scan,"Enter name of directory to copy", "toCopy");
                            String newDir = getInputFromUser(scan, "Enter name of new copy", "newDir");
                            remoteConnection.copyDirectory(toCopy, newDir);
                            break;

                        case "13":
                            System.out.println("13. Delete file from remote server\n");
                            String filePath = getInputFromUser(scan, "Please enter the file path to the remote directory you would like to delete", "filePath");
                            if (remoteConnection.deleteFile(filePath)) {
                                System.out.println("File deleted Successfully. \n");
                            } else {
                                System.out.println("-- Error: could not delete file in remote server --");
                            }
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
                    if (repeat.equalsIgnoreCase("n")) {
                        repeatProcess = false;
                    }
                }
            } else {
                System.out.println("Error: Could not connect to the Server.");
                logger.info("Provide HostName, UserName, Password and select Protocol, when prompted.");
            }
            logger.debug("Main Method Execution -> Ends");
        }
    }

    private static String getInputFromUser(Scanner scan, String inputMsg, String fieldName) {
        String inputString;
        do {
            System.out.print(inputMsg + ": ");
            inputString = scan.nextLine();
            checkNullOrEmpty(inputString, fieldName);
        } while (inputString.length() == 0);
        return inputString;
    }

    public static void checkNullOrEmpty(String input, String fieldName) {
        if (isNullOrEmpty(input))
            System.out.println((String.format("Field [%s] is mandatory", fieldName)));
    }
}
