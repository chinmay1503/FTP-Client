package ftp.core;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

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
    public boolean createNewDirectory(String dirName) throws IOException {
        return false;
    }

    @Override
    public int getClientReplyCode() throws IOException {
        return 0;
    }

    @Override
    public boolean checkDirectoryExists(String dirPath) throws IOException {
        return false;
    }

    @Override
    public boolean getRemoteFile(String remoteDirName, String localPath) throws IOException {
        return false;
    }

    @Override
    public boolean getRemoteFile(String remoteDirName, OutputStream localPath) throws IOException {
        return false;
    }

}