package ftp.core;

import java.io.IOException;

public interface RemoteConnection {

    int connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws FTPClientException, IOException;

    int getClientReplyCode() throws FTPClientException;

    void getCurrentRemoteDirectory() throws FTPClientException;

    void listCurrentDirectory() throws FTPClientException;

    boolean checkDirectoryExists(String dirPath) throws FTPClientException;

    boolean deleteFile(String filePath) throws FTPClientException;

    boolean deleteDirectory(String dirPath) throws  FTPClientException;

    void uploadSingleFile(String localFilePath, String remoteFilePath) throws IOException, FTPClientException;

    void uploadMultipleFiles(String[] localPaths, String remotePath);

}
