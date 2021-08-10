package ftp.core;

public class RemoteConnectionFactory {

    public RemoteConnection getInstance(String protocol) {
        if ("SFTP".equalsIgnoreCase(protocol)) {
            return new SFTPConnection();
        }  else {
            return new FTPConnection();
        }
    }
}