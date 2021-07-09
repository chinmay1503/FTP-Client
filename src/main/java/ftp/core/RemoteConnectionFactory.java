package ftp.core;

public class RemoteConnectionFactory {

    public RemoteConnection getInstance(String protocol) {
        if (protocol.equalsIgnoreCase("FTP")) {
            return new FTPConnection();
        } else if (protocol.equalsIgnoreCase("FTPS")) {
            return new FTPSConnection();
        } else {
            return new SFTPConnection();
        }
    }
}
