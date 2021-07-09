import ftp.core.FTPClientException;
import ftp.core.RemoteConnection;
import ftp.core.RemoteConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FTPClient {

    private static Logger logger = LogManager.getLogger(FTPClient.class);

    public static void main(String[] args) throws FTPClientException {
        logger.debug("Main method Execution -> Starts");
        if (!(args.length < 3)) {
            String hostName = args[0];
            String userName = args[1];
            String password = args[2];
            String protocol = "";
            if (args.length == 4) {
                protocol = args[3];
            }
            String currentDir = System.getProperty("user.dir");
            RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
            RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(protocol);
            try {
                remoteConnection.connect(hostName, userName, password);
                logger.debug("Main Method Execution -> Ends");
            } catch (Exception e) {
                throw new FTPClientException(e);
            } finally {
                remoteConnection.disconnect();
            }
        } else {
            logger.info("Specify the required Arguments as Follows :");
            logger.info("[hostName] [userName] [password] [PROTOCOL : OPTIONAL]");
        }
        logger.debug("Main Method Execution -> Ends");
    }

}
