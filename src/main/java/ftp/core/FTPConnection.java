package ftp.core;

import com.jcraft.jsch.IO;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ftp.core.FTPUtils.getFileNameFromRemote;


public class FTPConnection implements RemoteConnection {

    private FTPClient client;

    @Override
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

    @Override
    public void disconnect() throws FTPClientException {
        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean createNewDirectory(String dirName) throws IOException {
        try{
            return client.makeDirectory(dirName);
        } catch (SocketException e) {
            System.out.println("Something went wrong, when trying to create directory \""+dirName+"\"\n" +
                    "Give valid Directory path/ name .");
        }
        return false;
    }

    //download a single file from remote server to local
    @Override
    public boolean downloadSingleFile(String localPath, String remotePath) throws IOException, FTPClientException {
        OutputStream outputStream = null;
        try {
            String fileName = getFileNameFromRemote(remotePath);
            File downloadToLocal = new File(localPath + File.separator + fileName);
            outputStream = new BufferedOutputStream(new FileOutputStream(downloadToLocal));
            return client.retrieveFile(remotePath, outputStream);
        } finally {
            if(outputStream != null) {
                outputStream.close();
            }
        }
    }

    //download a single file from remote server to local
    @Override
    public boolean downloadMultipleFiles(String[] remotePaths, String localPath) throws IOException {
        System.out.println("Not implemented yet. Coming soon...");
        return false;
    }

    //Check if file exists in remote directory
    @Override
    public boolean checkFileExists(String filePath) throws IOException {
        FTPFile[] remoteFile = client.listFiles(filePath);
        return remoteFile.length > 0;
    }

    @Override
    public boolean checkLocalDirectoryExists(String dirPath) {
        Path path = Paths.get(dirPath);
        return Files.exists(path);
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
}

