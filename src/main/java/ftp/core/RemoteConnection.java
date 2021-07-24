package ftp.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws FTPClientException, IOException;

    int getClientReplyCode() throws FTPClientException;

    void getCurrentRemoteDirectory() throws FTPClientException;

    void listCurrentDirectory() throws FTPClientException;

    boolean checkFileExists(String fileName) throws FTPClientException;

    boolean checkLocalDirectoryExists(String dirPath) throws FileNotFoundException;

    boolean checkDirectoryExists(String dirPath) throws FTPClientException;

    boolean deleteDirectory(String dirPath) throws  FTPClientException;

    void uploadSingleFile(String localFilePath, String remoteFilePath) throws IOException, FTPClientException;

    void uploadMultipleFiles(String[] localPaths, String remotePath);

    boolean downloadSingleFile(String localPath, String remotePath) throws IOException;

    boolean downloadMultipleFiles(String[] localPaths, String remotePath) throws IOException;

}
