package ftp.core;

public class ClientCredentials {
    String userName;
    String password;
    String server;
    String protocol;

    public ClientCredentials(String userName, String password, String server, String protocol) {
        this.userName = userName;
        this.password = password;
        this.server = server;
        this.protocol = protocol;
    }

    public ClientCredentials() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String encryptPassword(String password) {
        String encryptedPassword = "";
        return encryptedPassword;
    }

    public String decryptPassword(String encryptedPassword) {
        String plainPassword = "";
        return plainPassword;
    }
}
