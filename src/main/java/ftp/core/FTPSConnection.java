package ftp.core;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

public class FTPSConnection implements RemoteConnection {

    private static Logger logger = LogManager.getLogger(FTPConnection.class);
    private FTPSClient client;

    public void connect(String hostName, String userName, String password) throws FTPClientException {
        try {
            logger.info("Creating FTPS client and trying to connect...");
            client = new FTPSClient(false);
            client.setAuthValue("TLS");
            client.connect(hostName, 23);
            boolean login = client.login(userName, password);
            if (login) {
                logger.info("Successfully logged in");
                client.execPBSZ(0);
                client.execPROT("P");
                client.enterLocalPassiveMode();
            } else {
                throw new FTPClientException("Username or password is incorrect");
            }
        } catch (SocketException e) {
            throw new FTPClientException(e);
        } catch (IOException e) {
            throw new FTPClientException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new FTPClientException(e);
        }
    }

    public void disconnect() throws FTPClientException {
        ConnectionUtils.disconnectClient(client);
    }
}
