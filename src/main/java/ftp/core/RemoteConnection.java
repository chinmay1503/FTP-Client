package ftp.core;

import java.io.IOException;

public interface RemoteConnection {

    boolean connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

    boolean createNewDirectory(String dirName) throws FTPClientException;

    int getClientReplyCode() throws FTPClientException;

    boolean checkDirectoryExists(String dirPath) throws FTPClientException;

    boolean deleteDirectory(String dirPath) throws  FTPClientException;

}
