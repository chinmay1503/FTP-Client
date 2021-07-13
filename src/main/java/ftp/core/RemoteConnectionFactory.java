package ftp.core;

public class RemoteConnectionFactory {

    public RemoteConnection getInstance(String protocol) {
        if (protocol.equalsIgnoreCase("SFTP")) {
            return new SFTPConnection();
        }  else {
            return new FTPConnection();
        }
    }
}
