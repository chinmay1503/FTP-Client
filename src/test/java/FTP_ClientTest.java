import ftp.core.FTPClientException;
import ftp.core.RemoteConnection;
import ftp.core.RemoteConnectionFactory;
import org.junit.Ignore;
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
    public void printPresentWorkingDirectory() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(prop.getProperty("protocol"));
        boolean connected = false;
        int connection_status = remoteConnection.connect(prop.getProperty("hostname"), prop.getProperty("username"), prop.getProperty("password"));
        if(connection_status == 1){
            connected = true;
        }
        assertTrue(connected);
        remoteConnection.getCurrentRemoteDirectory();
    }

    @Test
    public void listFilePresentInCurrentRemoteDirectory() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(prop.getProperty("protocol"));
        boolean connected = false;
        int connection_status = remoteConnection.connect(prop.getProperty("hostname"), prop.getProperty("username"), prop.getProperty("password"));
        if(connection_status == 1){
            connected = true;
        }
        assertTrue(connected);
        remoteConnection.listCurrentDirectory();
    }

    @Test
    public void deleteDirectoryFTPTest() throws FTPClientException, IOException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(prop.getProperty("protocol"));
        boolean connected = false;
        int connection_status = remoteConnection.connect(prop.getProperty("hostname"), prop.getProperty("username"), prop.getProperty("password"));
        if(connection_status == 1){
            connected = true;
        }
        assertTrue(connected);
        String path = "/Test";
        remoteConnection.createNewDirectory(path);
        assertTrue(remoteConnection.deleteDirectory(path));
        assertFalse(remoteConnection.checkDirectoryExists(path));
    }

    @Test
    public void makeNewDirectoryTest() throws FTPClientException, IOException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(prop.getProperty("protocol"));
        boolean connected = false;
        int connection_status = remoteConnection.connect(prop.getProperty("hostname"), prop.getProperty("username"), prop.getProperty("password"));
        if(connection_status == 1){
            connected = true;
        }
        assertTrue(connected);

        String path = "/newTestDir";
        boolean result = remoteConnection.createNewDirectory(path);
        assertTrue(result);
        remoteConnection.deleteDirectory(path);
    }

    @Ignore // Ignoring this before pushing code. because local file path is different for other team.
    @Test
    public void uploadSingleFileToRemoteMustNotRaiseErrorTest() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(prop.getProperty("protocol"));
        boolean connected = false;
        int connection_status = remoteConnection.connect(prop.getProperty("hostname"), prop.getProperty("username"), prop.getProperty("password"));
        if(connection_status == 1){
            connected = true;
        }
        assertTrue(connected);

        String localPath = "D:\\Summer21\\agile sw developement\\Local Files\\test1.txt";
        String remotePath = "/upload123";
        try{
            remoteConnection.uploadSingleFile(localPath, remotePath);
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}
