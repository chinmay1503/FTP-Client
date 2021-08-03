import ftp.core.ClientCredentials;
import ftp.core.FTPClientException;
import ftp.core.RemoteConnection;
import ftp.core.RemoteConnectionFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


public class FTP_ClientTest {

    static Properties prop = new Properties();
    static ClientCredentials ftpClientCredentials = null;
    static ClientCredentials sftpClientCredentials = null;
    static Path currentPath = Paths.get(System.getProperty("user.dir"));
    static Path localDummyFilePath = Paths.get(currentPath.toString(), "foo.txt");
    static RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
    static RemoteConnection ftpRemoteConnection = remoteConnectionFactory.getInstance("FTP");
    static RemoteConnection sftpRemoteConnection = remoteConnectionFactory.getInstance("SFTP");

    @BeforeAll
    public static void initialize() throws IOException, FTPClientException {
        FileInputStream fs = null;
        try {
            File propFile = new File("src/test/resources/connection_details.properties");
            fs = new FileInputStream(propFile);
            prop.load(fs);
            populateTestClientCredentials();
            connectFTPRemote();
            connectSFTPRemote();
            createDummyFooFile();
        } catch (IOException e) {
            throw new FTPClientException(e);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    private static void populateTestClientCredentials() {
        ftpClientCredentials = new ClientCredentials(prop.getProperty("ftp_username"), prop.getProperty("ftp_password"), prop.getProperty("ftp_hostname"), "FTP");
        sftpClientCredentials = new ClientCredentials(prop.getProperty("sftp_username"), prop.getProperty("sftp_password"), prop.getProperty("sftp_hostname"), "SFTP");
    }

    public static RemoteConnection getRemoteConnectionObject(ClientCredentials clientCredentials) throws FTPClientException {
        RemoteConnectionFactory remoteConnectionFactory = new RemoteConnectionFactory();
        RemoteConnection remoteConnection = remoteConnectionFactory.getInstance(clientCredentials.getProtocol());
        boolean connected = remoteConnection.connect(clientCredentials.getServer(), clientCredentials.getUserName(), clientCredentials.getPassword());
        assertTrue(connected);
        return remoteConnection;
    }

    private static void connectFTPRemote() throws FTPClientException {
        ftpRemoteConnection = getRemoteConnectionObject(ftpClientCredentials);
    }

    private static void connectSFTPRemote() throws FTPClientException {
        sftpRemoteConnection = getRemoteConnectionObject(sftpClientCredentials);
    }

    @Test
    public void printPresentWorkingDirectory_FTP() throws FTPClientException {
        ftpRemoteConnection.getCurrentRemoteDirectory();
    }

    @Test
    public void printPresentWorkingDirectory_SFTP() throws FTPClientException {
        sftpRemoteConnection.getCurrentRemoteDirectory();
    }

    @Test
    public void listFilePresentInCurrentRemoteDirectory_FTP() throws FTPClientException {
        ftpRemoteConnection.listCurrentDirectory();
    }

    @Test
    public void listFilePresentInCurrentRemoteDirectory_SFTP() throws FTPClientException {
        sftpRemoteConnection.listCurrentDirectory();
    }

    @Test
    public void deleteDirectory_FTP() throws FTPClientException, IOException {
        String path = "/TestFTP";
        ftpRemoteConnection.createNewDirectory(path);
        assertTrue(ftpRemoteConnection.deleteDirectory(path));
        assertFalse(ftpRemoteConnection.checkDirectoryExists(path));
    }

    @Test
    public void deleteDirectory_SFTP() throws FTPClientException, IOException {
        String path = "/TestSFTP";
        sftpRemoteConnection.createNewDirectory(path);
        assertTrue(sftpRemoteConnection.deleteDirectory(path));
        assertFalse(sftpRemoteConnection.checkDirectoryExists(path));
    }

    @Test
    public void makeNewDirectory_FTP() throws FTPClientException, IOException {
        String path = "/newTestDir";
        boolean result = ftpRemoteConnection.createNewDirectory(path);
        assertTrue(result);
        ftpRemoteConnection.deleteDirectory(path);
    }

    @Test
    public void makeNewDirectory_SFTP() throws FTPClientException, IOException {
        String path = "/newTestDir";
        boolean result = sftpRemoteConnection.createNewDirectory(path);
        assertTrue(result);
        sftpRemoteConnection.deleteDirectory(path);
    }

    @Test
    public void uploadSingleFileToRemote_FTP() throws FTPClientException {
        try {
            ftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

    @Test
    public void uploadSingleFileToRemote_SFTP() throws FTPClientException {
        try {
            sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    public static void createDummyFooFile() throws FTPClientException {
        try{
            FileUtils.touch(localDummyFilePath.toFile());
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

    public static void cleanUpDummyFooFile() {
        FileUtils.deleteQuietly(localDummyFilePath.toFile());
    }

    @Test
    public void searchFilesWithKeyword_FTP() throws FTPClientException, IOException {
        ftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        int fileCount = ftpRemoteConnection.searchFilesWithKeyword("/", "foo");
        assertTrue(fileCount > 0);
    }

    @Test
    public void searchFilesWithKeywordThatDontExist_FTP() throws FTPClientException {
        int fileCount = ftpRemoteConnection.searchFilesWithKeyword("/", "this_file_is_non-existent");
        assertEquals(0, fileCount);

        fileCount = ftpRemoteConnection.searchFilesWithExtension("", "this_file_is_non-existent");
        assertEquals(0, fileCount);

        fileCount = ftpRemoteConnection.searchFilesWithExtension("/", "");
        assertEquals(0, fileCount);
    }

    @Test
    public void searchFilesWithExtension_FTP() throws FTPClientException, IOException {
        ftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        int fileCount = ftpRemoteConnection.searchFilesWithExtension("/", "txt");
        assertTrue(fileCount > 0);
    }

    @Test
    public void searchFilesWithExtensionThatDontExist_FTP() throws FTPClientException {
        int fileCount = ftpRemoteConnection.searchFilesWithExtension("/", "zzz");
        assertEquals(0, fileCount);

        fileCount = ftpRemoteConnection.searchFilesWithExtension("", "zzz");
        assertEquals(0, fileCount);

        fileCount = ftpRemoteConnection.searchFilesWithExtension("/", "");
        assertEquals(0, fileCount);
    }

    @Test
    public void searchFilesWithKeyword_SFTP() throws FTPClientException, IOException {
        //TODO: this test fails because of incomplete upload single file implementation
        sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        int fileCount = sftpRemoteConnection.searchFilesWithKeyword("/", "foo");
        assertTrue(fileCount > 0);
    }

    @Test
    public void searchFilesWithKeywordThatDontExist_SFTP() throws FTPClientException {
        int fileCount = sftpRemoteConnection.searchFilesWithKeyword("/", "this_file_is_non-existent");
        assertEquals(0, fileCount);

        fileCount = sftpRemoteConnection.searchFilesWithExtension("", "this_file_is_non-existent");
        assertEquals(0, fileCount);

        fileCount = sftpRemoteConnection.searchFilesWithExtension("/", "");
        assertEquals(0, fileCount);
    }

    @Test
    public void searchFilesWithExtension_SFTP() throws FTPClientException, IOException {
        sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        int fileCount = sftpRemoteConnection.searchFilesWithExtension("/", "txt");
        assertTrue(fileCount > 0);
    }

    @Test
    public void searchFilesWithExtensionThatDontExist_SFTP() throws FTPClientException {
        int fileCount = sftpRemoteConnection.searchFilesWithExtension("/", "zzz");
        assertEquals(0, fileCount);

        fileCount = sftpRemoteConnection.searchFilesWithExtension("", "this_file_is_non-existent");
        assertEquals(0, fileCount);

        fileCount = sftpRemoteConnection.searchFilesWithExtension("/", "");
        assertEquals(0, fileCount);
    }

    @AfterAll
    // This method will delete the dummy foo.txt file created in user's current working directory.
    public static void cleanUpClass() throws FTPClientException {
        cleanUpDummyFooFile();
        closeFTPRemote();
        closeSFTPRemote();
    }

    private static void closeFTPRemote() throws FTPClientException {
        if (ftpRemoteConnection != null)
            ftpRemoteConnection.disconnect();
    }

    private static void closeSFTPRemote() throws FTPClientException {
        if (sftpRemoteConnection != null)
            sftpRemoteConnection.disconnect();
    }

}
