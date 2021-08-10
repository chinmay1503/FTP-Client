package ftp.core;

public class ClientCredentials {
    String userName;
    String password;
    String server;
    String protocol;
    EncryptionKeys ek = null;

    public ClientCredentials() {
    }
    public ClientCredentials(String userName, String password, String server, String protocol) throws Exception {
        EncryptionKeys ek1 = new EncryptionKeys(password);
        setEk(ek1);
        this.userName = userName;
        this.password = getEk().getEncryptedPasswordString();
        this.server = server;
        this.protocol = protocol;
    }

    private String encodePassword(String password) {
        StringBuffer sb = new StringBuffer();
        char ch[] = password.toCharArray();
        for(int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            sb.append(hexString);
        }
        String result = sb.toString();
        return result;
    }

    public String decodePassword(String encodedPassword){
        String result = new String();
        char[] charArray = encodedPassword.toCharArray();
        for(int i = 0; i < charArray.length; i=i+2) {
            String st = ""+charArray[i]+""+charArray[i+1];
            char ch = (char)Integer.parseInt(st, 16);
            result = result + ch;
        }
        return result;
    }

    public EncryptionKeys getEk() {
        return ek;
    }

    public void setEk(EncryptionKeys ek) {
        this.ek = ek;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
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

}