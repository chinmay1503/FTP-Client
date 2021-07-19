import ftp.core.FTPConnection;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link FTPClient} class.
 */
public class FTP_ClientTest {

    @Test
    public void test_JUnit() {
        String str1="This is the testcase in this class";
        assertEquals("This is the testcase in this class", str1);
    }
}
