package ftp.core;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;

public class FTPConnection implements RemoteConnection {

    private static Logger logger = LogManager.getLogger(FTPConnection.class);
    private FTPClient client;

    public void connect(String hostName, String userName, String password) throws FTPClientException {
        try {
            logger.info("Creating FTP client and trying to connect...");
            client = new FTPClient();
            client.connect(hostName, 21);
            boolean login = client.login(userName, password);
            if (login) {
                logger.info("Successfully logged in");
                client.enterLocalPassiveMode();
                client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            } else {
                throw new FTPClientException("Username or password is incorrect");
            }
        } catch (SocketException e) {
            throw new FTPClientException(e);
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    public void disconnect() throws FTPClientException {
        ConnectionUtils.disconnectClient(client);
    }
}
