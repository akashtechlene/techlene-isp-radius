/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.workers;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.CDRData;
import com.radiusengine.aaa.data.customerData;
import com.radiusengine.aaa.db.DBAccountingDriver;
import com.radiusengine.aaa.db.DBAuthenticationDriver;
import com.radiusengine.aaa.db.DataSource;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccountingRequest;

public class RecordCDRProcessor implements Runnable {
    AccountingRequest acctRequest;
    ConcurrentHashMap acctFieldMapping;
    String PACKET_IDENTIFIER = null;
    CDRData liveSessionData = null;
    long custPackId = -1L;
    public static Logger log = LoggerFactory.getLogger("radiuslog");

    public RecordCDRProcessor(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, String PACKET_IDENTIFIER, CDRData liveSessionData, long custPackId) {
        this.acctRequest = acctRequest;
        this.acctFieldMapping = acctFieldMapping;
        this.PACKET_IDENTIFIER = PACKET_IDENTIFIER;
        this.liveSessionData = liveSessionData;
        this.custPackId = custPackId;
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

            String sessionId = this.attributeValueOrNull("Acct-Session-Id");
            String nasIP = this.attributeValueOrNull("NAS-IP-Address");
            String acctSessionTime = this.attributeValueOrNull("Acct-Session-Time");

            try (Connection conn = DataSource.getConnection()) {
                if (driver.isDupliacteStop(conn, strUsername, sessionId, nasIP, acctSessionTime)) {
                    log.info(this.PACKET_IDENTIFIER + "dropping the duplicate Stop");
                    return;
                }

                driver.insertCDR(conn, this.acctRequest, this.acctFieldMapping, this.liveSessionData);

                if (this.custPackId != -1L) {
                    this.closeSessionAndUpdateQuota(conn, driver);
                } else {
                    log.debug(this.PACKET_IDENTIFIER + "Not Processing the request as custpackid is null");
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET RecordCDRProcessor()" + e, e);
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

    /** Removes the live session and adds this session's usage to the customer's plan quota. */
    private void closeSessionAndUpdateQuota(Connection conn, DBAccountingDriver driver) throws Exception {
        log.info(this.PACKET_IDENTIFIER + "[STOP CALLED]");
        driver.deleteDBSesion(conn, this.acctRequest.getAttributeValue("Acct-Session-Id"), this.acctRequest.getAttributeValue("User-Name"));

        String strUsername;
        if (this.acctRequest.getAttribute(89) != null) {
            strUsername = this.acctRequest.getAttributeValue("Chargeable-User-Identity");
        } else {
            strUsername = this.acctRequest.getAttribute("User-Name").getAttributeValue();
        }

        strUsername = strUsername != null ? strUsername.trim() : null;
        DBAuthenticationDriver dbAuth = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        customerData custRetrunData = dbAuth.getDBCustomer(conn, strUsername);
        log.debug(this.PACKET_IDENTIFIER + "custReturnData  :" + custRetrunData);
        if (custRetrunData == null) {
            log.info(this.PACKET_IDENTIFIER + "OOPS Customer not found...");
            return;
        }

        DBAccountingDriver dbAcct = new DBAccountingDriver(this.PACKET_IDENTIFIER);
        dbAcct.populatePlanUsageDetails(conn, this.custPackId, custRetrunData);
        log.debug(this.PACKET_IDENTIFIER + "Customer Data is  :" + custRetrunData.toString());

        long download = this.readCounter(42, "Download not available");
        long upload = this.readCounter(43, "Upload not available");
        download += this.gigawordsAsBytes(52, 52, "Input Gigawords not preset");
        // the Output-Gigawords value (53) is only read when attribute 52 is present, as it always was
        upload += this.gigawordsAsBytes(52, 53, "Output Gigawords not preset");

        long downloadKb = download / 1024L;
        long uploadKb = upload / 1024L;
        Double totalSessionActualBytes = (double) (download + upload);
        long totalSessionActualKb = downloadKb + uploadKb;
        long sessiontime = 0L;

        try {
            sessiontime = (long) Integer.parseInt(this.acctRequest.getAttribute(46).getAttributeValue());
        } catch (Exception e) {
            log.info(this.PACKET_IDENTIFIER + "Session Time not present");
            sessiontime = 0L;
        }

        long totalSessionActualTime = sessiontime;
        String strQuotaUnit = custRetrunData.getQuotaunit();
        String strQuotaUnitTime = custRetrunData.getTimequotaunit();
        if (strQuotaUnit != null) {
            if (strQuotaUnit.equalsIgnoreCase("KB")) {
                totalSessionActualBytes = totalSessionActualBytes / 1024.0;
            }

            if (strQuotaUnit.equalsIgnoreCase("MB")) {
                totalSessionActualBytes = totalSessionActualBytes / 1024.0 / 1024.0;
            }

            if (strQuotaUnit.equalsIgnoreCase("GB")) {
                totalSessionActualBytes = totalSessionActualBytes / 1024.0 / 1024.0 / 1024.0;
            }
        }

        if (strQuotaUnitTime != null) {
            // the time unit is tested through strQuotaUnit (not strQuotaUnitTime) and throws when that is null, as before
            if (strQuotaUnit.equalsIgnoreCase("Min")) {
                totalSessionActualTime /= 60L;
            }

            if (strQuotaUnit.equalsIgnoreCase("Hours")) {
                totalSessionActualTime = totalSessionActualTime / 60L / 60L;
            }

            if (strQuotaUnit.equalsIgnoreCase("Days")) {
                totalSessionActualTime = totalSessionActualTime / 60L / 60L / 24L;
            }
        }

        boolean bBWDowndgraded = false;
        if (custRetrunData.isFUPPlan() && !Double.valueOf(custRetrunData.getTotalquotakb()).equals(AAAConstant.UNLIMITED_QUOTA)) {
            double dTotalQuotaKb = Double.valueOf(custRetrunData.getTotalquotakb());
            double dtotalSessionActualKb = Double.valueOf((double) totalSessionActualKb);
            double dtotalUsedQuotaKb = Double.valueOf(custRetrunData.getUsedquotakb());
            if (dtotalUsedQuotaKb + dtotalSessionActualKb >= dTotalQuotaKb) {
                bBWDowndgraded = true;
            }
        }

        log.debug(this.PACKET_IDENTIFIER + "custReturnData:" + custRetrunData);
        log.debug(this.PACKET_IDENTIFIER + "bBWDowndgraded:" + bBWDowndgraded);
        dbAuth.updateQuota(conn, totalSessionActualBytes, totalSessionActualKb, totalSessionActualTime, sessiontime, custRetrunData.getCustid(), strQuotaUnit, strQuotaUnitTime, this.custPackId, bBWDowndgraded);
    }

    /** Octet counter (42 = input, 43 = output); 0 when the packet does not carry it. */
    private long readCounter(int attributeType, String notAvailableMessage) {
        try {
            return Long.parseLong(this.acctRequest.getAttribute(attributeType).getAttributeValue());
        } catch (Exception e) {
            log.info(this.PACKET_IDENTIFIER + notAvailableMessage);
            return 0L;
        }
    }

    /** Gigaword counter converted to bytes (one gigaword = 4 GiB); 0 when it is absent. */
    private long gigawordsAsBytes(int presenceCheckType, int valueType, String notPresentMessage) {
        try {
            if (this.acctRequest.getAttribute(presenceCheckType) != null) {
                long gigawords = Long.parseLong(this.acctRequest.getAttribute(valueType).getAttributeValue());
                return gigawords * 4L * 1024L * 1024L * 1024L;
            }

            log.info(this.PACKET_IDENTIFIER + notPresentMessage);
        } catch (Exception e) {
            log.info(this.PACKET_IDENTIFIER + notPresentMessage);
        }

        return 0L;
    }
}
