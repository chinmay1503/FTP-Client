package ftp.core;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * FTPConnection class - this class has all the method implementations that is used by FTPClient.
 */
public class FTPConnection implements RemoteConnection {

    private FTPClient client;
    private static final Logger logger = LogManager.getLogger(FTPClient.class);

    public boolean connect(String hostName, String userName, String password) {
        try {
            client = new FTPClient();
            client.connect(hostName, 21);
            boolean login = client.login(userName, password);
            if (login) {
                client.enterLocalPassiveMode();
                client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            } else {
                System.err.println("Username or password is incorrect");
            }
            return login;
        } catch (SocketException e) {
            System.err.println("Error occurred when trying to connect to Server");
        } catch (IOException e) {
            System.err.println("Error due to IOException");
        }
        return false;
    }

    public void disconnect() throws FTPClientException {
        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to create new Directory on remote server.
     * @param dirName - name of the directory.
     * @return [boolean] - returns true if successfully created a directory on remote server, else return false.
     * @throws IOException - can throw exception while handling files.
     */
    public boolean createNewDirectory(String dirName) throws IOException {
        try{
            return client.makeDirectory(dirName);
        } catch (SocketException e) {
            System.out.println("Something went wrong, when trying to create directory \""+dirName+"\"\n" +
                    "Give valid Directory path/ name .");
        }
        return false;
    }

    /**
     * This method is used to retrieve a reply code of current FTP client connection.
     * @return [int] - reply code of current client connection.
     */
    public int getClientReplyCode() {
        return client.getReplyCode();
    }

    /**
     * This method is used to check if directory exists or not.
     * @param dirPath - the remote directory path.
     * @return [boolean] - returns true if exists else return false.
     * @throws FTPClientException - can throw IOException while handling files.
     */
    @Override
    public boolean checkDirectoryExists(String dirPath) throws FTPClientException {
        try {
            client.changeWorkingDirectory(dirPath);
            int returnCode = client.getReplyCode();
            if (returnCode == 550) {
                return false;
            }
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
        return true;
    }

    @Override
    public void getCurrentRemoteDirectory() throws FTPClientException {
        try {
            System.out.println(client.printWorkingDirectory());
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public void listCurrentDirectory() throws FTPClientException {
        try {
            FTPFile[] ftpFiles = client.listFiles();
            for (FTPFile file : ftpFiles) {
                System.out.println(file.getName());
            }
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) throws FTPClientException {
        try {
            return client.deleteFile(filePath);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean deleteDirectory(String dirPath) throws FTPClientException {
        try {
            return client.removeDirectory(dirPath);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to put a single file on the remote server, using FTP protocol.
     * @param localFilePath - this is the path on local system
     * @param remotePath - this is the path on remote server.
     * @throws IOException - can throw IOException, while handling files.
     * @throws FTPClientException - throws this exception while checking if file exist or not opn remote server.
     */
    @Override
    public void uploadSingleFile(String localFilePath, String remotePath) throws IOException, FTPClientException {
        boolean uploaded = false;
        String remoteFilePath;

        File localFile = new File(localFilePath);
        if(localFile.isFile()){
            remoteFilePath = remotePath + "/" + localFile.getName();
            if(checkDirectoryExists(remotePath)){
                InputStream inputStream = new FileInputStream(localFile);
                try {
                    client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                    uploaded = client.storeFile(remoteFilePath, inputStream);
                } catch (IOException e) {
                    System.out.println("-- Something went wrong when trying to upload the file. --\n");
                } finally {
                    inputStream.close();
                }
            if (uploaded) {
                logger.info("file upload successful");
                System.out.println("Upload Successful. Uploaded to: " + remoteFilePath );
            } else {
                logger.info("file upload Unsuccessful");
                System.out.println("Error occurred when trying to upload the file: \""
                            + localFilePath + "\" to \"" + remoteFilePath + "\"");
            }
        } else {
                logger.info("Error occurred - The Remote file path provided does not exist.");
                System.out.println("Error: The Remote file path provided does not exist.\n");
            }
        } else {
            logger.info("Error occurred - The local path provided is not valid.");
            System.out.println("Error: The local path provided is not valid.\n");
        }
    }

    /**
     * This method is used to upload multiple files onto remote server.
     * This method
     * @param localPaths [Array] - these are the paths of all the files on local system, that user wants to upload
     * @param remotePath - this is the path on remote server, where user want to upload all those files.
     */
    @Override
    public void uploadMultipleFiles(String[] localPaths, String remotePath){
        System.out.println("local paths --> "+ localPaths);
        try {
            for (String localPath : localPaths) {
                uploadSingleFile(localPath, remotePath);
            }
            logger.info("All files uploaded successfully");
        } catch (IOException | FTPClientException e) {
            logger.info("Error occurred - file upload Unsuccessful - Error while uploading files to Remote server");
            System.out.println("-- Error while uploading files to Remote server --");
        }
    }
}
