package ftp.core;

import com.jcraft.jsch.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
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
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

import static ftp.core.FTPUtils.getFileNameFromRemote;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SFTPConnection implements RemoteConnection {

    private static Logger logger = LogManager.getLogger(SFTPConnection.class);

    static JSch jsch = new JSch();
    private static ChannelSftp sftpChannel;
    private static Session session = null;

    public boolean connect(String hostName, String userName, String password) throws FTPClientException {
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
            return true;
        } catch (JSchException e) {
            throw new FTPClientException(e);
        }
    }

    public void disconnect() {
        sftpChannel.disconnect();
        session.disconnect();
        logger.info("Disconnecting from the remote server");
    }

    @Override
    public int getClientReplyCode() throws FTPClientException {
        return 0;
    }

    @Override
    public void getCurrentRemoteDirectory() throws FTPClientException {
        try {
            System.out.println(sftpChannel.pwd());
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public void listCurrentDirectory() throws FTPClientException {
        try {
            Vector fileList = sftpChannel.ls(sftpChannel.pwd());
            for (int i = 0; i < fileList.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) fileList.get(i);
                System.out.println(entry.getFilename());
            }
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) throws FTPClientException {
        try {
            logger.debug("Going to delete file :[" + filePath + "]");
            sftpChannel.rm(filePath);
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean deleteDirectory(String dirPath) throws FTPClientException {
        try {
            logger.debug("Going to delete Directory :[" + dirPath + "]");
            sftpChannel.rmdir(dirPath);
            logger.debug("Directory deleted successfully.");
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public void uploadSingleFile(String localFilePath, String remotePath) throws IOException, FTPClientException {
        String remoteFilePath;
        File localFile = new File(localFilePath);
        if (localFile.isFile()) {
            remoteFilePath = remotePath + "/" + localFile.getName();
            if (checkDirectoryExists(remotePath)) {
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

    @Override
    public void uploadMultipleFiles(String[] localPaths, String remotePath) {

    }

    @Override
    public boolean createNewDirectory(String dirName) throws FTPClientException {
        try {
            sftpChannel.mkdir(dirName);
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

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

    @Override
    public boolean checkLocalDirectoryExists(String dirPath) throws FileNotFoundException {
        Path path = Paths.get(dirPath);
        return Files.exists(path);
    }

    @Override
    public boolean checkDirectoryExists(String dirPath) throws FTPClientException {
        SftpATTRS attrs = null;
        try {
            attrs = sftpChannel.stat(dirPath);
        } catch (SftpException e) {
            logger.error(dirPath + " not found");
            return false;
        }
        return attrs != null && attrs.isDir();
    }

    @Override
    public boolean renameRemoteFile(String oldName, String newName) throws FTPClientException {
        try {
            if (checkFileExists(oldName)) {
                sftpChannel.rename(oldName, newName);
                return true;
            }
            else {
                return false;
            }
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean copyDirectory(String sourceDir, String desDir) throws FTPClientException, IOException {
        try {
            if (checkDirectoryExists(sourceDir)) {
                String tempFolder = System.getProperty("user.dir") + '\\' + "temp";
                String tempFolderWithDes = System.getProperty("user.dir") + '\\' + "temp" + '\\' + desDir;
                File theDir = new File(tempFolderWithDes);
                if (!theDir.exists()) {
                    theDir.mkdirs();
                }
                System.out.println("Working Directory = " + System.getProperty("user.dir"));
                System.out.println(sftpChannel.pwd());
                downloadDirectory(sftpChannel.pwd() + sourceDir, tempFolderWithDes);
                if (!checkDirectoryExists(desDir)) {
                    sftpChannel.mkdir(desDir);
                }
                uploadDirectory(tempFolderWithDes, sftpChannel.pwd());
                FileUtils.deleteDirectory(new File(tempFolder));
            }
            else {
                return false;
            }
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
        return false;
    }

    @Override
    public boolean downloadSingleFile(String localPath, String remotePath) throws IOException, FTPClientException {
        try {
            String fileName = getFileNameFromRemote(remotePath);
            String outputLocation = localPath + File.separator + fileName;
            sftpChannel.get(remotePath, outputLocation);
            logger.info("Downloading file : [" + fileName + "] from remote location");
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean downloadMultipleFiles(String[] remotePaths, String localPath) throws IOException {
        System.out.println("Remote paths --> "+ remotePaths);
        try {
            for (String remotePath : remotePaths) {
                downloadSingleFile(localPath, remotePath);
            }
        } catch (IOException | FTPClientException e) {
            System.out.println("-- Error while downloading files from Remote server --");
        }
        return false;
    }

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

    private void printSearchResult(ArrayList<String> result) {
        if (result != null && result.size() > 0) {
            System.out.println("SEARCH RESULT:");
            for (String ftpFile : result) {
                System.out.println(ftpFile);
            }
        }
    }

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

    @Override
    public void downloadDirectory(String currentDir, String saveDir) throws IOException, FTPClientException {
        try {
            Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(currentDir); // List source directory structure.
            for (ChannelSftp.LsEntry oListItem : list) { // Iterate objects in the list to get file/folder names.
                if (!oListItem.getAttrs().isDir()) { // If it is a file (not a directory).
                    if (!(new File(saveDir + "/" + oListItem.getFilename())).exists() || (oListItem.getAttrs().getMTime() > Long.valueOf(new File(saveDir + "/" + oListItem.getFilename()).lastModified() / (long) 1000).intValue())) { // Download only if changed later.
                        new File(saveDir + "/" + oListItem.getFilename());
                        sftpChannel.get(currentDir + "/" + oListItem.getFilename(), saveDir + "/" + oListItem.getFilename()); // Grab file from source ([source filename], [destination filename]).
                    }
                } else if (!(".".equals(oListItem.getFilename()) || "..".equals(oListItem.getFilename()))) {
                    new File(saveDir + "/" + oListItem.getFilename()).mkdirs(); // Empty folder copy.
                    downloadDirectory(currentDir + "/" + oListItem.getFilename(), saveDir + "/" + oListItem.getFilename()); // Enter found folder on server to read its contents and create locally.
                }
            }
        }catch (SftpException e) {
            throw new FTPClientException(e);
        }
        return;
    }

    @Override
    public void uploadDirectory(String localParentDir, String remoteParentDir) throws IOException, FTPClientException {
        try {
            File sourceFile = new File(localParentDir);
            if (sourceFile.isFile()) {
                // copy if it is a file
                sftpChannel.cd(remoteParentDir);
                if (!sourceFile.getName().startsWith(".")) {
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(sourceFile);
                        sftpChannel.put(fileInputStream, sourceFile.getName(), ChannelSftp.OVERWRITE);
                    } finally {
                        if(fileInputStream != null) {
                            fileInputStream.close();
                        }
                    }

                }
            } else {
                File[] files = sourceFile.listFiles();
                if (files != null && !sourceFile.getName().startsWith(".")) {
                    sftpChannel.cd(remoteParentDir);
                    SftpATTRS attrs = null;
                    // check if the directory is already existing
                    try {
                        attrs = sftpChannel.stat(remoteParentDir + "/" + sourceFile.getName());
                    } catch (Exception e) {
                        System.out.println(remoteParentDir + "/" + sourceFile.getName() + " not found");
                    }
                    // else create a directory
                    if (attrs != null) {

                    } else {

                        sftpChannel.mkdir(sourceFile.getName());
                    }
                    for (File f : files) {
                        uploadDirectory(f.getAbsolutePath(), remoteParentDir + "/" + sourceFile.getName());
                    }
                }
            }
        }catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

}