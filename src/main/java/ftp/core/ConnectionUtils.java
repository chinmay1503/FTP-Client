package ftp.core;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ConnectionUtils {
    private static Logger logger = LogManager.getLogger(ConnectionUtils.class);

    public static void disconnectClient(FTPClient client) throws FTPClientException {
        try {
            client.logout();
            client.disconnect();
            logger.info("Disconnecting from the remote server");
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }
}