package ftp.core;

import java.io.IOException;
import java.io.OutputStream;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws IOException;

    int getClientReplyCode() throws IOException;

    boolean checkDirectoryExists(String dirPath) throws IOException;

    boolean getRemoteFile(String remoteDirName, String localPath) throws IOException;

    boolean getRemoteFile(String remoteDirName, OutputStream localPath) throws IOException;

}
