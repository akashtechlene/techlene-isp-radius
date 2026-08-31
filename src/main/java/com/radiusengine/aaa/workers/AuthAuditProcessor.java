/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.workers;

import com.radiusengine.aaa.db.DBAuthenticationDriver;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthAuditProcessor implements Runnable {
    HashMap<String, String> hsAuthRes = null;
    String PACKET_IDENTIFIER = null;
    public static Logger log = LoggerFactory.getLogger("radiuslog");

    public AuthAuditProcessor(HashMap<String, String> hsAuthRes, String PACKET_IDENTIFIER) {
        this.hsAuthRes = hsAuthRes;
        this.PACKET_IDENTIFIER = PACKET_IDENTIFIER;
    }

    public void run() {
        try {
            DBAuthenticationDriver driver = new DBAuthenticationDriver();
            driver.insertAuthResponse(this.hsAuthRes);
        } catch (Exception var2) {
            Exception e = var2;
            log.error(this.PACKET_IDENTIFIER + "Error IN AuthAuditProcessor()" + e, e);
        }

    }
}
