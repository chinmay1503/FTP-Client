package ftp.core;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.google.common.base.Strings.isNullOrEmpty;

public class FTPUtils {
    private static final Logger logger = LogManager.getLogger(FTPUtils.class);

    //Parse file name from filepath
    public static String getFileNameFromRemote(String filePath) {
        if (filePath.length() > 0) {
            File file = new File(filePath);
            return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\") + 1);
        }
        return "";
    }

    public static boolean renameLocalFile(String oldName, String newName) throws FTPClientException {
        try {
            if (org.codehaus.plexus.util.FileUtils.fileExists(oldName)) {
                System.out.println("Going to rename file from :[" + oldName + "] to [" + newName + "]");
                org.codehaus.plexus.util.FileUtils.rename(new File(oldName), new File(newName));
            } else {
                System.out.println("File with name :[" + oldName + "] does not exist");
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getInputFromUser(Scanner scan, String inputMsg, String fieldName) {
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

    /**
     * This function prints all saved connections on console. and prompts user to select the connection details,
     * that the user wants to use to connect to the remote server.
     *
     * @return selectedConnectionDetails [ArrayList<String>]  -
     *              A list of selected connection information is returned.
     */
    public static ArrayList<String> listAllUserCredentials() {
        ArrayList<ArrayList<String> > aList =
                new ArrayList<>();
        Scanner scan = new Scanner(System.in);
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
                a1.add(cc.getEk().getPasswordString());
                a1.add(cc.getServer());
                a1.add(cc.getProtocol());
                aList.add(a1);
                System.out.println(i + ". userName: " + cc.getUserName() + "\tserver: " + cc.getServer() + "\tProtocol: " + cc.getProtocol());
                i = i + 1;
            }
            System.out.println(i + ". None of the above. Enter New Credentials");
            if(i == 1){
                System.out.println("Sorry, No saved Connections. You will have to enter all Credentials.");
                userIndex = i;
            } else {
                do {
                    userIndex = Integer.parseInt(getInputFromUser(scan, "\nEnter Option", "userIndex"));
                    if(userIndex > i){
                        System.out.println("-- Please choose from above options only. --");
                    }
                } while(userIndex > i);
            }
            inputStream.close();
        } catch (IOException e){
            System.out.println("--Error Occurred when trying to list client credentials.--");
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
    public static void storeClientCredentials(String hostName, String userName, String password, String protocol) {
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
            } catch (Exception e) {
                logger.debug("Error Occurred - error occurred while trying to store new user credentials.");
                System.out.println("Error Occurred - error occurred while trying to store new user credentials.");
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
                    logger.debug("Client details already saved.");
                    return false;
                }
            }
        } catch (IOException e) {
            System.out.println("--Error Occurred when trying to read client credentials file.--");
        }
        logger.debug("Its a new Client.");
        logger.debug("End of functionality - checking if its a new client login.");
        return true;
    }

    public static void searchFile(String userOption, File theDir) {
        File[] the_list = theDir.listFiles();

        if (the_list != null) {
            for (int i = 0; i < the_list.length; i++) {
                if (the_list[i].isFile() && the_list[i].getName().equals(userOption)) {
                    System.out.println(the_list[i].getPath() + " found");
                    break;
                }
                if (the_list[i].isDirectory() && the_list[i].getName().equals(userOption))
                {
                    System.out.println(the_list[i].getPath() +" "+ userOption + " is a directory not file");
                    break;
                }
                if (i == (the_list.length - 1)) {
                    System.out.println("file not found in the given location.");
                }
            }
        } else {
            System.out.println("No files found in this folder");
        }
    }

}