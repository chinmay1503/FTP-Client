import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import ftp.core.RemoteConnectionFactory;
import ftp.core.RemoteConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import  ftp.core.ClientCredentials;
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

    /**
     * This method is used to print the options for user's to choose from.
     */
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
                "14. Search file on remote server\n" +
                "15. Search file on local machine\n" +
                "16. Log off from the Server\n" +
                "\n" );
    }

    /**
     * Main method for FTPClient class.
     *
     * @throws Exception
     *          This method can throw many Exception's. so mentioning parent Exception.
     */
    public static void main(String[] args) throws Exception {

        logger.debug("Main method Execution -> Starts");

        try (Scanner scan = new Scanner(System.in)) {
            String userOption;
            boolean repeatProcess = true;

            System.out.println("HostName: (Eg: 127.0.0.1)");
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
                storeClientCredentials(hostName, userName, password, protocol);
                System.out.println("\n--- Connected to Remote FTP Server ---\n");
                showOptions();

                // Provide respective functionality to user, based on their choice.
                while (repeatProcess) {
                    System.out.println("Choose your Option : ");
                    userOption = scan.nextLine();

                    switch (userOption) {
                        case "1":
                            System.out.println("1. list directories & files on remote server\n");
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

                            System.out.println("Enter Local file path, that you want to upload");
                            String localFilePath = scan.nextLine();
                            System.out.println("Enter Destination");
                            String remotePath = scan.nextLine();
                            remoteConnection.uploadSingleFile(localFilePath, remotePath);

                            logger.debug("End of functionality - Put file onto remote server");
                            break;

                        case "6":
                            logger.debug("starting functionality - Put multiple files on remote server");

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

                            logger.debug("End of functionality - Put multiple files on remote server");
                            break;

                        case "7":
                            logger.debug("starting functionality - Create New Directory on Remote Server");

                            System.out.println("7. Create New Directory on Remote Server\n");
                            boolean tryCreatingDirAgain;
                            do {
                                tryCreatingDirAgain = false;
                                System.out.println("Enter Directory Name: (relative path or absolute path)");
                                String dirName = scan.nextLine();
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
                            System.out.println("12. Copy directory from remote server\n");
                            System.out.println("coming soon ... \n");
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

    /**
     * This method is used to save client credentials to `clientCredentials.json` file, if its a new client login.
     * @param hostName - hostname, can be (127.0.0.1) or any other.
     * @param userName - registered client user name.
     * @param password - password to connect to server.
     * @param protocol - selected protocol (either FTP or SFTP).
     */
    private static void storeClientCredentials(String hostName, String userName, String password, String protocol) {
        logger.debug("starting functionality - Store new client credentials");
        boolean newClient = isNewClient(userName);
        if(newClient){
            try{
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new FileInputStream(new File("target\\classes\\clientCredentials.json"));
                JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ClientCredentials.class);
                List<ClientCredentials> allClients = mapper.readValue(inputStream, type); // [obj, obj]

                ClientCredentials newClientData = new ClientCredentials( userName, password, hostName, protocol);
                allClients.add(newClientData);
                mapper.writeValue(new File("target\\classes\\clientCredentials.json"), allClients);

                inputStream.close();
                logger.info("new client credentials are stored.");
            } catch (IOException e){
                logger.info("Error Occurred - error occurred while trying to store new user credentials.");
                e.printStackTrace();
            }
        }
        logger.debug("End of functionality - Store new client credentials");
    }

    /**
     * This method is used to check if client credentials are already present in the `clientCredentials.json` file
     * @param userName - registered client user name.
     * @return [boolean] - return true if credentials are not present else return false if client details are already saved.
     */
    private static boolean isNewClient(String userName) {
        logger.debug("starting functionality - checking if its a new client login.");

        try{
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new FileInputStream(new File("target\\classes\\clientCredentials.json"));
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ClientCredentials.class);
            List<ClientCredentials> clients = mapper.readValue(inputStream, type); // [obj, obj]
            for(ClientCredentials cc : clients){
                if(cc.getUserName().equals(userName)){
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
