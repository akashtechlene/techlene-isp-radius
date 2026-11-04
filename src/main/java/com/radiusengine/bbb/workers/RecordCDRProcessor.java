//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.bbb.workers;

import com.radiusengine.bbb.constant.AAAConstant;
import com.radiusengine.bbb.data.CDRData;
import com.radiusengine.bbb.data.customerData;
import com.radiusengine.bbb.db.DBAccountingDriver;
import com.radiusengine.bbb.db.DBAuthenticationDriver;
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
            } catch (Exception var41) {
                log.info("User-Name Not Found");
            }

            String sessionId = null;

            try {
                sessionId = this.acctRequest.getAttribute("Acct-Session-Id").getAttributeValue();
            } catch (Exception var40) {
                sessionId = null;
            }

            String nasIP = null;

            try {
                nasIP = this.acctRequest.getAttribute("NAS-IP-Address").getAttributeValue();
            } catch (Exception var39) {
                nasIP = null;
            }

            String acctSessionTime = null;

            try {
                acctSessionTime = this.acctRequest.getAttribute("Acct-Session-Time").getAttributeValue();
            } catch (Exception var38) {
                acctSessionTime = null;
            }

            if (!driver.isDupliacteStop(strUsername, sessionId, nasIP, acctSessionTime)) {
                driver.insertCDR(this.acctRequest, this.acctFieldMapping, this.liveSessionData);
                if (this.custPackId != -1L) {
                    log.info(this.PACKET_IDENTIFIER + "[STOP CALLED]");
                    driver.deleteDBSesion(this.acctRequest.getAttributeValue("Acct-Session-Id"), this.acctRequest.getAttributeValue("User-Name"));
                    if (this.acctRequest.getAttribute(89) != null) {
                        strUsername = this.acctRequest.getAttributeValue("Chargeable-User-Identity");
                    } else {
                        strUsername = this.acctRequest.getAttribute("User-Name").getAttributeValue();
                    }

                    strUsername = strUsername != null ? strUsername.trim() : null;
                    DBAuthenticationDriver dbAuth = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
                    customerData custRetrunData = dbAuth.getDBCustomer(strUsername);
                    log.debug(this.PACKET_IDENTIFIER + "custReturnData  :" + custRetrunData);
                    if (custRetrunData != null) {
                        DBAccountingDriver dbAcct = new DBAccountingDriver(this.PACKET_IDENTIFIER);
                        dbAcct.populatePlanUsageDetails(this.custPackId, custRetrunData);
                        log.debug(this.PACKET_IDENTIFIER + "Customer Data is  :" + custRetrunData.toString());
                        long download = 0L;
                        long upload = 0L;

                        try {
                            download = Long.parseLong(this.acctRequest.getAttribute(42).getAttributeValue());
                        } catch (Exception var37) {
                            log.info(this.PACKET_IDENTIFIER + "Download not available");
                        }

                        try {
                            upload = Long.parseLong(this.acctRequest.getAttribute(43).getAttributeValue());
                        } catch (Exception var36) {
                            log.info(this.PACKET_IDENTIFIER + "Upload not available");
                        }

                        long uploadGB;
                        try {
                            if (this.acctRequest.getAttribute(52) != null) {
                                uploadGB = Long.parseLong(this.acctRequest.getAttribute(52).getAttributeValue());
                                download += uploadGB * 4L * 1024L * 1024L * 1024L;
                            } else {
                                log.info(this.PACKET_IDENTIFIER + "Input Gigawords not preset");
                            }
                        } catch (Exception var35) {
                            log.info(this.PACKET_IDENTIFIER + "Input Gigawords not preset");
                        }

                        try {
                            if (this.acctRequest.getAttribute(52) != null) {
                                uploadGB = Long.parseLong(this.acctRequest.getAttribute(53).getAttributeValue());
                                upload += uploadGB * 4L * 1024L * 1024L * 1024L;
                            } else {
                                log.info(this.PACKET_IDENTIFIER + "Output Gigawords not preset");
                            }
                        } catch (Exception var34) {
                            log.info(this.PACKET_IDENTIFIER + "Output Gigawords not preset");
                        }

                        uploadGB = download / 1024L;
                        long uploadkb = upload / 1024L;
                        Double totalSessionActualBytes = (double)(download + upload);
                        long totalSessionActualKb = uploadGB + uploadkb;
                        long sessiontime = 0L;

                        try {
                            sessiontime = (long)Integer.parseInt(this.acctRequest.getAttribute(46).getAttributeValue());
                        } catch (Exception var33) {
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
                            double dtotalSessionActualKb = Double.valueOf((double)totalSessionActualKb);
                            double dtotalUsedQuotaKb = Double.valueOf(custRetrunData.getUsedquotakb());
                            if (dtotalUsedQuotaKb + dtotalSessionActualKb >= dTotalQuotaKb) {
                                bBWDowndgraded = true;
                            }
                        }

                        log.debug(this.PACKET_IDENTIFIER + "custReturnData:" + custRetrunData);
                        log.debug(this.PACKET_IDENTIFIER + "bBWDowndgraded:" + bBWDowndgraded);
                        dbAuth.updateQuota(totalSessionActualBytes, totalSessionActualKb, totalSessionActualTime, sessiontime, custRetrunData.getCustid(), strQuotaUnit, strQuotaUnitTime, this.custPackId, bBWDowndgraded);
                    } else {
                        log.info(this.PACKET_IDENTIFIER + "OOPS Customer not found...");
                    }
                } else {
                    log.debug(this.PACKET_IDENTIFIER + "Not Processing the request as custpackid is null");
                }
            } else {
                log.info(this.PACKET_IDENTIFIER + "dropping the duplicate Stop");
            }
        } catch (Exception var42) {
            Exception e = var42;
            log.error(this.PACKET_IDENTIFIER + "Error IN GET RecordCDRProcessor()" + e, e);
        }

    }
}
