package ftp.core;

import org.apache.commons.net.ftp.FTPClient;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws FTPClientException, IOException;

    void getCurrentRemoteDirectory() throws FTPClientException;

    void listCurrentDirectory() throws FTPClientException;

    boolean checkFileExists(String filePath) throws FTPClientException, IOException;

    boolean checkLocalDirectoryExists(String dirPath) throws FileNotFoundException;

    boolean checkDirectoryExists(String dirPath) throws FTPClientException;

    boolean deleteFile(String filePath) throws FTPClientException;

    boolean deleteDirectory(String dirPath) throws  FTPClientException;

    void uploadSingleFile(String localFilePath, String remoteFilePath) throws IOException, FTPClientException;

    void uploadMultipleFiles(String[] localPaths, String remotePath);

    boolean downloadSingleFile(String localPath, String remotePath) throws IOException, FTPClientException;

    boolean downloadMultipleFiles(String[] localPaths, String remotePath) throws IOException;

    boolean renameRemoteFile(String oldName, String newName) throws FTPClientException;

    boolean copyDirectory(String toCopy, String newDir) throws FTPClientException, IOException;

    int searchFilesWithKeyword(String filePath, String keyword) throws FTPClientException;

    int searchFilesWithExtension(String filePath, String extension) throws FTPClientException;

    void downloadDirectory(String currentDir, String saveDir) throws IOException, FTPClientException;

    void uploadDirectory(String localParentDir, String remoteParentDir) throws IOException, FTPClientException;

    boolean renameLocalFile(String oldName, String newName) throws FTPClientException;
}
