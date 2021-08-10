package ftp.core;

import com.jcraft.jsch.*;


import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import static ftp.core.FTPUtils.getFileNameFromRemote;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SFTPConnection implements RemoteConnection {

    private static Logger logger = LogManager.getLogger(SFTPConnection.class);

    static JSch jsch = new JSch();
    private static ChannelSftp sftpChannel;
    private static Session session = null;

    /**
     * This method is used to make connection with the SFTP remote server.
     *
     * @param hostName - eg: 127.0.0.1 (for localhost)
     * @param userName - client name
     * @param password - client password
     * @return [int] - return 1 for success
     * @throws FTPClientException
     */
    public int connect(String hostName, String userName, String password) throws FTPClientException {
        try {
            session = jsch.getSession(userName, hostName, 22);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);
            session.connect();
            logger.info("Successfully Connected , creating a channel");
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            return 1;
        } catch (JSchException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to disconnect from the remote SFTP server
     */
    public void disconnect() {
        sftpChannel.disconnect();
        session.disconnect();
        logger.info("Disconnecting from the remote server");
    }

    /**
     * This method is used to get current remote directory location
     *
     * @throws FTPClientException
     */
    @Override
    public void getCurrentRemoteDirectory() throws FTPClientException {
        try {
            System.out.println(sftpChannel.pwd());
        } catch (SftpException e) {
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
            Vector fileList = sftpChannel.ls(sftpChannel.pwd());
            for (int i = 0; i < fileList.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) fileList.get(i);
                String fileName = entry.getFilename();
                if(!".".equals(fileName) && !"..".equals(fileName)) {
                    System.out.println(entry.getFilename());
                }
            }
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to delete a file, present on remote SFTP server.
     * @param filePath - remote file path
     * @return [boolean] - true if success
     */
    @Override
    public boolean deleteFile(String filePath) {
        try {
            if (!isDirectory(filePath)) {
                System.out.println("Going to delete file :[" + filePath + "]");
                sftpChannel.rm(filePath);
                return true;
            } else {
                System.out.println("Expecting a file. Please enter valid file path.");
                return false;
            }
        } catch (SftpException e) {
            System.out.println("Failed due to: " + e.getMessage());
            return false;
        }
    }

    /**
     * This method is used to delete an entire directory (including files present in it),
     * on the SFTP remote server.
     *
     * @param remoteDir - remote directory path
     * @return [boolean] - true if success else return false
     */
    @Override
    public boolean deleteDirectory(String remoteDir) {
        try {
            if (isDirectory(remoteDir)) {
                Vector<ChannelSftp.LsEntry> dirList = sftpChannel.ls(remoteDir);
                for (ChannelSftp.LsEntry entry : dirList) {
                    if (!(".".equals(entry.getFilename()) || "..".equals(entry.getFilename()))) {
                        remoteDir = remoteDir.endsWith("/") ? remoteDir : remoteDir + "/";
                        if (entry.getAttrs().isDir()) {
                            deleteDirectory(remoteDir + entry.getFilename() + "/");
                        } else {
                            sftpChannel.rm(remoteDir + entry.getFilename());
                        }
                    }
                }
                sftpChannel.cd("..");
                sftpChannel.rmdir(remoteDir);
            }
        } catch (SftpException e) {
            logger.error("Error while deleting the directory :[" + e.getMessage() + "]");
            return false;
        }
        return true;
    }

    /**
     * This method is used to check if provided path (remote path) is a directory or not.
     *
     * @param remoteDirectory - remote SFTP path
     * @return [boolean]
     * @throws SftpException
     */
    private boolean isDirectory(String remoteDirectory) throws SftpException {
        return sftpChannel.stat(remoteDirectory).isDir();
    }

    /**
     * This method is used to put a single file on the remote server, using SFTP protocol.
     *
     * @param localFilePath - this is the path on local system
     * @param remotePath    - this is the path on remote server.
     * @throws IOException        - can throw IOException, while handling files.
     * @throws FTPClientException - throws this exception while checking if file exist or not opn remote server.
     */
    @Override
    public void uploadSingleFile(String localFilePath, String remotePath) throws IOException, FTPClientException {
        String remoteFilePath;
        File localFile = new File(localFilePath);
        if (localFile.isFile()) {
            remoteFilePath = remotePath + "/" + localFile.getName();
            if (checkRemoteDirectoryExists(remotePath)) {
                try (InputStream inputStream = new FileInputStream(localFile)) {
                    sftpChannel.put(inputStream, remoteFilePath);
                    logger.info("file upload successful");
                    System.out.println("UPLOADED a file to: " + remoteFilePath);
                } catch (SftpException e) {
                    logger.info("file upload Unsuccessful");
                    System.out.println("Error occurred when trying to upload the file: \""
                            + localFilePath + "\" to \"" + remoteFilePath + "\"");
                    System.out.println("-- Something went wrong when trying to upload the file. --\n");
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
     * This method is used to upload multiple files onto remote SFTP server.
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
     * This method is used to create new Directory on remote SFTP server.
     *
     * @param dirName - name of the directory.
     * @return [boolean] - returns true if successfully created a directory on remote server, else return false.
     * @throws IOException - can throw exception while handling files.
     */
    @Override
    public boolean createNewDirectory(String dirName) throws FTPClientException {
        try {
            if (!checkRemoteDirectoryExists(dirName)) {
                sftpChannel.mkdir(dirName);
            }
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to check if given path is a file or not
     *
     * @param filePath - remote SFTP path
     * @return [boolean]
     * @throws FTPClientException
     */
    @Override
    public boolean checkFileExists(String filePath) throws FTPClientException {
        try {
            Vector files = sftpChannel.ls(filePath);
            return files.size() == 1;
        } catch (SftpException e) {
            logger.error(filePath + " not found");
            return false;
        }
    }

    /**
     * This method is used to if the given directory exists or not on the local machine.
     *
     * @param dirPath - local path
     * @return [boolean]
     * @throws FileNotFoundException
     */
    @Override
    public boolean checkLocalDirectoryExists(String dirPath) throws FileNotFoundException {
        Path path = Paths.get(dirPath);
        return Files.exists(path);
    }

    /**
     * This method is used to check if the given directory path exists or not on remote server
     *
     * @param dirPath - remote path
     * @return [boolean]
     * @throws FTPClientException
     */
    @Override
    public boolean checkRemoteDirectoryExists(String dirPath) throws FTPClientException {
        SftpATTRS attrs = null;
        try {
            attrs = sftpChannel.stat(dirPath);
        } catch (SftpException e) {
            return false;
        }
        return attrs != null && attrs.isDir();
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
                sftpChannel.rename(oldName, newName);
                return true;
            }
            else {
                System.out.println(oldName + " file does not exist");
                return false;
            }
        } catch (SftpException e) {
            System.out.println(newName + " already exists");
            return false;
        }
    }

    /**
     * This method is used to create a copy of a directory on remote server.
     *
     * @param sourceDir - the name of the directory you want to copy.
     * @param desDir - the name of the new copy
     * @return [boolean]
     * @throws FTPClientException
     * @throws IOException
     */
    @Override
    public boolean copyDirectory(String sourceDir, String desDir) throws FTPClientException, IOException {
        try {
            if (checkRemoteDirectoryExists(sourceDir)) {
                String tempFolder = System.getProperty("user.dir") + '\\' + "temp";
                String tempFolderWithDes = System.getProperty("user.dir") + '\\' + "temp" + '\\' + desDir;
                File theDir = new File(tempFolderWithDes);
                if (!theDir.exists()) {
                    theDir.mkdirs();
                }
                downloadDirectory("/" + sourceDir, tempFolderWithDes);
                if (!checkRemoteDirectoryExists(desDir)) {
                    sftpChannel.mkdir(desDir);
                }
                uploadDirectory(tempFolderWithDes, "/");
                FileUtils.deleteDirectory(new File(tempFolder));
                System.out.println("Successfully made a copy of " + sourceDir + " called " + desDir);
                return true;
            }
            System.out.println(sourceDir + " directory does not exist");
            return false;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    /**
     * This method is used to download a single file from a remote SFTP server to local machine.
     *
     * @param localPath - local path where you want to download the file to.
     * @param remotePath - remote path from where you want to download the file from.
     * @return [boolean] - true if success.
     * @throws IOException
     * @throws FTPClientException
     */
    @Override
    public boolean downloadSingleFile(String localPath, String remotePath) throws IOException, FTPClientException {
        try {
            if(!checkLocalDirectoryExists(localPath)){
                File downloadLocation = new File(localPath);
                downloadLocation.mkdirs();
            }
            String fileName = getFileNameFromRemote(remotePath);
            String outputLocation = localPath + File.separator + fileName;
            sftpChannel.get(remotePath, outputLocation);
            logger.info("Downloading file : [" + fileName + "] from remote location");
            return true;
        } catch (SftpException e) {
            logger.error("Error Downloading file : [" + remotePath + "] from remote location");
            return false;
        }
    }

    /**
     * This method is used to download a multiple files from a remote SFTP server to local machine.
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
     * This method is used to search for a file present on the remote SFTP server using a keyword.
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

        ArrayList<String> result = new ArrayList<>();
        try {
            Vector fileList = sftpChannel.ls(filePath);
            for (Object sftpFile : fileList) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) sftpFile;
                if (entry.getFilename().contains(keyword)) {
                    result.add(entry.getFilename());
                }
            }
            printSearchResult(result);
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
        return result.size();
    }

    /**
     * This method is used to print the search result for 'searchFilesWithKeyword' and 'searchFilesWithExtension' functions.
     *
     * @param result - list of file names, which contain keyword/ extension.
     */
    private void printSearchResult(ArrayList<String> result) {
        if (result != null && result.size() > 0) {
            System.out.println("SEARCH RESULT:");
            for (String ftpFile : result) {
                System.out.println(ftpFile);
            }
        }
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
        ArrayList<String> result = new ArrayList<>();
        try {
            Vector fileList = sftpChannel.ls(filePath);
            for (Object sftpFile : fileList) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) sftpFile;
                if (entry.getFilename().endsWith(ext)) {
                    result.add(entry.getFilename());
                }
            }
            printSearchResult(result);
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
        return result.size();
    }

    /**
     * This method is used to download a directory on the remote server onto local.
     *
     * @param currentDir
     * @param saveDir
     * @return [boolean]
     * @throws IOException
     * @throws FTPClientException
     */
    @Override
    public boolean downloadDirectory(String currentDir, String saveDir) throws IOException, FTPClientException {
        if (checkRemoteDirectoryExists(currentDir)) {
            try {
                if (!checkLocalDirectoryExists(saveDir)) {
                    File downloadLocation = new File(saveDir);
                    downloadLocation.mkdirs();
                }
                if (checkRemoteDirectoryExists(currentDir)) {
                    Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(currentDir);
                    for (ChannelSftp.LsEntry listItem : list) {
                        if (!listItem.getAttrs().isDir()) {
                            if (!(new File(saveDir + "/" + listItem.getFilename())).exists() ||
                                    (listItem.getAttrs().getMTime() > Long.valueOf(new File(saveDir + "/" + listItem.getFilename()).lastModified() / (long) 1000).intValue())) {
                                new File(saveDir + "/" + listItem.getFilename());
                                sftpChannel.get(currentDir + "/" + listItem.getFilename(), saveDir + "/" + listItem.getFilename());
                            }
                        } else if (!(".".equals(listItem.getFilename()) || "..".equals(listItem.getFilename()))) {
                            new File(saveDir + "/" + listItem.getFilename()).mkdirs();
                            downloadDirectory(currentDir + "/" + listItem.getFilename(), saveDir + "/" + listItem.getFilename());
                        }
                    }
                }
            } catch (SftpException e) {
                throw new FTPClientException(e);
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * This method is used to upload a directory on local onto the remote server.
     *
     * @param localParentDir
     * @param remoteParentDir
     * @return [boolean]
     * @throws IOException
     * @throws FTPClientException
     */
    @Override
    public boolean uploadDirectory(String localParentDir, String remoteParentDir) throws IOException, FTPClientException {
        if (checkLocalDirectoryExists(localParentDir)) {
            try {
                File sourceFile = new File(localParentDir);
                if (sourceFile.isFile()) {
                    sftpChannel.cd(remoteParentDir);
                    if (!sourceFile.getName().startsWith(".")) {
                        FileInputStream fileInputStream = null;
                        try {
                            fileInputStream = new FileInputStream(sourceFile);
                            sftpChannel.put(fileInputStream, sourceFile.getName(), ChannelSftp.OVERWRITE);
                        } finally {
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                        }

                    }
                } else {
                    File[] files = sourceFile.listFiles();
                    if (files != null && !sourceFile.getName().startsWith(".")) {
                        sftpChannel.cd(remoteParentDir);
                        SftpATTRS attrs = null;
                        try {
                            attrs = sftpChannel.stat(remoteParentDir + "/" + sourceFile.getName());
                        } catch (Exception e) {
                            logger.debug(remoteParentDir + "/" + sourceFile.getName() + " not found. creating it now.");
                        }
                        if (attrs == null) {
                            sftpChannel.mkdir(sourceFile.getName());
                        }
                        sftpChannel.cd("..");
                        for (File f : files) {
                            uploadDirectory(f.getAbsolutePath(), remoteParentDir + "/" + sourceFile.getName());
                        }
                    }
                }
                sftpChannel.cd("..");
            } catch (SftpException e) {
                throw new FTPClientException(e);
            }
            return true;
        } else {
            return false;
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
        try {
            sftpChannel.chmod(Integer.parseInt(permissions, 8), inputPath);
            logger.info("Successfully changed the file permissions..!!");
            return true;
        } catch (SftpException | NumberFormatException e) {
            logger.debug("Failed to change file permissions");
            logger.debug(e.getMessage());
            System.out.println("-- Error: Could not change permissions or invalid chmod code. "+ e.getMessage() +" --");
            return false;
        }
    }

    @Override
    public void searchFile(String userOption, File theDir) {
        FTPUtils.searchFile(userOption, theDir);
    }

}