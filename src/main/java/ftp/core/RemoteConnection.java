package ftp.core;

import org.apache.commons.net.ftp.FTPClient;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

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

    boolean renameRemoteFile(String oldName, String newName) throws FTPClientException;

    boolean copyDirectory(String toCopy, String newDir) throws FTPClientException, IOException;

    int searchFilesWithKeyword(String filePath, String keyword) throws FTPClientException;

    int searchFilesWithExtension(String filePath, String extension) throws FTPClientException;

    boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException;

    void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir) throws IOException;

    void uploadDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir, String remoteParentDir) throws IOException;

    boolean uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath) throws IOException;
}
