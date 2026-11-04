//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.bbb.workers;

import com.radiusengine.bbb.db.DBAccountingDriver;
import com.radiusengine.bbb.server.CachingConfigurations;
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
            } catch (Exception var13) {
                log.info("User-Name Not Found");
            }

            String sessionId = null;

            try {
                sessionId = this.acctRequest.getAttribute("Acct-Session-Id").getAttributeValue();
            } catch (Exception var12) {
                sessionId = null;
            }

            String nasIP = null;

            try {
                nasIP = this.acctRequest.getAttribute("NAS-IP-Address").getAttributeValue();
            } catch (Exception var11) {
                nasIP = null;
            }

            String framedIPAddress = null;

            try {
                framedIPAddress = this.acctRequest.getAttribute("Framed-IP-Address").getAttributeValue();
            } catch (Exception var10) {
                framedIPAddress = null;
            }

            boolean enableDuplicateCheck = false;

            try {
                CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
                Cache cache = CachingConfigurations.getCacheManagerData().getCache("serverConfig");
                enableDuplicateCheck = Boolean.valueOf(cache.get("enable_duplicate_start_check").getObjectValue().toString());
            } catch (Exception var9) {
                enableDuplicateCheck = false;
            }

            boolean bInsertLiveUser = true;
            if (enableDuplicateCheck && driver.isDupliacteStart(strUsername, nasIP, framedIPAddress)) {
                bInsertLiveUser = false;
            }

            if (bInsertLiveUser) {
                driver.insertDBSesion(this.acctRequest, this.acctFieldMapping);
            } else {
                log.info(this.PACKET_IDENTIFIER + "dropping the duplicate start,enableDuplicateCheck:{} ", enableDuplicateCheck);
            }
        } catch (Exception var14) {
            Exception e = var14;
            log.error(this.PACKET_IDENTIFIER + "Error IN GET RecordAcctStartProcessor()" + e, e);
        }

    }
}
