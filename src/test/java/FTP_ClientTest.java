import ftp.core.FTPClientException;
import ftp.core.RemoteConnection;
import ftp.core.RemoteConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class FTP_ClientTest {

    static Properties prop = new Properties();

    @BeforeAll
    public static void initializeProp() throws IOException, FTPClientException {
        FileInputStream fs = null;
        try {
            File propFile = new File("src/test/resources/connection_details.properties");
            fs = new FileInputStream(propFile);
            prop.load(fs);
        } catch (IOException e) {
            throw new FTPClientException(e);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    @Test
    public void deleteDirectoryFTPTest() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(prop.getProperty("protocol"));
        boolean connected = remoteConnection.connect(prop.getProperty("hostname"), prop.getProperty("username"), prop.getProperty("password"));
        assertTrue(connected);
        String path = "/Test";
        remoteConnection.createNewDirectory(path);
        assertTrue(remoteConnection.deleteDirectory(path));
        assertFalse(remoteConnection.checkDirectoryExists(path));
    }

}
