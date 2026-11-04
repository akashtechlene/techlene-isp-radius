///**
// * @author Akash Gaur
// * @created year 2026
// */
//
//package com.radiusengine.bbb.workers;
//
//import com.radiusengine.bbb.constant.AAAConstant;
//import com.radiusengine.bbb.data.CDRData;
//import com.radiusengine.bbb.db.DBAccountingDriver;
//import com.radiusengine.bbb.db.DataSource;
//import com.radiusengine.bbb.server.CachingConfigurations;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.tinyradius.packet.AccountingRequest;
//import org.tinyradius.threadpool.RadiusCOAPool;
//import org.tinyradius.workers.RadiusCoAProcessor;
//
//import java.sql.Connection;
//import java.util.ArrayList;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class RecordAcctInterimProcessor implements Runnable {
//    AccountingRequest acctRequest;
//    ConcurrentHashMap acctFieldMapping;
//    String PACKET_IDENTIFIER = null;
//    long custPackRelId = -1L;
//    boolean isUnlimitedQuota = false;
//    boolean isSpeedDowngradeFUP = false;
//    public static Logger log = LoggerFactory.getLogger("radiuslog");
//
//    public RecordAcctInterimProcessor(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, String PACKET_IDENTIFIER, long custPackRelId, boolean isUnlimitedQuota, boolean isSpeedDowngradeFUP) {
//        this.acctRequest = acctRequest;
//        this.acctFieldMapping = acctFieldMapping;
//        this.PACKET_IDENTIFIER = PACKET_IDENTIFIER;
//        this.custPackRelId = custPackRelId;
//        this.isUnlimitedQuota = isUnlimitedQuota;
//        this.isSpeedDowngradeFUP = isSpeedDowngradeFUP;
//    }
//
//    public void run() {
//        log.info(this.PACKET_IDENTIFIER + "[INTERIM CALLED]");
//
//        try (Connection conn = DataSource.getConnection()) {
//            DBAccountingDriver dbAccountingDrive = new DBAccountingDriver(this.PACKET_IDENTIFIER);
//            dbAccountingDrive.updateDBSesion(conn, this.acctRequest, this.acctFieldMapping);
//            String coaOperation = CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.COAOPERATION).toString();
//            if (this.custPackRelId != -1L && this.custPackRelId != -2L && !this.isUnlimitedQuota && !this.isSpeedDowngradeFUP) {
//                ArrayList<CDRData> cdrList = dbAccountingDrive.getCustomerCoAStatus(conn, this.acctRequest.getAttributeValue("User-Name"), this.custPackRelId);
//                if (cdrList != null) {
//                    log.info(this.PACKET_IDENTIFIER + "COA triggered");
//
//                    for (CDRData cdr : cdrList) {
//                        RadiusCOAPool.getCoaPool().getCOAExecutor().execute(new RadiusCoAProcessor(this.PACKET_IDENTIFIER, cdr, coaOperation));
//                    }
//                }
//            } else {
//                log.info(this.PACKET_IDENTIFIER + "COA status check skipped::custPackRelId:" + this.custPackRelId + ",isUnlimitedQuota:" + this.isUnlimitedQuota + ",isSpeedDowngradeFUP:" + this.isSpeedDowngradeFUP);
//            }
//        } catch (Exception e) {
//            log.error(this.PACKET_IDENTIFIER + "Error IN GET RecordAcctInterimProcessor()" + e, e);
//        }
//    }
//}
