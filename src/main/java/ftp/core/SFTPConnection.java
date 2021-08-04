package ftp.core;

import com.jcraft.jsch.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

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
    public int getClientReplyCode() throws FTPClientException {
        return 0;
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
    public boolean copyDirectory(String toCopy, String newDir) throws FTPClientException {
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
}