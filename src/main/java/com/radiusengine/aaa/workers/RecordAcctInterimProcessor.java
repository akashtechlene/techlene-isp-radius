/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.workers;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.CDRData;
import com.radiusengine.aaa.db.DBAccountingDriver;
import com.radiusengine.aaa.db.DataSource;
import com.radiusengine.aaa.server.CachingConfigurations;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.threadpool.RadiusCOAPool;
import org.tinyradius.workers.RadiusCoAProcessor;

public class RecordAcctInterimProcessor implements Runnable {
    AccountingRequest acctRequest;
    ConcurrentHashMap acctFieldMapping;
    String PACKET_IDENTIFIER = null;
    long custPackRelId = -1L;
    boolean isUnlimitedQuota = false;
    boolean isSpeedDowngradeFUP = false;
    public static Logger log = LoggerFactory.getLogger("radiuslog");

    public RecordAcctInterimProcessor(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, String PACKET_IDENTIFIER, long custPackRelId, boolean isUnlimitedQuota, boolean isSpeedDowngradeFUP) {
        this.acctRequest = acctRequest;
        this.acctFieldMapping = acctFieldMapping;
        this.PACKET_IDENTIFIER = PACKET_IDENTIFIER;
        this.custPackRelId = custPackRelId;
        this.isUnlimitedQuota = isUnlimitedQuota;
        this.isSpeedDowngradeFUP = isSpeedDowngradeFUP;
    }

    public void run() {
        log.info(this.PACKET_IDENTIFIER + "[INTERIM CALLED]");

        try (Connection conn = DataSource.getConnection()) {
            DBAccountingDriver dbAccountingDrive = new DBAccountingDriver(this.PACKET_IDENTIFIER);
            dbAccountingDrive.updateDBSesion(conn, this.acctRequest, this.acctFieldMapping);
            String coaOperation = CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.COAOPERATION).toString();
            if (this.custPackRelId != -1L && this.custPackRelId != -2L && !this.isUnlimitedQuota && !this.isSpeedDowngradeFUP) {
                ArrayList<CDRData> cdrList = dbAccountingDrive.getCustomerCoAStatus(conn, this.acctRequest.getAttributeValue("User-Name"), this.custPackRelId);
                if (cdrList != null) {
                    log.info(this.PACKET_IDENTIFIER + "COA triggered");

                    for (CDRData cdr : cdrList) {
                        RadiusCOAPool.getCoaPool().getCOAExecutor().execute(new RadiusCoAProcessor(this.PACKET_IDENTIFIER, cdr, coaOperation));
                    }
                }
            } else {
                log.info(this.PACKET_IDENTIFIER + "COA status check skipped::custPackRelId:" + this.custPackRelId + ",isUnlimitedQuota:" + this.isUnlimitedQuota + ",isSpeedDowngradeFUP:" + this.isSpeedDowngradeFUP);
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET RecordAcctInterimProcessor()" + e, e);
        }
    }
}
