package ftp.core;

import com.jcraft.jsch.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

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
        } catch (JSchException e) {
            throw new FTPClientException(e);
        }
        return false;
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
            Vector ls = sftpChannel.ls(sftpChannel.pwd());
            Iterator iterator = ls.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
        } catch (SftpException e) {
            throw new FTPClientException(e);
        }
    }


    @Override
    public boolean deleteDirectory(String dirPath) throws FTPClientException {
        try {
            logger.debug("Going to delete file :[" + dirPath + "]");
            sftpChannel.rmdir(dirPath);
            logger.debug("File deleted successfully.");
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
        return false;
    }

    @Override
    public int getClientReplyCode() throws FTPClientException {
        return 0;
    }

    @Override
    public boolean checkDirectoryExists(String dirPath) throws FTPClientException {
        return false;
    }
}