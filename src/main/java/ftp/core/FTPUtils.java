package ftp.core;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FTPUtils {
    private static final Logger logger = LogManager.getLogger(FTPUtils.class);

    //Parse file name from filepath
    public static String getFileNameFromRemote(String filePath) {
        if (filePath.length() > 0) {
            File file = new File(filePath);
            return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\") + 1);
        }
        return "";
    }

    public static boolean renameLocalFile(String oldName, String newName) throws FTPClientException {
        try {
            if (org.codehaus.plexus.util.FileUtils.fileExists(oldName)) {
                logger.info("Going to rename file from :[" + oldName + "] to [" + newName + "]");
                org.codehaus.plexus.util.FileUtils.rename(new File(oldName), new File(newName));
            } else {
                logger.info("File with name :[" + oldName + "] does not exist");
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
