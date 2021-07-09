package ftp.core;

public interface RemoteConnection {

    void connect(String hostName, String userName, String password) throws FTPClientException;

    void disconnect() throws FTPClientException;

}
