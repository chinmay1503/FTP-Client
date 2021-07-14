package ftp.core;

import java.io.IOException;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws IOException;

    int getClientReplyCode() throws IOException;

    boolean checkDirectoryExists(String dirPath) throws IOException;

}
