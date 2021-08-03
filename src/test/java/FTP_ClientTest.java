import ftp.core.ClientCredentials;
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
    static ClientCredentials ftpClientCredentials = null;
    static ClientCredentials sftpClientCredentials = null;

    @BeforeAll
    public static void initializeProp() throws IOException, FTPClientException {
        FileInputStream fs = null;
        try {
            File propFile = new File("src/test/resources/connection_details.properties");
            fs = new FileInputStream(propFile);
            prop.load(fs);
            populateTestClientCredentials();
        } catch (IOException e) {
            throw new FTPClientException(e);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    private static void populateTestClientCredentials() {
        ftpClientCredentials = new ClientCredentials(prop.getProperty("ftp_username"),prop.getProperty("ftp_password"), prop.getProperty("ftp_hostname"), "FTP");
        sftpClientCredentials = new ClientCredentials(prop.getProperty("sftp_username"),prop.getProperty("sftp_password"), prop.getProperty("sftp_hostname"), "SFTP");
    }

    @Test
    public void printPresentWorkingDirectory_FTP() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(ftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(ftpClientCredentials.getServer(), ftpClientCredentials.getUserName(), ftpClientCredentials.getPassword());
        assertTrue(connected);
        remoteConnection.getCurrentRemoteDirectory();
    }

    @Test
    public void printPresentWorkingDirectory_SFTP() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(sftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(sftpClientCredentials.getServer(), sftpClientCredentials.getUserName(), sftpClientCredentials.getPassword());
        assertTrue(connected);
        remoteConnection.getCurrentRemoteDirectory();
    }

    @Test
    public void listFilePresentInCurrentRemoteDirectory_FTP() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(ftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(ftpClientCredentials.getServer(), ftpClientCredentials.getUserName(), ftpClientCredentials.getPassword());
        assertTrue(connected);
        remoteConnection.listCurrentDirectory();
    }

    @Test
    public void listFilePresentInCurrentRemoteDirectory_SFTP() throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(sftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(sftpClientCredentials.getServer(), sftpClientCredentials.getUserName(), sftpClientCredentials.getPassword());
        assertTrue(connected);
        remoteConnection.listCurrentDirectory();
    }

    @Test
    public void deleteDirectory_FTP() throws FTPClientException, IOException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(ftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(ftpClientCredentials.getServer(), ftpClientCredentials.getUserName(), ftpClientCredentials.getPassword());
        assertTrue(connected);
        String path = "/TestFTP";
        remoteConnection.createNewDirectory(path);
        assertTrue(remoteConnection.deleteDirectory(path));
        assertFalse(remoteConnection.checkDirectoryExists(path));
    }

    @Test
    public void deleteDirectory_SFTP() throws FTPClientException, IOException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(sftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(sftpClientCredentials.getServer(), sftpClientCredentials.getUserName(), sftpClientCredentials.getPassword());
        assertTrue(connected);
        String path = "/TestSFTP";
        remoteConnection.createNewDirectory(path);
        assertTrue(remoteConnection.deleteDirectory(path));
        assertFalse(remoteConnection.checkDirectoryExists(path));
    }

    @Test
    public void makeNewDirectory_FTP() throws FTPClientException, IOException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(ftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(ftpClientCredentials.getServer(), ftpClientCredentials.getUserName(), ftpClientCredentials.getPassword());
        assertTrue(connected);

        String path = "/newTestDir";
        boolean result = remoteConnection.createNewDirectory(path);
        assertTrue(result);
        remoteConnection.deleteDirectory(path);
    }

    @Test
    public void makeNewDirectory_SFTP() throws FTPClientException, IOException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(sftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(sftpClientCredentials.getServer(), sftpClientCredentials.getUserName(), sftpClientCredentials.getPassword());
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
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(sftpClientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(sftpClientCredentials.getServer(), sftpClientCredentials.getUserName(), sftpClientCredentials.getPassword());
        assertTrue(connected);

        String localPath = "D:\\Summer21\\agile sw developement\\Local Files\\test1.txt";
        String remotePath = "/upload123";
        try{
            remoteConnection.uploadSingleFile(localPath, remotePath);
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

}
