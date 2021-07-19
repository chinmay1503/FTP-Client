package ftp.core;

import org.apache.commons.net.ftp.FTPClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

    public boolean createNewDirectory(String dirName) {
        try{
            boolean val = client.makeDirectory(dirName);
            return val;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getClientReplyCode() throws IOException{
        int returnCode = client.getReplyCode();
        return returnCode;
    }

    public boolean checkDirectoryExists(String dirPath) throws IOException {
        client.changeWorkingDirectory(dirPath);
        int returnCode = client.getReplyCode();
        if (returnCode == 550) {
            return false;
        }
        return true;
    }

    public boolean getRemoteFile(String remoteDirName, String localPath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(localPath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        getRemoteFile(remoteDirName, out);
        return true;
    }

    public boolean getRemoteFile(String remoteDirName, OutputStream localPath) throws IOException {
        client.retrieveFile(remoteDirName, localPath);
        return true;
    }
}

