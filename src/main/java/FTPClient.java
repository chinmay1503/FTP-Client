import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import ftp.core.RemoteConnectionFactory;
import ftp.core.RemoteConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ftp.core.ClientCredentials;

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
     * @param promptDialog [String] - Prompt to display before providing the options.
     */
    public static void showConnectionOptions(String promptDialog) {

        try {
            logger.info("creating clientCredentials.json file if it does not exsist");
            File myObj = new File("ClientCredentials.json");
            if (myObj.createNewFile()) {
                FileWriter myWriter = new FileWriter("ClientCredentials.json");
                myWriter.write("[]");
                myWriter.close();
                logger.info("created the file");
            }
        } catch (IOException e) {
            logger.info("Error occurred while creating the file");
            e.printStackTrace();
        }

        boolean repeatConnectOptions = false;
        Scanner scan = new Scanner(System.in);
        logger.debug("Prompting the user to select connect options.");

        System.out.println(promptDialog+"\n");
        String connectOptions = getInputFromUser(scan, "1. Use saved Connections \t"
                + " 2. Enter New user Credentials and Save them \t"
                + " 3. Exit\n"
                + "Enter your Option", "connectOptions");
        do {
            switch (connectOptions) {
                case "1":
                    System.out.println("List of all saved connections (Choose from below connections):\n");
                    ArrayList<String> selectedUserDetails = listAllUserCredentials();
                    if (selectedUserDetails.isEmpty()) {
                        connectOptions = "2";
                        repeatConnectOptions = true;
                    } else {
                        userName = selectedUserDetails.get(0);
                        hostName = selectedUserDetails.get(2);
                        protocol = selectedUserDetails.get(3);
                        System.out.println("Connecting to:\nUserName: " + userName + "\tserver: " + hostName + "\tProtocol: " + protocol);
                        password = getInputFromUser(scan, "Password", "Password");
                    }
                    break;

                case "2":
                    System.out.println("HostName: (Eg: 127.0.0.1)");
                    hostName = scan.nextLine();
                    if (isNullOrEmpty(hostName))
                        hostName = "127.0.0.1";

                    userName = getInputFromUser(scan, "UserName", "UserName");
                    password = getInputFromUser(scan, "Password", "Password");

                    String protocolNum = getInputFromUser(scan, "Select Protocol: 1. FTP \t 2. SFTP", "protocolNum");
                    if (protocolNum.equals("1"))
                        protocol = "FTP";
                    else
                        protocol = "SFTP";

                    repeatConnectOptions = false;
                    break;

                case "3":
                    System.exit(0);
                    break;

                default:
                    connectOptions = getInputFromUser(scan,
                            "please enter Correct option, choose form following.\n"
                                    + "1. Use saved Connections \t 2. Enter New user Credentials and Save them\n"
                                    + "Enter your Option",
                            "connectOptions");
                    repeatConnectOptions = true;
                    break;
            }
        }while (repeatConnectOptions);

    }

    /**
     * Main method for FTPClient class.
     *
     * @throws Exception This method can throw many Exception's. so mentioning parent Exception.
     */
    public static void main(String[] args) throws Exception {

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
            do{
                connected_status = remoteConnection.connect(hostName, userName, password);

                if(connected_status == 1){
                    connected = true;
                } else if(connected_status == 0){
                    connected = false;
                    showConnectionOptions("Try Other options");
                } else{
                    System.exit(0);
                }
            } while (!connected);


            if (connected) {
                storeClientCredentials(hostName, userName, password, protocol);
                System.out.println("\n--- Connected to Remote FTP Server ---\n");
                showOptions();

                // Provide respective functionality to user, based on their choice.
                while (repeatProcess) {
                    userOption = getInputFromUser(scan, "Choose your Option", "userOption");
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
                                    remoteFileUserInput = getInputFromUser(scan, "Enter File Name to download from Remote Server", "remoteFileUserInput");
                                    promptForRemoteFile = remoteConnection.checkFileExists(remoteFileUserInput);
                                    if (!promptForRemoteFile) {
                                        System.out.println("-- Error: could not locate Directory with the name " + remoteFileUserInput +
                                                " in remote server --");
                                    }
                                } while (!promptForRemoteFile);

                                do {
                                    localPathUserInput = getInputFromUser(scan, "\"Enter File Path to download to", "localPathUserInput");
                                    promptForLocalPath = remoteConnection.checkLocalDirectoryExists(localPathUserInput);
                                    if (!promptForLocalPath) {
                                        System.out.println("-- Error: could not locate Directory with the name " + localPathUserInput +
                                                " in local computer --");
                                    }
                                } while (!promptForLocalPath);
                                remoteConnection.downloadSingleFile(localPathUserInput, remoteFileUserInput);

                                break;

                            case "3":
                                System.out.println("3. Get multiple file from remote server\n");
                                String userOptions = getInputFromUser(scan, "Would you like to download the contents of the entire directory? y/n\n", "userOption");

                                //Call method from case 11 to download entire directory
                                if ("y".equalsIgnoreCase(userOptions)) {
                                    System.out.println("Enter Destination to download to: ");

                                }
                                //Prompt and download each file from the remote path
                                else {
                                    String local_Path = getInputFromUser(scan, "Enter Destination to download to", "local_Path");
                                    Set<String> downloadFilesSet = new HashSet<>();
                                    boolean downloadMore;

                                    do {
                                        downloadMore = false;
                                        do {
                                            String remote_Path = getInputFromUser(scan, "Enter remote path, where you wish to download from", "remote_Path");
                                            promptForRemoteFile = remoteConnection.checkFileExists(remote_Path);

                                            if (!promptForRemoteFile) {
                                                System.out.println("-- Error: could not locate Directory with the name " + remote_Path +
                                                        " in remote server --");
                                            } else {
                                                downloadFilesSet.add(remote_Path);
                                            }
                                        } while (!promptForRemoteFile);

                                        String downloadMoreFiles = getInputFromUser(scan, "Do you want to download another File? (y/n)", "downloadMoreFiles");
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
                                File curDir = new File(".");
                                File[] filesList = curDir.listFiles();
                                for (int i = 0; i < filesList.length; i++) {
                                    if (filesList[i].isDirectory()) {
                                        System.out.println(filesList[i].getName() + " this is a directory");

                                    } else {

                                        System.out.println(filesList[i].getName() + " this is a file");
                                    }
                                }
                                break;

                            case "5":
                                logger.debug("starting functionality - Put file onto remote server");

                                System.out.println("5. Put file onto remote server\n");

                            String localFilePath = getInputFromUser(scan, "Enter Local file path, that you want to upload", "localFilePath");
                            String remotePath = getInputFromUser(scan, "Enter Destination", "remotePath");
                            remoteConnection.uploadSingleFile(localFilePath, remotePath);

                                logger.debug("End of functionality - Put file onto remote server");
                                break;

                            case "6":
                                logger.debug("starting functionality - Put multiple files on remote server");

                                System.out.println("6. Put multiple files on remote server\n");

                            String remote_Path = getInputFromUser(scan, "Enter Destination", "remote_Path");

                            Set<String> uploadFilesSet = new HashSet<>();
                            boolean uploadMore;
                            boolean isValidPath;
                            String uploadMoreFiles;

                            do {
                                uploadMore = false;

                                String local_Path = getInputFromUser(scan, "Enter Local file path, that you want to upload", "local_Path");
                                File localFile = new File(local_Path);
                                if(localFile.isFile()) {
                                    uploadFilesSet.add(local_Path);
                                    isValidPath = true;
                                }else {
                                    System.out.println("Error: The local path provided is not valid.\n");
                                    isValidPath = false;
                                }
                                if(isValidPath) {
                                    uploadMoreFiles = getInputFromUser(scan, "Do you want to upload another File ? (y/n)", "uploadMoreFiles");
                                } else {
                                    uploadMoreFiles = getInputFromUser(scan, "Try again? (y/n)", "uploadMoreFiles");
                                }

                                if (uploadMoreFiles.equals("y")) {
                                    uploadMore = true;
                                }
                            } while (uploadMore);
                            remoteConnection.uploadMultipleFiles(Arrays.copyOf(uploadFilesSet.toArray(), uploadFilesSet.toArray().length, String[].class), remote_Path);

                                logger.debug("End of functionality - Put multiple files on remote server");
                                break;

                            case "7":
                                logger.debug("starting functionality - Create New Directory on Remote Server");

                                System.out.println("7. Create New Directory on Remote Server\n");
                                boolean tryCreatingDirAgain;
                                do {
                                    tryCreatingDirAgain = false;
                                    String dirName = getInputFromUser(scan, "Enter Directory Name: (relative path or absolute path)", "dirName");

                                    boolean newDirStatus = remoteConnection.createNewDirectory(dirName);
                                    if (newDirStatus) {
                                        logger.info("Directory created Successfully");
                                        System.out.println("* Directory created Successfully. *\n");
                                    } else {
                                        logger.info("Error occurred - could not create New Directory in remote server");
                                        System.out.println("-- Error: could not create New Directory in remote server --\n" +
                                                "Directory may already exist. Do you want try creating Directory again ? (y/n)");
                                        String tryAgain = scan.nextLine();
                                        if (tryAgain.equals("y")) {
                                            tryCreatingDirAgain = true;
                                        }
                                    }
                                } while (tryCreatingDirAgain);

                                logger.debug("End of functionality - Create New Directory on Remote Server");
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

                            case "9":
                                System.out.println("9. Change permissions on remote server\n");
                                String inputPath = getInputFromUser(scan, "Absolute Path to file or directory you want to change permission of", "inputPath");
                                String permissions = getInputFromUser(scan, "Please enter the the new file permissions (e.g. 777, 600, 444)", "permissions");
                                remoteConnection.changePermission(permissions, inputPath);
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

                            case "11":
                                System.out.println("10. Rename file on local machine\n");
                                String oldLocalName = getInputFromUser(scan, "Enter name of file to rename", "oldLocalName");
                                String newLocalName = getInputFromUser(scan, "Enter new name", "newLocalName");
                                boolean local_rename_success = remoteConnection.renameLocalFile(oldLocalName, newLocalName);
                                if (local_rename_success) {
                                    System.out.println("[" + oldLocalName + "] was renamed to: [" + newLocalName + "]");
                                    logger.info("[" + oldLocalName + "] was renamed to: [" + newLocalName + "]");
                                } else {
                                    System.out.println("Failed to rename: [" + oldLocalName + "]");
                                    logger.info("Failed to rename: [" + oldLocalName + "]");
                                }
                                break;

                            case "12":
                                System.out.println("12. Copy directories on remote server\n");
                                String sourceDir = getInputFromUser(scan, "Enter name of source directory to copy", "sourceDir");
                                String desDir = getInputFromUser(scan, "Enter name of new copy", "desDir");
                                while (sourceDir.equals(desDir)) {
                                    System.out.println("Copy cannot have the same name");
                                    sourceDir = getInputFromUser(scan, "Enter name of directory to copy", "sourceDir");
                                    desDir = getInputFromUser(scan, "Enter name of new copy", "desDir");
                                }
                                remoteConnection.copyDirectory(sourceDir, desDir);
                                break;

                            case "13":
                                System.out.println("13. Delete file from remote server\n");
                                String filePath = getInputFromUser(scan, "Please enter the file path to the remote directory you would like to delete", "filePath");
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
                                String searchFilePath = getInputFromUser(scan, "Please enter the folder path to the remote directory", "searchFilePath");
                                String searchOption = getInputFromUser(scan, "1. Search File With Keyword\n" +
                                        "2. Search File ending with Extension\n" +
                                        "Please Choose Options \"1 or 2\"", "searchOption");
                                if (searchOption.equals("1")) {
                                    String keyword = getInputFromUser(scan, "Enter Search Keyword", "keyword");
                                    int fileCount = remoteConnection.searchFilesWithKeyword(searchFilePath, keyword);
                                    logger.info("The number of files found with keyword :[" + keyword + "] are [" + fileCount + "]");
                                } else if (searchOption.equals("2")) {
                                    String extension = getInputFromUser(scan, "Enter Search File Extension", "extension");
                                    int fileCount = remoteConnection.searchFilesWithExtension(searchFilePath, extension);
                                    logger.info("The number of files found with extension :[" + extension + "] are [" + fileCount + "]");
                                } else {
                                    logger.debug("-- Error: Invalid Search Option Selected! --");
                                    System.out.println("-- Error: Invalid Search Option Selected! --");
                                }
                                break;

                            case "15":
                                System.out.println("15. Search file on local machine\n");
                                System.out.println("coming soon ... \n");
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
                        }
                    }
                } else {
                    System.out.println("Error: Could not connect to the Server.");
                    logger.info("Provide HostName, UserName, Password and select Protocol, when prompted.");
                }
            } finally {
                if (remoteConnection != null && connected) {
                    remoteConnection.disconnect();
                }
            }
            logger.debug("Main Method Execution -> Ends");
        }

    /**
     * This function prints all saved connections on console. and prompts user to select the connection details,
     * that the user wants to use to connect to the remote server.
     *
     * @return selectedConnectionDetails [ArrayList<String>]  -
     *              A list of selected connection information is returned.
     */
    private static ArrayList<String> listAllUserCredentials() {
        ArrayList<ArrayList<String> > aList =
                new ArrayList<>();
        int userIndex = 0;
        int i = 1;
        try{
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new FileInputStream("clientCredentials.json");
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ClientCredentials.class);
            List<ClientCredentials> allClients = mapper.readValue(inputStream, type); // [obj, obj]

            for(ClientCredentials cc : allClients){
                ArrayList<String> a1 = new ArrayList<>();
                a1.add(cc.getUserName());
                a1.add(cc.getPassword());
                a1.add(cc.getServer());
                a1.add(cc.getProtocol());
                aList.add(a1);
                System.out.println(i + ". userName: " + cc.getUserName() + "\tserver: " + cc.getServer() + "\tProtocol: " + cc.getProtocol());
                i = i + 1;
            }
            if(i == 1){
                System.out.println("Sorry, No saved Connections. You will have to enter all Credentials (choose below option)");
            }
            System.out.println(i + ". None of the above. Enter New Credentials");
            Scanner scan = new Scanner(System.in);
            userIndex = Integer.parseInt(getInputFromUser(scan, "\nEnter Option", "userIndex"));
            inputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        if(userIndex == i){
            return new ArrayList<>();
        }
        return aList.get(userIndex - 1);
    }

    /**
     * This method is used to save client credentials to `clientCredentials.json` file, if its a new client login.
     *
     * @param hostName - hostname, can be (127.0.0.1) or any other.
     * @param userName - registered client user name.
     * @param password - password to connect to server.
     * @param protocol - selected protocol (either FTP or SFTP).
     */
    private static void storeClientCredentials(String hostName, String userName, String password, String protocol) {
        logger.debug("starting functionality - Store new client credentials");
        boolean newClient = isNewClient(userName);
        if (newClient) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new FileInputStream("clientCredentials.json");
                JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ClientCredentials.class);
                List<ClientCredentials> allClients = mapper.readValue(inputStream, type); // [obj, obj]

                ClientCredentials newClientData = new ClientCredentials(userName, password, hostName, protocol);
                allClients.add(newClientData);
                mapper.writeValue(new File("clientCredentials.json"), allClients);

                inputStream.close();
                logger.info("new client credentials are stored.");
            } catch (IOException e) {
                logger.info("Error Occurred - error occurred while trying to store new user credentials.");
                e.printStackTrace();
            }
        }
        logger.debug("End of functionality - Store new client credentials");
    }

    /**
     * This method is used to check if client credentials are already present in the `clientCredentials.json` file
     *
     * @param userName - registered client user name.
     * @return [boolean] - return true if credentials are not present else return false if client details are already saved.
     */
    private static boolean isNewClient(String userName) {
        logger.debug("starting functionality - checking if its a new client login.");

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new FileInputStream("clientCredentials.json");
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ClientCredentials.class);
            List<ClientCredentials> clients = mapper.readValue(inputStream, type); // [obj, obj]
            for (ClientCredentials cc : clients) {
                if (cc.getUserName().equals(userName)) {
                    logger.info("Client details already saved.");
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Its a new Client.");
        logger.debug("End of functionality - checking if its a new client login.");
        return true;
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
