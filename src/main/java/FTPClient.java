import ftp.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The main class for the FTP-Client Project
 *
 * @authors Aditya Sharoff, Anthony Chin, Chinmay Tawde, Minjin Enkhjargal, Sree Vandana
 */
public class FTPClient {

    private static final Logger logger = LogManager.getLogger(FTPClient.class);
    private static String hostName = "";
    private static String protocol = "";
    private static String userName = "";
    private static String password = "";

    /**
     * This method is used to print the options for user's to choose from.
     */
    public static void showOptions() {
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
                "14. Search file on remote server\n" +
                "15. Search file on local machine\n" +
                "16. Log off from the Server\n" +
                "\n");
    }

    /**
     * This method is used to provide connection options for the user.
     *
     * @param promptDialog [String] - Prompt to display before providing the options.
     */
    public static void showConnectionOptions(String promptDialog) throws FTPClientException {

        try {
            logger.debug("creating clientCredentials.json file if it does not exsist");
            File myObj = new File("ClientCredentials.json");
            if (myObj.createNewFile()) {
                FileWriter myWriter = new FileWriter("ClientCredentials.json");
                myWriter.write("[]");
                myWriter.close();
                logger.debug("created the file");
            }
        } catch (IOException e) {
            logger.info("-- Error occurred while creating the file --");
            throw new FTPClientException(e);
        }

        boolean repeatConnectOptions = false;
        Scanner scan = new Scanner(System.in);
        logger.debug("Prompting the user to select connect options.");

        System.out.println(promptDialog + "\n");
        String connectOptions = FTPUtils.getInputFromUser(scan, "1. Use saved Connections \t"
                + " 2. Enter New user Credentials and Save them \t"
                + " 3. Exit\n"
                + "Enter your Option", "connectOptions");
        do {
            switch (connectOptions) {
                case "1":
                    System.out.println("List of all saved connections (Choose from below connections):\n");
                    ArrayList<String> selectedUserDetails = FTPUtils.listAllUserCredentials();
                    if (selectedUserDetails.isEmpty()) {
                        connectOptions = "2";
                        repeatConnectOptions = true;
                    } else {
                        userName = selectedUserDetails.get(0);
                        hostName = selectedUserDetails.get(2);
                        protocol = selectedUserDetails.get(3);
                        System.out.println("Connecting to:\nUserName: " + userName + "\tserver: " + hostName + "\tProtocol: " + protocol);
                        password = selectedUserDetails.get(1);
                    }
                    break;

                case "2":
                    System.out.println("HostName: (Eg: 127.0.0.1)");
                    hostName = scan.nextLine();
                    if (isNullOrEmpty(hostName))
                        hostName = "127.0.0.1";

                    userName = FTPUtils.getInputFromUser(scan, "UserName", "UserName");
                    password = FTPUtils.getInputFromUser(scan, "Password", "Password");

                    String protocolNum = FTPUtils.getInputFromUser(scan, "Select Protocol: 1. FTP \t 2. SFTP", "protocolNum");
                    if ("1".equals(protocolNum))
                        protocol = "FTP";
                    else
                        protocol = "SFTP";

                    repeatConnectOptions = false;
                    break;

                case "3":
                    System.exit(0);
                    break;

                default:
                    connectOptions = FTPUtils.getInputFromUser(scan,
                            "please enter Correct option, choose form following.\n"
                                    + "1. Use saved Connections \t 2. Enter New user Credentials and Save them\n"
                                    + "Enter your Option",
                            "connectOptions");
                    repeatConnectOptions = true;
                    break;
            }
        } while (repeatConnectOptions);

    }

    /**
     * Main method for FTPClient class.
     *
     * @throws Exception This method can throw many Exception's. so mentioning parent Exception.
     */
    public static void main(String[] args) throws FTPClientException {

        String userOption;
        boolean repeatProcess = true;
        boolean connected = false;
        int connected_status;
        RemoteConnection remoteConnection = null;

        logger.debug("Main method Execution -> Starts");

        try (Scanner scan = new Scanner(System.in)) {

            showConnectionOptions("How do you want to connect to remote server ?");

            System.out.println("Connecting to Remote Server...");
            RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
            remoteConnection = remoteConnectionFactory.getInstance(protocol);
            do {
                connected_status = remoteConnection.connect(hostName, userName, password);

                if (connected_status == 1) {
                    connected = true;
                } else if (connected_status == 0) {
                    connected = false;
                    showConnectionOptions("Try Other options");
                } else {
                    System.exit(0);
                }
            } while (!connected);


            if (connected) {
                FTPUtils.storeClientCredentials(hostName, userName, password, protocol);
                if ("FTP".equals(protocol))
                    System.out.println("\n--- Connected to Remote FTP Server ---\n");
                else
                    System.out.println("\n--- Connected to Remote SFTP Server ---\n");
                showOptions();

                // Provide respective functionality to user, based on their choice.
                while (repeatProcess) {
                    userOption = FTPUtils.getInputFromUser(scan, "Choose your Option", "userOption");
                    switch (userOption) {
                        case "1":
                            System.out.println("1. list directories & files on remote server\n");
                            remoteConnection.listCurrentDirectory();
                            break;

                        case "2":
                            System.out.println("2. Get file from remote server\n");
                            String remoteFileUserInput;
                            String localPathUserInput;
                            boolean promptForRemoteFile;
                            boolean promptForLocalPath;

                            // Prompt user for remote file to be downloaded
                            do {
                                remoteFileUserInput = FTPUtils.getInputFromUser(scan, "Enter File Name to download from Remote Server", "remoteFileUserInput");
                                promptForRemoteFile = remoteConnection.checkFileExists(remoteFileUserInput);
                                if (!promptForRemoteFile) {
                                    System.out.println("-- Error: could not locate remote Directory with the name " + remoteFileUserInput + " --");
                                }
                            } while (!promptForRemoteFile);

                            do {
                                localPathUserInput = FTPUtils.getInputFromUser(scan, "Enter File Path to download to", "localPathUserInput");
                                promptForLocalPath = remoteConnection.checkLocalDirectoryExists(localPathUserInput);
                                if (!promptForLocalPath) {
                                    System.out.println("-- Error: could not locate local Directory with the name " + localPathUserInput + " --");
                                }
                            } while (!promptForLocalPath);
                            remoteConnection.downloadSingleFile(localPathUserInput, remoteFileUserInput);

                            break;

                        case "3":
                            System.out.println("3. Get multiple file from remote server\n");
                            String userOptions = FTPUtils.getInputFromUser(scan, "Would you like to download the contents of the entire directory? y/n\n", "userOption");

                            //Call method from case 11 to download entire contents of remote directory
                            if ("y".equalsIgnoreCase(userOptions)) {
                                String dirPath = FTPUtils.getInputFromUser(scan, "Enter remote Directory Path", "dirPath");
                                String localPath = FTPUtils.getInputFromUser(scan, "Enter Destination to download to", "local_Path");

                                if (remoteConnection.downloadDirectory(dirPath, localPath)) {
                                    System.out.println(dirPath + " successfully downloaded to " + localPath);
                                } else {
                                    System.out.println("Failed to download " + dirPath);
                                }
                            }
                            //Prompt and download each file from the remote directory
                            else {
                                String local_Path = FTPUtils.getInputFromUser(scan, "Enter Destination to download to", "local_Path");
                                Set<String> downloadFilesSet = new HashSet<>();
                                boolean downloadMore;

                                do {
                                    downloadMore = false;
                                    do {
                                        String remote_Path = FTPUtils.getInputFromUser(scan, "Enter remote path of the file you wish to download", "remote_Path");
                                        promptForRemoteFile = remoteConnection.checkFileExists(remote_Path);

                                        if (!promptForRemoteFile) {
                                            System.out.println("-- Error: could not locate remote Directory with the name " + remote_Path + " --");
                                        } else {
                                            downloadFilesSet.add(remote_Path);
                                        }
                                    } while (!promptForRemoteFile);

                                    String downloadMoreFiles = FTPUtils.getInputFromUser(scan, "Do you want to download another File? (y/n)", "downloadMoreFiles");
                                    if ("y".equalsIgnoreCase(downloadMoreFiles)) {
                                        downloadMore = true;
                                    }
                                } while (downloadMore);
                                remoteConnection.downloadMultipleFiles(Arrays.copyOf(downloadFilesSet.toArray(), downloadFilesSet.toArray().length, String[].class), local_Path);
                            }

                            break;

                        case "4":
                            /**
                             *This code was inspired by https://www.geeksforgeeks.org/java-program-to-display-all-the-directories-in-a-directory/
                             */
                            System.out.println("4. list directories & files on local machine\n");
                            String curDirStr = "";
                            File curDir = null;
                            boolean askAgain;
                            do {
                                curDirStr = FTPUtils.getInputFromUser(scan, "Enter local Location", "curDir");
                                curDir = new File(curDirStr);
                                if (!remoteConnection.checkLocalDirectoryExists(curDirStr)) {
                                    System.out.println("-- Please enter valid Directory Path --");
                                    askAgain = true;
                                } else {
                                    askAgain = false;
                                }
                            } while (askAgain);

                            File[] filesList = curDir.listFiles();
                            for (int i = 0; i < filesList.length; i++) {
                                System.out.println(filesList[i].getName());
                            }
                            break;

                        case "5":
                            logger.debug("starting functionality - Put file onto remote server");

                            System.out.println("5. Put file onto remote server\n");

                            String localFilePath = FTPUtils.getInputFromUser(scan, "Enter Local file path, that you want to upload", "localFilePath");
                            String remotePath = FTPUtils.getInputFromUser(scan, "Enter Destination", "remotePath");
                            remoteConnection.uploadSingleFile(localFilePath, remotePath);

                            logger.debug("End of functionality - Put file onto remote server");
                            break;

                        case "6":
                            logger.debug("starting functionality - Put multiple files on remote server");

                            System.out.println("6. Put multiple files on remote server\n");
                            String remote_Path = FTPUtils.getInputFromUser(scan, "Enter Remote Destination", "remote_Path");
                            if(remoteConnection.checkRemoteDirectoryExists(remote_Path)){
                                String userOpt = FTPUtils.getInputFromUser(scan, "Would you like to upload the contents of the entire directory? y/n\n", "userOption");
                                if ("y".equalsIgnoreCase(userOpt)) {
                                    String localDirPath = FTPUtils.getInputFromUser(scan, "Enter local Directory Path", "dirPath");
                                    if (remoteConnection.uploadDirectory(localDirPath, remote_Path)) {
                                        System.out.println(localDirPath + " successfully uploaded to " + remote_Path);
                                    }
                                    else {
                                        System.out.println("Failed to upload " + localDirPath);
                                    }
                                } else {
                                    Set<String> uploadFilesSet = new HashSet<>();
                                    boolean uploadMore;
                                    boolean isValidPath;
                                    String uploadMoreFiles;

                                do {
                                    uploadMore = false;

                                        String local_Path = FTPUtils.getInputFromUser(scan, "Enter Local file path, that you want to upload", "local_Path");
                                        File localFile = new File(local_Path);
                                        if (localFile.isFile()) {
                                            uploadFilesSet.add(local_Path);
                                            isValidPath = true;
                                        } else {
                                            System.out.println("-- Error: The local path provided is not valid.--\n");
                                            isValidPath = false;
                                        }
                                        if (isValidPath) {
                                            uploadMoreFiles = FTPUtils.getInputFromUser(scan, "Do you want to upload another File ? (y/n)", "uploadMoreFiles");
                                        } else {
                                            uploadMoreFiles = FTPUtils.getInputFromUser(scan, "Try again? (y/n)", "uploadMoreFiles");
                                        }

                                        if ("y".equals(uploadMoreFiles)) {
                                            uploadMore = true;
                                        }
                                    } while (uploadMore);
                                    remoteConnection.uploadMultipleFiles(Arrays.copyOf(uploadFilesSet.toArray(), uploadFilesSet.toArray().length, String[].class), remote_Path);

                                    logger.debug("End of functionality - Put multiple files on remote server");
                                }
                            } else {
                                System.out.println("-- Error: given remote location does not exist. Check and try again later. --");
                            }
                            break;

                        case "7":
                            logger.debug("starting functionality - Create New Directory on Remote Server");

                            System.out.println("7. Create New Directory on Remote Server\n");
                            boolean tryCreatingDirAgain;
                            do {
                                tryCreatingDirAgain = false;
                                String dirName = FTPUtils.getInputFromUser(scan, "Enter Directory Name: (relative path or absolute path)", "dirName");

                                boolean newDirStatus = remoteConnection.createNewDirectory(dirName);
                                if (newDirStatus) {
                                    logger.debug("Directory created Successfully");
                                    System.out.println("Directory created Successfully.\n");
                                } else {
                                    logger.debug("Error occurred - could not create New Directory in remote server");
                                    System.out.println("-- Error: could not create New Directory in remote server --\n" +
                                            "Directory may already exist. Do you want try creating Directory again ? (y/n)");
                                    String tryAgain = scan.nextLine();
                                    if ("y".equals(tryAgain)) {
                                        tryCreatingDirAgain = true;
                                    }
                                }
                            } while (tryCreatingDirAgain);

                            logger.debug("End of functionality - Create New Directory on Remote Server");
                            break;

                        case "8":
                            System.out.println("8. Delete directories from remote server\n");
                            String dirPath = FTPUtils.getInputFromUser(scan, "Please enter the path to the remote directory you would like to delete", "Path");
                            if (remoteConnection.deleteDirectory(dirPath)) {
                                System.out.println("Directory deleted Successfully. \n");
                            } else {
                                System.out.println("-- Error: could not delete New Directory in remote server --");
                            }
                            break;

                        case "9":
                            System.out.println("9. Change permissions on remote server\n");
                            String inputPath = FTPUtils.getInputFromUser(scan, "Absolute Path to file or directory you want to change permission of", "inputPath");
                            String permissions = FTPUtils.getInputFromUser(scan, "Please enter the the new file permissions (e.g. 777, 600, 444)", "permissions");
                            remoteConnection.changePermission(permissions, inputPath);
                            break;

                        case "10":
                            System.out.println("10. Rename file on remote server\n");
                            String oldName = FTPUtils.getInputFromUser(scan, "Enter path of file to rename", "oldName");
                            String newName = FTPUtils.getInputFromUser(scan, "Enter new name", "newName");
                            boolean success = remoteConnection.renameRemoteFile(oldName, newName);
                            if (success) {
                                System.out.println(oldName + " was renamed to " + newName);
                            } else {
                                System.out.println("-- Error Failed to rename: " + oldName +" --");
                            }
                            break;

                        case "11":
                            System.out.println("11. Rename file on local machine\n");
                            String oldLocalName = FTPUtils.getInputFromUser(scan, "Enter Absolute path of file to rename", "oldLocalName");
                            String newLocalName = FTPUtils.getInputFromUser(scan, "Enter new name with Absolute path", "newLocalName");
                            if (remoteConnection.checkLocalDirectoryExists(newLocalName)) {
                                System.out.println(newLocalName + " already exists");
                            } else {
                                boolean local_rename_success = remoteConnection.renameLocalFile(oldLocalName, newLocalName);
                                if (local_rename_success) {
                                    System.out.println("[" + oldLocalName + "] was renamed to: [" + newLocalName + "]");
                                } else {
                                    System.out.println("-- Error: Failed to rename: [" + oldLocalName + "] --");
                                }
                            }
                            break;

                        case "12":
                            System.out.println("12. Copy directories on remote server\n");
                            String sourceDir = FTPUtils.getInputFromUser(scan, "Enter path of source directory to copy", "sourceDir");
                            String desDir = FTPUtils.getInputFromUser(scan, "Enter name of new copy", "desDir");
                            while (sourceDir.equals(desDir)) {
                                System.out.println("Copy cannot have the same name");
                                sourceDir = FTPUtils.getInputFromUser(scan, "Enter name of directory to copy", "sourceDir");
                                desDir = FTPUtils.getInputFromUser(scan, "Enter name of new copy", "desDir");
                            }
                            remoteConnection.copyDirectory(sourceDir, desDir);
                            break;

                        case "13":
                            System.out.println("13. Delete file from remote server\n");
                            String filePath = FTPUtils.getInputFromUser(scan, "Please enter the remote file path you would like to delete", "filePath");
                            if (remoteConnection.deleteFile(filePath)) {
                                logger.debug("File deleted successfully.");
                                System.out.println("File deleted Successfully. \n");
                            } else {
                                logger.debug("-- Error: could not delete file in remote server --");
                                System.out.println("-- Error: could not delete file in remote server --");
                            }
                            break;

                        case "14":
                            System.out.println("14. Search file on remote server\n");
                            String searchFilePath = "";
                            boolean askOptAgain;
                            do {
                                searchFilePath = FTPUtils.getInputFromUser(scan, "Please enter the folder path to the remote directory", "searchFilePath");
                                if (!remoteConnection.checkRemoteDirectoryExists(searchFilePath)) {
                                    System.out.println("-- Please enter valid remote Directory Path --");
                                    askOptAgain = true;
                                } else {
                                    askOptAgain = false;
                                }
                            } while (askOptAgain);

                            String searchOption = FTPUtils.getInputFromUser(scan, "1. Search File With Keyword\n" +
                                    "2. Search File ending with Extension\n" +
                                    "Please Choose Options \"1 or 2\"", "searchOption");
                            if ("1".equals(searchOption)) {
                                String keyword = FTPUtils.getInputFromUser(scan, "Enter Search Keyword", "keyword");
                                int fileCount = remoteConnection.searchFilesWithKeyword(searchFilePath, keyword);
                                System.out.println("The number of files found with keyword :[" + keyword + "] are [" + fileCount + "]");
                            } else if ("2".equals(searchOption)) {
                                String extension = FTPUtils.getInputFromUser(scan, "Enter Search File Extension", "extension");
                                int fileCount = remoteConnection.searchFilesWithExtension(searchFilePath, extension);
                                System.out.println("The number of files found with extension :[" + extension + "] are [" + fileCount + "]");
                            } else {
                                logger.debug("-- Error: Invalid Search Option Selected! --");
                                System.out.println("-- Error: Invalid Search Option Selected! --");
                            }
                            break;

                        case "15":
                            System.out.println("15. Search file on local machine\n");
                            userOption = FTPUtils.getInputFromUser(scan, "Enter local file name", "userOption");
                            String local_file_path = FTPUtils.getInputFromUser(scan, "Enter local file path", "local_file_path:");
                            File theDir = new File(local_file_path);

                            if (!theDir.isDirectory()) {
                                System.out.println("-- Error: Not valid local Directory path. --");
                            }
                            if(remoteConnection.checkLocalDirectoryExists(local_file_path)){
                                remoteConnection.searchFile(userOption, theDir);
                            }
                            break;

                        case "16":
                            System.out.println("16. Log off from the Server\n");
                            logger.info("Going to disconnect from the server");
                            remoteConnection.disconnect();
                            repeatProcess = false;
                            connected = false;
                            logger.info("Disconnected from the server successfully");
                            break;

                        default:
                            logger.info("Please Select a Valid Option");
                            break;
                    }
                    String repeat = "";
                    if (repeatProcess) {
                        System.out.println("Do you want to choose other option? (y/n): ");
                        repeat = scan.nextLine();
                    }
                    if ("n".equalsIgnoreCase(repeat)) {
                        repeatProcess = false;
                    } else {
                        showOptions();
                    }
                }
            } else {
                System.out.println("-- Error: Could not connect to the Server.--");
                logger.info("Provide HostName, UserName, Password and select Protocol, when prompted.");
            }
        } catch (Exception e) {
            logger.error("Something went wrong.");
            logger.error(e.getMessage());
            logger.error("See the message above.");
        } finally {
            if (remoteConnection != null && connected) {
                remoteConnection.disconnect();
            }
        }
        logger.debug("Main Method Execution -> Ends");
    }

}
