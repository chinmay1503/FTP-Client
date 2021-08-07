import ftp.core.*;
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
    static Path localDummyFilePath1 = Paths.get(currentPath.toString(), "foo1.txt");
    static Path localDummyFilePath2 = Paths.get(currentPath.toString(), "foo2.txt");
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
        boolean connected = false;
        int connected_value = remoteConnection.connect(clientCredentials.getServer(), clientCredentials.getUserName(), clientCredentials.getPassword());
        if(connected_value == 1){
            connected = true;
        }
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
            assertTrue(ftpRemoteConnection.checkFileExists("/foo.txt"));
            assertTrue(ftpRemoteConnection.deleteFile("/foo.txt"));
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

    @Test
    public void uploadSingleFileToRemote_SFTP() throws FTPClientException {
        try {
            sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
            assertTrue(sftpRemoteConnection.checkFileExists("/foo.txt"));
            assertTrue(sftpRemoteConnection.deleteFile("/foo.txt"));
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Test
    public void uploadMultipleFilesToRemote_FTP() throws FTPClientException {
        try {
            String[] localPaths = {
                    localDummyFilePath1.toString(),
                    localDummyFilePath2.toString()
            };
            ftpRemoteConnection.uploadMultipleFiles(localPaths, "/");
            assertTrue(ftpRemoteConnection.checkFileExists("/foo1.txt"));
            assertTrue(ftpRemoteConnection.checkFileExists("/foo2.txt"));
            assertTrue(ftpRemoteConnection.deleteFile("/foo1.txt"));
            assertTrue(ftpRemoteConnection.deleteFile("/foo2.txt"));
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

    @Test
    public void uploadMultipleFilesToRemote_SFTP() throws FTPClientException {
        try {
            String[] localPaths = {
                    localDummyFilePath1.toString(),
                    localDummyFilePath2.toString()
            };
            sftpRemoteConnection.uploadMultipleFiles(localPaths, "/");
            assertTrue(sftpRemoteConnection.checkFileExists("/foo1.txt"));
            assertTrue(sftpRemoteConnection.checkFileExists("/foo2.txt"));
            assertTrue(sftpRemoteConnection.deleteFile("/foo1.txt"));
            assertTrue(sftpRemoteConnection.deleteFile("/foo2.txt"));
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

    @Test
    public void downloadSingleFileFromRemote_SFTP() throws FTPClientException {
        try {
            String curDir = System.getProperty("user.dir");
            String testDir = curDir + "/test";
            FileUtils.forceMkdir(new File(testDir));
            FileUtils.touch(new File(curDir + "/abc.txt"));
            sftpRemoteConnection.uploadSingleFile(curDir + "/abc.txt", "/");
            sftpRemoteConnection.downloadSingleFile(testDir, "/abc.txt");
            sftpRemoteConnection.deleteFile("/abc.txt");
            FileUtils.deleteDirectory(new File(testDir));
            FileUtils.forceDelete(new File(curDir + "/abc.txt"));
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Ignore ("Test failing")
    @Test
    public void downloadSingleFileFromRemote_FTP() throws FTPClientException {
        try {
            String curDir = System.getProperty("user.dir");
            String testDir = curDir + "/test";
            FileUtils.forceMkdir(new File(testDir));
            FileUtils.touch(new File(curDir + "/abc.txt"));
            ftpRemoteConnection.uploadSingleFile(curDir + "/abc.txt", "/");
            ftpRemoteConnection.downloadSingleFile(testDir, "/abc.txt");
            ftpRemoteConnection.deleteFile("/abc.txt");
            FileUtils.deleteDirectory(new File(testDir));
            FileUtils.forceDelete(new File(curDir + "/abc.txt"));
        } catch (IOException e) {
            throw new FTPClientException(e);
        }
    }

    @Test
    public void downloadNonExistentSingleFileFromRemote_FTP() throws FTPClientException, IOException {
        assertFalse(ftpRemoteConnection.downloadSingleFile(localDummyFilePath.toString(), "/foo-non-existent-file.txt"));
    }

    @Test
    public void downloadNonExistentSingleFileFromRemote_SFTP() throws FTPClientException, IOException {
        assertFalse(sftpRemoteConnection.downloadSingleFile(localDummyFilePath.toString(), "/foo-non-existent-file.txt"));
    }

    @Test
    public void deleteDummyFileFromRemote_FTP() throws FTPClientException, IOException {
        ftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertTrue(ftpRemoteConnection.deleteFile("/foo.txt"));
    }

    @Test
    public void deleteNonExistentFileFromRemote_FTP() throws FTPClientException {
        assertFalse(ftpRemoteConnection.deleteFile("/foo-non-existent-file.txt"));
    }

    @Test
    public void deleteDummyFileFromRemote_SFTP() throws FTPClientException, IOException {
        //TODO: this test fails because of incomplete upload single file implementation
        sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertTrue(sftpRemoteConnection.deleteFile("/foo.txt"));
    }

    @Test
    public void deleteNonExistentFileFromRemote_SFTP() {
        assertThrows(FTPClientException.class, () -> {
            sftpRemoteConnection.deleteFile("/foo-non-existent-file.txt");
        });
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

    @Test
    public void renameRemoteFileExist_FTP() throws FTPClientException, IOException {
        ftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertTrue(ftpRemoteConnection.renameRemoteFile("/foo.txt", "/bar.txt"));
        ftpRemoteConnection.deleteFile("/bar.txt");
    }

    @Test
    public void renameRemoteFileDoesntExist_FTP() throws FTPClientException, IOException {
        assertFalse(ftpRemoteConnection.renameRemoteFile("/this_file_does_not_exist_I_hope.txt", "/bar.txt"));
    }

    @Test
    public void renameRemoteFileExist_SFTP() throws FTPClientException, IOException {
        sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertTrue(sftpRemoteConnection.renameRemoteFile("/foo.txt", "/bar.txt"));
        sftpRemoteConnection.deleteFile("/bar.txt");
    }

    @Test
    public void renameRemoteFileDoesntExist_SFTP() throws FTPClientException, IOException {
        assertFalse(sftpRemoteConnection.renameRemoteFile("/this_file_does_not_exist_I_hope.txt", "/bar.txt"));
    }

    @Test
    public void copyRemoteDirExist_FTP() throws IOException, FTPClientException {
        String curDir = System.getProperty("user.dir");
        String testDir = curDir + "/test";
        FileUtils.forceMkdir(new File(testDir));
        FileUtils.touch(new File(testDir + "/a.txt"));
        if(!ftpRemoteConnection.checkDirectoryExists(testDir)) {
            ftpRemoteConnection.createNewDirectory("/test");
        }
        ftpRemoteConnection.uploadDirectory(testDir, "/test");
        assertTrue(ftpRemoteConnection.copyDirectory("/test", "/copyTest"));
        ftpRemoteConnection.deleteDirectory("/test");
        ftpRemoteConnection.deleteDirectory("/copyTest");
        FileUtils.deleteDirectory(new File(testDir));
    }

    @Test
    public void copyRemoteDirDoesntExist_FTP() throws FTPClientException, IOException {
        assertFalse(ftpRemoteConnection.copyDirectory("/test-I-hope-doesnt-exist", "/copyTest"));
    }

    @Test
    public void copyRemoteDirExist_SFTP() throws IOException, FTPClientException {
        String curDir = System.getProperty("user.dir");
        String testDir = curDir + "/test";
        FileUtils.forceMkdir(new File(testDir));
        FileUtils.touch(new File(testDir + "/a.txt"));
        sftpRemoteConnection.createNewDirectory("/test");
        sftpRemoteConnection.uploadDirectory(testDir, "/");
        assertTrue(sftpRemoteConnection.copyDirectory("/test", "/copyTest"));
        sftpRemoteConnection.deleteDirectory("/test");
        sftpRemoteConnection.deleteDirectory("/copyTest");
        FileUtils.deleteDirectory(new File(testDir));
    }

    @Test
    public void copyRemoteDirDoesntExist_SFTP() throws FTPClientException, IOException {
        assertFalse(sftpRemoteConnection.copyDirectory("/test_I_hope_doesnt_exist", "/copyTest"));
    }

    @Test
    public void renameLocalFileTest() throws FTPClientException, IOException {
        String oldName = System.getProperty("user.dir") + File.separator + "test_rename.txt";
        String newName = System.getProperty("user.dir") + File.separator + "test_renamed_file.txt";
        FileUtils.touch(new File(oldName));
        FTPUtils.renameLocalFile(oldName, newName);
        assertTrue(org.codehaus.plexus.util.FileUtils.fileExists(newName));
        FileUtils.deleteQuietly(new File(newName));
    }

    @Test
    public void renameLocalFileNotExistTest() throws FTPClientException, IOException {
        String oldName = System.getProperty("user.dir") + File.separator + "test_rename.txt";
        String newName = System.getProperty("user.dir") + File.separator + "test_renamed_file.txt";
        assertFalse(org.codehaus.plexus.util.FileUtils.fileExists(oldName));
        assertFalse(FTPUtils.renameLocalFile(oldName, newName));
    }

    @Test
    public void changePermissions_FTP() throws FTPClientException, IOException {
        ftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertFalse(ftpRemoteConnection.changePermission("444", "/foo.txt"));
        ftpRemoteConnection.deleteFile("/foo.txt");
    }

    @Test
    public void changePermissions_SFTP() throws FTPClientException, IOException {
        sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertTrue(sftpRemoteConnection.changePermission("444", "/foo.txt"));
        assertTrue(sftpRemoteConnection.changePermission("600", "/foo.txt"));
        assertTrue(sftpRemoteConnection.changePermission("320", "/foo.txt"));
        sftpRemoteConnection.deleteFile("/foo.txt");
    }

    @Test
    public void changePermissionsInvalid_SFTP() throws FTPClientException, IOException {
        sftpRemoteConnection.uploadSingleFile(localDummyFilePath.toString(), "/");
        assertFalse(sftpRemoteConnection.changePermission("abc", "/foo.txt"));
        assertFalse(sftpRemoteConnection.changePermission("888", "/foo.txt"));
        sftpRemoteConnection.deleteFile("/foo.txt");
    }

    public static void createDummyFooFile() throws FTPClientException {
        try{
            FileUtils.touch(localDummyFilePath.toFile());
            FileUtils.touch(localDummyFilePath1.toFile());
            FileUtils.touch(localDummyFilePath2.toFile());
        } catch(IOException e){
            throw new FTPClientException(e);
        }
    }

    public static void cleanUpDummyFooFile() {
        FileUtils.deleteQuietly(localDummyFilePath.toFile());
        FileUtils.deleteQuietly(localDummyFilePath1.toFile());
        FileUtils.deleteQuietly(localDummyFilePath2.toFile());
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
