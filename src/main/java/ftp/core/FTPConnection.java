package ftp.core;

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

    /**
     * This method is used to make connection with the FTP remote server.
     *
     * @param hostName - eg: 127.0.0.1 (for localhost)
     * @param userName - client name
     * @param password - client password
     * @return [int] - return 1 for success
     * @throws FTPClientException
     */
    @Override
    public int connect(String hostName, String userName, String password) {
        try {
            client = new FTPClient();
            client.connect(hostName, 21);
            boolean login = client.login(userName, password);
            if (login) {
                client.enterLocalPassiveMode();
                client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
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
         return 2;
    }

    /**
     * This method is used to disconnect from the remote FTP server
     */
    public void disconnect() throws FTPClientException {
        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to create new Directory on remote FTP server.
     *
     * @param dirName - name of the directory.
     * @return [boolean] - returns true if successfully created a directory on remote server, else return false.
     * @throws IOException - can throw exception while handling files.
     */
    @Override
    public boolean createNewDirectory(String dirName) throws IOException, FTPClientException {
        try {
            if (!checkRemoteDirectoryExists(dirName)) {
                client.makeDirectory(dirName);
            }
        } catch (SocketException e) {
            System.out.println("Something went wrong, when trying to create directory \"" + dirName + "\"\n" +
                    "Give valid Directory path/ name .");
            return false;
        }
        return true;
    }

    /**
     * This method is used to if the given directory exists or not on remote server.
     *
     * @param filePath - remote path
     * @return [boolean]
     * @throws FileNotFoundException
     */
    @Override
    public boolean checkFileExists(String filePath) throws IOException {
        FTPFile[] remoteFile = client.listFiles(filePath);
        return remoteFile.length > 0;
    }

    /**
     *
     * @param dirPath
     * @return
     */
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
    public boolean checkRemoteDirectoryExists(String dirPath) throws FTPClientException {
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

    /**
     * This method is used to get current remote directory location
     *
     * @throws FTPClientException
     */
    @Override
    public void getCurrentRemoteDirectory() throws FTPClientException {
        try {
            System.out.println(client.printWorkingDirectory());
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to list all the files present in current remote directory
     *
     * @throws FTPClientException
     */
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

    /**
     * This method is used to delete a file, present on remote FTP server.
     * @param filePath - remote file path
     * @return [boolean] - true if success
     * @throws FTPClientException
     */
    @Override
    public boolean deleteFile(String filePath) throws FTPClientException {
        try {
            logger.debug("Going to delete file :[" + filePath + "]");
            return client.deleteFile(filePath);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to delete an entire directory (including files present in it),
     * on the FTP remote server.
     *
     * @param dirPath - remote directory path
     * @return [boolean] - true if success else return false
     */
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
            if (checkRemoteDirectoryExists(remotePath)) {
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
     * This method is used to upload multiple files onto remote FTP server.
     *
     * @param localPaths [Array] - these are the paths of all the files on local system, that user wants to upload
     * @param remotePath - this is the path on remote server, where user want to upload all those files.
     */
    @Override
    public void uploadMultipleFiles(String[] localPaths, String remotePath) {
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

    /**
     * This method is used to rename the file present on remote server.
     *
     * @param oldName - the name of the file you want to update.
     * @param newName - the new name
     * @return [boolean]
     * @throws FTPClientException
     */
    @Override
    public boolean renameRemoteFile(String oldName, String newName) throws FTPClientException {
        try {
            if (checkFileExists(oldName)) {
                return client.rename(oldName, newName);
            }
            else {
                System.out.println(oldName + " does not exist");
                return false;
            }
        } catch (IOException e) {
            System.out.println(newName + " already exists");
            return false;
        }
    }

    /**
     *
     * @param sourceDir
     * @param desDir
     * @return
     * @throws FTPClientException
     * @throws IOException
     */
    @Override
    public boolean copyDirectory(String sourceDir, String desDir) throws FTPClientException, IOException {
        if (checkRemoteDirectoryExists(sourceDir)) {
            String tempFolder = System.getProperty("user.dir") + '\\' + "temp";
            File theDir = new File(tempFolder);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            downloadDirectory(client.printWorkingDirectory() + sourceDir, tempFolder);
            if (!checkRemoteDirectoryExists(desDir)) {
                client.makeDirectory(desDir);
            }
            uploadDirectory(tempFolder + '\\' + sourceDir, desDir);
            FileUtils.deleteDirectory(new File(tempFolder));
            return true;
        }
        System.out.println(sourceDir + " directory does not exist");
        return false;
    }

    /**
     *
     * @param currentDir
     * @param saveDir
     * @throws IOException
     * @throws FTPClientException
     */
    @Override
    public void downloadDirectory(String currentDir, String saveDir) throws IOException, FTPClientException {
        String parentDir = client.printWorkingDirectory();
        String dirToList = parentDir;
        if (!"".equals(currentDir)) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = client.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (".".equals(currentFileName) || "..".equals(currentFileName)) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
                if ("".equals(currentDir)) {
                    filePath = parentDir + "/" + currentFileName;
                }

                String newDirPath = saveDir + parentDir + File.separator + currentDir + File.separator + currentFileName;
                if ("".equals(currentDir)) {
                    newDirPath = saveDir + parentDir + File.separator + currentFileName;
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
                    downloadDirectory(filePath, saveDir);
                } else {
                    // download the file
                    boolean success = downloadSingleFile(newDirPath, filePath);
                    if (success) {
                        System.out.println("DOWNLOADED the file: " + filePath);
                    } else {
                        System.out.println("COULD NOT download the file: " + filePath);
                    }
                }
            }
        }
    }

    /**
     * This method is used to download a single file from a remote FTP server to local machine.
     *
     * @param localPath - local path where you want to download the file to.
     * @param remoteFilePath - remote path from where you want to download the file from.
     * @return [boolean] - true if success.
     * @throws IOException
     * @throws FTPClientException
     */
    @Override
    public boolean downloadSingleFile(String localPath, String remoteFilePath) throws IOException, FTPClientException {
        File downloadFile = new File(localPath);
        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }
        String fileName = getFileNameFromRemote(remoteFilePath);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE);
            logger.info("Downloading file : [" + fileName + "] from remote location");
            client.retrieveFile(remoteFilePath, outputStream);
            return true;
        } catch (IOException ex) {
            logger.error("Error Downloading file : [" + remoteFilePath + "] from remote location");
            return false;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * This method is used to download a multiple files from a remote FTP server to local machine.
     *
     * @param remotePaths - remote path's (String array) from where you want to download the file from.
     * @param localPath - local path where you want to download the file to.
     * @return [boolean] - true if success.
     * @throws IOException
     */
    @Override
    public boolean downloadMultipleFiles(String[] remotePaths, String localPath) throws IOException {
        try {
            for (String remotePath : remotePaths) {
                downloadSingleFile(localPath, remotePath);
            }
        } catch (IOException | FTPClientException e) {
            System.out.println("-- Error while downloading files from Remote server --");
        }
        return false;
    }

    /**
     * This method is used to search for a file present on the remote FTP server using a keyword.
     *
     * @param filePath - file path, where you want to search.
     * @param keyword - keyword to use
     * @return [int]
     * @throws FTPClientException
     */
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

    /**
     * This method is used to search for a file based on given extension.
     *
     * @param filePath - file path, where you want to search.
     * @param extension - extension to use
     * @return [int]
     * @throws FTPClientException
     */
    @Override
    public int searchFilesWithExtension(String filePath, String extension) throws FTPClientException {
        if (isNullOrEmpty(filePath) || isNullOrEmpty(extension)) {
            return 0;
        }

        String ext = extension.startsWith(".") ? extension : "." + extension;
        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().endsWith(ext));
        return searchFiles(filePath, filter);
    }

    /**
     *
     * @param localParentDir
     * @param remoteParentDir
     * @throws IOException
     * @throws FTPClientException
     */
    @Override
    public void uploadDirectory(String localParentDir, String remoteParentDir) throws IOException, FTPClientException {
        String remoteDirPath = client.printWorkingDirectory();
        System.out.println("LISTING directory: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath =  "/" + remoteParentDir;
                if ("".equals(remoteParentDir)) {
                    remoteFilePath = remoteDirPath;
                }
                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("About to upload the file: " + localFilePath);
                    uploadSingleFile(localFilePath, remoteFilePath);
                } else {
                    // create directory on the server
                    String remoteSubDirPath = "/" + remoteParentDir + "/" + item.getName();
                    boolean created = client.makeDirectory(remoteSubDirPath);
                    if (created) {
                        System.out.println("CREATED the directory: " + remoteSubDirPath);
                    } else {
                        System.out.println("COULD NOT create the directory: " + remoteSubDirPath);
                    }

                    // upload the sub directory

                    if ("".equals(remoteParentDir)) {
                        remoteSubDirPath = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    uploadDirectory(localParentDir, remoteSubDirPath);
                }
            }
        }
    }

    /**
     * This method is used to rename the file present on local machine
     *
     * @param oldName - the name of the file you want to update.
     * @param newName - the new name
     * @return [boolean]
     * @throws FTPClientException
     */
    @Override
    public boolean renameLocalFile(String oldName, String newName) throws FTPClientException {
        return FTPUtils.renameLocalFile(oldName, newName);
    }

    /**
     * This method is used to change permissions of file on remote machine
     *
     * @param permissions - User permissions for the file (e.g. 777, 600, 444).
     * @param inputPath - the absolute filepath on the remote server
     * @return [boolean]
     */
    @Override
    public boolean changePermission(String permissions, String inputPath) {
        System.out.println("Not Supported in FTP through Site Commands.");
        return false;
    }

    @Override
    public void searchFile(String userOption, File theDir) {
        FTPUtils.searchFile(userOption, theDir);
    }

}

