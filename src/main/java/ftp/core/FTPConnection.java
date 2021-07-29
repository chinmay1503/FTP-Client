package ftp.core;

import com.jcraft.jsch.IO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;


public class FTPConnection implements RemoteConnection {

    private FTPClient client;

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

    public boolean createNewDirectory(String dirName) throws IOException {
        try{
            return client.makeDirectory(dirName);
        } catch (SocketException e) {
            System.out.println("Something went wrong, when trying to create directory \""+dirName+"\"\n" +
                    "Give valid Directory path/ name .");
        }
        return false;
    }

    public int getClientReplyCode() {
        return client.getReplyCode();
    }

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
                System.out.println("UPLOADED a file to: " + remoteFilePath );
            } else {
                System.out.println("Error occurred when trying to upload the file: \""
                            + localFilePath + "\" to \"" + remoteFilePath + "\"");
            }
        } else {
                System.out.println("Error: The Remote file path provided does not exist.\n");
            }
        } else {
            System.out.println("Error: The local path provided is not valid.\n");
        }
    }

    @Override
    public void uploadMultipleFiles(String[] localPaths, String remotePath){
        System.out.println("local paths --> "+ localPaths);
        try {
            for (String localPath : localPaths) {
                uploadSingleFile(localPath, remotePath);
            }
        } catch (IOException | FTPClientException e) {
            System.out.println("-- Error while uploading files to Remote server --");
        }
    }

    @Override
    public boolean renameRemoteFile(String oldName, String newName) throws FTPClientException {
        try {
            return client.rename(oldName, newName);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean copyDirectory(String toCopy, String newDir) throws FTPClientException {
        try {
            FTPFile[] ftpFiles = client.listFiles();
            for (FTPFile file : ftpFiles) {
                if (toCopy.equals(file.getName()) && file.isDirectory()) {


                }
            }
            return false;
        } catch (IOException e) {
                throw new FTPClientException(e);
        }
    }
}

