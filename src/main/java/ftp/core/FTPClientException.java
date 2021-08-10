package ftp.core;

public class FTPClientException extends Exception {

    public FTPClientException(String msg) { super(msg);}

    public FTPClientException(Throwable e) { super(e);}

}