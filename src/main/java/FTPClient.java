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
    private static String hostName = "";
    private static String protocol = "";
    private static String userName = "";
    private static String password = "";

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
                "14. Log off from the Server\n" +
                "\n" );
    }

    public static void showConnectionOptions(String promptDialog){
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
     * @throws Exception
     *          This method can throw many Exception's. so mentioning parent Exception.
     */
    public static void main(String[] args) throws Exception {

        String userOption;
        boolean repeatProcess = true;
        boolean connected = false;
        int connected_status = 0;

        logger.debug("Main method Execution -> Starts");

        try (Scanner scan = new Scanner(System.in)) {

            showConnectionOptions("How do you want to connect to remote server ?");

            System.out.println("Connecting to Remote Server...");
            RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
            RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(protocol);
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
                    System.out.println("Choose your Option : ");
                    userOption = scan.nextLine();

                    switch (userOption) {
                        case "1":
                            System.out.println("1. list directories & files on remote server\n");
                            System.out.println("Coming Soon...");
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

                            Set uploadFilesSet = new HashSet<String>();
                            boolean uploadMore;
                            boolean isValidPath = true;
                            String uploadMoreFiles = "n";

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
                                    System.out.println("-- Error: could not create New Directory in remote server --\n");
                                    String tryAgain = getInputFromUser(scan, "Directory may already exist. Do you want try creating Directory again ? (y/n)", "tryAgain");
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
            InputStream inputStream = new FileInputStream(new File("target\\classes\\clientCredentials.json"));
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
