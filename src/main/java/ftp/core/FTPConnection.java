package ftp.core;

import com.jcraft.jsch.IO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ftp.core.FTPUtils.getFileNameFromRemote;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * FTPConnection class - this class has all the method implementations that is used by FTPClient.
 */
public class FTPConnection implements RemoteConnection {

    private FTPClient client;
    private static final Logger logger = LogManager.getLogger(FTPClient.class);

    @Override
    public int connect(String hostName, String userName, String password) {
        try {
            client = new FTPClient();
            client.connect(hostName, 21);
            boolean login = client.login(userName, password);
            if (login) {
                client.enterLocalPassiveMode();
                client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
//                return login;
                return 1;
            } else {
                System.err.println("Username or password is incorrect");
                 return 0;
            }
        } catch (SocketException e) {
            System.err.println("Error occurred when trying to connect to Server");
        } catch (IOException e) {
            System.err.println("Error due to IOException");
        }
//        return false;
         return 2;
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
     *
     * @param dirName - name of the directory.
     * @return [boolean] - returns true if successfully created a directory on remote server, else return false.
     * @throws IOException - can throw exception while handling files.
     */
    @Override
    public boolean createNewDirectory(String dirName) throws IOException {
        try {
            return client.makeDirectory(dirName);
        } catch (SocketException e) {
            System.out.println("Something went wrong, when trying to create directory \"" + dirName + "\"\n" +
                    "Give valid Directory path/ name .");
        }
        return false;
    }

    //download a single file from remote server to local
    @Override
    public boolean downloadMultipleFiles(String[] remotePaths, String localPath) throws IOException {
        System.out.println("Remote paths --> " + remotePaths);
        try {
            for (String remotePath : remotePaths) {
                downloadSingleFile(localPath, remotePath);
            }
        } catch (IOException | FTPClientException e) {
            System.out.println("-- Error while downloading files from Remote server --");
        }

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

    /**
     * This method is used to check if directory exists or not.
     *
     * @param dirPath - the remote directory path.
     * @return [boolean] - returns true if exists else return false.
     * @throws FTPClientException - can throw IOException while handling files.
     */
    @Override
    public boolean checkDirectoryExists(String dirPath) throws FTPClientException {
        try {
            // This is the limitation of the FTPClient library that we are using, thus we need to use changeWorkingDirectory and later traverse back to original path.
            client.changeWorkingDirectory(dirPath);
            int returnCode = client.getReplyCode();
            if (returnCode == 550) {
                return false;
            } else {
                logger.info("File exists, reverting to previous directory");
                client.changeWorkingDirectory("..");
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
            logger.debug("Going to delete file :[" + filePath + "]");
            return client.deleteFile(filePath);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean deleteDirectory(String dirPath) throws FTPClientException {
        try {
            FTPFile[] ftpFiles = client.listFiles(dirPath);
            for (FTPFile file : ftpFiles) {
                dirPath = dirPath.endsWith("/") ? dirPath : dirPath + "/";
                if (file.isDirectory()) {
                    deleteDirectory(dirPath + file.getName());
                } else {
                    client.deleteFile(dirPath + file.getName());
                }
            }
            return client.removeDirectory(dirPath);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to put a single file on the remote server, using FTP protocol.
     *
     * @param localFilePath - this is the path on local system
     * @param remotePath    - this is the path on remote server.
     * @throws IOException        - can throw IOException, while handling files.
     * @throws FTPClientException - throws this exception while checking if file exist or not opn remote server.
     */
    @Override
    public void uploadSingleFile(String localFilePath, String remotePath) throws IOException, FTPClientException {
        boolean uploaded = false;
        String remoteFilePath;

        File localFile = new File(localFilePath);
        if (localFile.isFile()) {
            remoteFilePath = remotePath + "/" + localFile.getName();
            if (checkDirectoryExists(remotePath)) {
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
     *
     * @param localPaths [Array] - these are the paths of all the files on local system, that user wants to upload
     * @param remotePath - this is the path on remote server, where user want to upload all those files.
     */
    @Override
    public void uploadMultipleFiles(String[] localPaths, String remotePath) {
        System.out.println("local paths --> " + localPaths);
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

    @Override
    public boolean renameRemoteFile(String oldName, String newName) throws FTPClientException {
        try {
            return client.rename(oldName, newName);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean copyDirectory(String sourceDir, String desDir) throws FTPClientException, IOException {
        if (checkDirectoryExists(sourceDir)) {
            String tempFolder = System.getProperty("user.dir") + '\\' + "temp";
            File theDir = new File(tempFolder);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            downloadDirectory(client.printWorkingDirectory() + sourceDir, tempFolder);
            if (!checkDirectoryExists(desDir)) {
                client.makeDirectory(desDir);
            }
            uploadDirectory(tempFolder + '\\' + sourceDir, desDir);
            FileUtils.deleteDirectory(new File(tempFolder));
            return true;
        }
        System.out.println(sourceDir + " directory does not exist");
        return false;
    }

    @Override
    public void downloadDirectory(String currentDir, String saveDir) throws IOException, FTPClientException {
        String parentDir = client.printWorkingDirectory();
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = client.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/"
                        + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                String newDirPath = saveDir + parentDir + File.separator
                        + currentDir + File.separator + currentFileName;
                if (currentDir.equals("")) {
                    newDirPath = saveDir + parentDir + File.separator
                            + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // create the directory in saveDir
                    File newDir = new File(newDirPath);
                    boolean created = newDir.mkdirs();
                    if (created) {
                        System.out.println("CREATED the directory: " + newDirPath);
                    } else {
                        System.out.println("COULD NOT create the directory: " + newDirPath);
                    }

                    // download the sub directory
                    downloadDirectory(currentFileName, saveDir);
                } else {
                    // download the file
                    boolean success = downloadSingleFile(newDirPath, filePath);
                    if (success) {
                        System.out.println("DOWNLOADED the file: " + filePath);
                    } else {
                        System.out.println("COULD NOT download the file: "
                                + filePath);
                    }
                }
            }
        }
    }

    @Override
    public boolean downloadSingleFile(String localPath, String remoteFilePath) throws IOException, FTPClientException {
        File downloadFile = new File(localPath);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(downloadFile));
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE);
            return client.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    @Override
    public int searchFilesWithKeyword(String filePath, String keyword) throws FTPClientException {
        if (isNullOrEmpty(filePath) || isNullOrEmpty(keyword)) {
            return 0;
        }

        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(keyword));
        return searchFiles(filePath, filter);
    }

    private int searchFiles(String filePath, FTPFileFilter filter) throws FTPClientException {
        FTPFile[] result;
        try {
            result = client.listFiles(filePath, filter);
            if (result != null && result.length > 0) {
                System.out.println("SEARCH RESULT:");
                for (FTPFile ftpFile : result) {
                    System.out.println(ftpFile.getName());
                }
            }
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
        return result != null ? result.length : 0;
    }

    @Override
    public int searchFilesWithExtension(String filePath, String extension) throws FTPClientException {
        if (isNullOrEmpty(filePath) || isNullOrEmpty(extension)) {
            return 0;
        }

        String ext = extension.startsWith(".") ? extension : "." + extension;
        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().endsWith(ext));
        return searchFiles(filePath, filter);
    }

    @Override
    public void uploadDirectory(String localParentDir, String remoteParentDir) throws IOException, FTPClientException {
        String remoteDirPath = client.printWorkingDirectory();
        System.out.println("LISTING directory: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath = remoteDirPath + "/" + remoteParentDir
                        + "/" + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteDirPath + "/" + item.getName();
                }


                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("About to upload the file: " + localFilePath);
                    uploadSingleFile(localFilePath, remoteFilePath);
                } else {
                    // create directory on the server
                    boolean created = client.makeDirectory(remoteFilePath);
                    if (created) {
                        System.out.println("CREATED the directory: "
                                + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT create the directory: "
                                + remoteFilePath);
                    }

                    // upload the sub directory
                    String parent = remoteParentDir + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    uploadDirectory(localParentDir, parent);
                }
            }
        }
    }

    @Override
    public boolean renameLocalFile(String oldName, String newName) throws FTPClientException {
        return FTPUtils.renameLocalFile(oldName, newName);
    }

}

