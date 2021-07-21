package ftp.core;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.*;
import java.io.IOException;
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

    public boolean getRemoteFile(String remoteDirName, String localPath) throws IOException {

        File downloadToLocal = new File(localPath + "/" + remoteDirName);
//        File downloadToLocal = new File("C:/Users/Minji/Documents/PSU 2020-2021/Summer/CS 410 MODERN " +
//                "AGILE AND OTHER XP SOFT/Filezilla - Local Folder for testing" + "/" + remoteDirName);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadToLocal));

        //returns true if file transfer was successful
        boolean transferSuccess = client.retrieveFile(remoteDirName, outputStream);
        outputStream.close();
        return transferSuccess;

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
    public boolean deleteDirectory(String dirPath) throws FTPClientException {
        try {
            return client.removeDirectory(dirPath);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean uploadSingleFile(String localFilePath, String remoteFilePath) throws IOException {
        File localFile = new File(localFilePath);

        InputStream inputStream = new FileInputStream(localFile);
        try {
            client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            return client.storeFile(remoteFilePath, inputStream);
        } catch (IOException e) {
            System.out.println("-- Something went wrong when trying to upload the file. --\n");
        } finally {
            inputStream.close();
        }
        return false;

    }
}

