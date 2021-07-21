package ftp.core;

import java.io.IOException;
import java.io.OutputStream;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws FTPClientException, IOException;

    int getClientReplyCode() throws FTPClientException;

    void getCurrentRemoteDirectory() throws FTPClientException;

    void listCurrentDirectory() throws FTPClientException;

    boolean checkDirectoryExists(String dirPath) throws FTPClientException;

    boolean deleteDirectory(String dirPath) throws  FTPClientException;

    boolean uploadSingleFile(String localFilePath, String remoteFilePath) throws IOException;

    boolean getRemoteFile(String remoteDirName, String localPath) throws IOException;

//    boolean getRemoteFile(String remoteDirName, OutputStream localPath) throws IOException;

}
