package ftp.core;

import java.io.File;

public class FTPUtils {

    //Parse file name from filepath
    public static String getFileNameFromRemote(String filePath) {
        if (filePath.length() > 0) {
            File file = new File(filePath);
            return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\") + 1);
        }
        return "";
    }

}
