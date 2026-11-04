/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.workers;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.db.DBAccountingDriver;
import com.radiusengine.aaa.db.DataSource;
import com.radiusengine.aaa.server.CachingConfigurations;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccountingRequest;

public class RecordAcctStartProcessor implements Runnable {
    AccountingRequest acctRequest;
    ConcurrentHashMap acctFieldMapping;
    String PACKET_IDENTIFIER = null;
    public static Logger log = LoggerFactory.getLogger("radiuslog");

    public RecordAcctStartProcessor(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, String PACKET_IDENTIFIER) {
        this.acctRequest = acctRequest;
        this.acctFieldMapping = acctFieldMapping;
        this.PACKET_IDENTIFIER = PACKET_IDENTIFIER;
    }

    public void run() {
        try {
            DBAccountingDriver driver = new DBAccountingDriver(this.PACKET_IDENTIFIER);
            String strUsername = null;

            try {
                strUsername = this.acctRequest.getAttribute("User-Name").getAttributeValue();
                strUsername = strUsername != null ? strUsername.trim() : null;
            } catch (Exception e) {
                log.info("User-Name Not Found");
            }

            String nasIP = this.attributeValueOrNull("NAS-IP-Address");
            String framedIPAddress = this.attributeValueOrNull("Framed-IP-Address");
            boolean enableDuplicateCheck = false;

            try {
                CachingConfigurations.getInstance(); // makes sure the cache is initialised
                Cache cache = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCACHING);
                enableDuplicateCheck = Boolean.valueOf(cache.get(AAAConstant.ENABLE_DUPLICATE_START_CHECK).getObjectValue().toString());
            } catch (Exception e) {
                enableDuplicateCheck = false;
            }

            try (Connection conn = DataSource.getConnection()) {
                boolean duplicateStart = enableDuplicateCheck && driver.isDupliacteStart(conn, strUsername, nasIP, framedIPAddress);
                if (duplicateStart) {
                    log.info(this.PACKET_IDENTIFIER + "dropping the duplicate start,enableDuplicateCheck:{} ", enableDuplicateCheck);
                } else {
                    driver.insertDBSesion(conn, this.acctRequest, this.acctFieldMapping);
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET RecordAcctStartProcessor()" + e, e);
        }
    }

    /** Value of the named attribute, or null when it is missing or cannot be read. */
    private String attributeValueOrNull(String attributeName) {
        try {
            return this.acctRequest.getAttribute(attributeName).getAttributeValue();
        } catch (Exception e) {
            return null;
        }
    }
}
