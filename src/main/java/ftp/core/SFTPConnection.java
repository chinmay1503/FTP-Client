package ftp.core;

import com.jcraft.jsch.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

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
//            return true;
            return 1;
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
            logger.debug("File deleted successfully.");
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean deleteDirectory(String remoteDir) {
        try {
            if (isDirectory(remoteDir)) {
                Vector<ChannelSftp.LsEntry> dirList = sftpChannel.ls(remoteDir);
                for (ChannelSftp.LsEntry entry : dirList) {
                    if (!(entry.getFilename().equals(".") || entry.getFilename().equals(".."))) {
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

    private boolean isDirectory(String remoteDirectory) throws SftpException {
        return sftpChannel.stat(remoteDirectory).isDir();
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
    public boolean createNewDirectory(String dirName) throws FTPClientException {
        try {
            if (!checkDirectoryExists(dirName)) {
                sftpChannel.mkdir(dirName);
            }
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
            sftpChannel.rename(oldName, newName);
            return true;
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }

    @Override
    public boolean copyDirectory(String toCopy, String newDir) throws FTPClientException, IOException {
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
        return;
    }

    @Override
    public void uploadDirectory(String localParentDir, String remoteParentDir) throws IOException, FTPClientException {
        return;
    }

    @Override
    public boolean renameLocalFile(String oldName, String newName) throws FTPClientException {
        return FTPUtils.renameLocalFile(oldName, newName);
    }

}