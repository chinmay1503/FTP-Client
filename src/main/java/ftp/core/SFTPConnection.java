package ftp.core;

import com.jcraft.jsch.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import static ftp.core.FTPUtils.getFileNameFromRemote;

public class SFTPConnection implements RemoteConnection {

    private static Logger logger = LogManager.getLogger(SFTPConnection.class);

    static JSch jsch = new JSch();
    private static ChannelSftp sftpChannel;
    private static Session session = null;

    @Override
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

    @Override
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
    public void uploadSingleFile(String localFilePath, String remoteFilePath) throws IOException {

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
        return false;
    }

    @Override
    public boolean checkLocalDirectoryExists(String dirPath) throws FileNotFoundException {
        return false;
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
    public boolean downloadSingleFile(String remotePath, String localPath) throws IOException, FTPClientException {
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
    public boolean downloadMultipleFiles(String[] localPaths, String remotePath) throws IOException {
        return false;
    }
}