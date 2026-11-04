/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.server;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.CDRData;
import com.radiusengine.aaa.data.customerData;
import com.radiusengine.aaa.db.DBAccountingDriver;
import com.radiusengine.aaa.db.DBAuthenticationDriver;
import com.radiusengine.aaa.db.DataSource;
import com.radiusengine.aaa.util.RSAUtility;
import com.radiusengine.aaa.workers.RecordAcctInterimProcessor;
import com.radiusengine.aaa.workers.RecordAcctStartProcessor;
import com.radiusengine.aaa.workers.RecordCDRProcessor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.threadpool.CommonThreadPool;
import org.tinyradius.threadpool.RadiusCOAPool;
import org.tinyradius.threadpool.RadiusRecordCDRPool;
import org.tinyradius.workers.RadiusCoAProcessor;

public class RadiusAcctImpl {
    Logger log = LoggerFactory.getLogger("radiuslog");
    Logger reqreslog = LoggerFactory.getLogger("reqresplog");
    String PACKET_IDENTIFIER = "";
    AccountingRequest request;
    RadiusPacket accoutningResponse;
    String strCDRSession;
    String strSessionMgr;
    ConcurrentHashMap acctFieldMapping;
    long lngStartTime = 0L;

    public RadiusAcctImpl() {
    }

    public RadiusAcctImpl(AccountingRequest request, RadiusPacket accoutningResponse, String strCDRSession, String strSessionMgr, ConcurrentHashMap acctFieldMapping) {
        this.request = request;
        this.accoutningResponse = accoutningResponse;
        this.strCDRSession = strCDRSession;
        this.strSessionMgr = strSessionMgr;
        this.acctFieldMapping = acctFieldMapping;
        String strUsername = null;

        try {
            strUsername = request.getAttribute("User-Name").getAttributeValue();
            strUsername = strUsername != null ? strUsername.trim() : null;
        } catch (Exception var12) {
            this.log.info("User-Name Not Found");
        }

        Long lngPacketId = null;

        try {
            lngPacketId = (long) request.getPacketIdentifier();
        } catch (Exception var11) {
            lngPacketId = System.currentTimeMillis();
        }

        String sessionId = null;

        try {
            sessionId = request.getAttribute("Acct-Session-Id").getAttributeValue();
        } catch (Exception var10) {
            sessionId = String.valueOf(System.currentTimeMillis());
        }

        this.lngStartTime = System.currentTimeMillis();
        this.PACKET_IDENTIFIER = "[" + strUsername + "][" + lngPacketId + "][" + sessionId + "][RADACCTIMPL]";
    }

    public void processAcctPacket() {
        try {
            String statusType = this.request.getAttributeValue("Acct-Status-Type");
            this.log.debug(this.PACKET_IDENTIFIER + "Radius Accounting " + statusType + " Request ,Store Session:" + this.strCDRSession + ",Session Manager:" + this.strSessionMgr + ":" + this.request);
            CachingConfigurations.getInstance(); // makes sure the cache is initialised
            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCACHING);
            Cache cacheServerConfgAttribes = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
            boolean acceptAll = false;

            try {
                acceptAll = Boolean.parseBoolean(cacheServerAttribe.get(AAAConstant.ACCEPT_ALL).getObjectValue().toString());
            } catch (Exception e) {
                acceptAll = false;
            }

            if (acceptAll) {
                this.accoutningResponse.setPacketType(5);
                long endTime = System.currentTimeMillis();
                this.log.info(this.PACKET_IDENTIFIER + "ACCT ACCEPT-ALL Time to Final Respond:" + (endTime - this.lngStartTime));
                return;
            }

            // a missing Acct-Status-Type throws here, exactly as before, and is logged by the catch below
            boolean sessionPacket = statusType.equalsIgnoreCase("Start") || statusType.equalsIgnoreCase("Stop")
                    || statusType.equalsIgnoreCase("Alive") || statusType.equalsIgnoreCase("Interim-Update");

            if (sessionPacket) {
                this.processSessionPacket(statusType, cacheServerConfgAttribes);
            } else if (statusType.equalsIgnoreCase("Accounting-On") || statusType.equalsIgnoreCase("Accounting-Off")) {
                this.log.debug(this.PACKET_IDENTIFIER + " Accounting On/Off Response " + statusType);
                this.accoutningResponse.setPacketType(5);
            } else {
                this.log.debug(this.PACKET_IDENTIFIER + "Dropping Unsupported Accounting Packet:" + statusType);
            }
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error Processing Accounting Request:" + e, e);
        }

        long endTime = System.currentTimeMillis();
        this.log.info(this.PACKET_IDENTIFIER + "ACCT Time to Final Respond:" + (endTime - this.lngStartTime));
    }

    /** Start / Stop / Interim-Update (Alive) handling; answers with Accounting-Response when it completes. */
    private void processSessionPacket(String statusType, Cache cacheServerConfgAttribes) {
        DBAccountingDriver dbAccountingDrive = new DBAccountingDriver(this.PACKET_IDENTIFIER);
        if (statusType.equalsIgnoreCase("Stop") && this.strSessionMgr.equalsIgnoreCase("N") && this.strCDRSession.equalsIgnoreCase("Y")) {
            RadiusRecordCDRPool.getInstance().getExecutor()
                    .execute(new RecordCDRProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER, null, -1L));
        }

        if (this.strSessionMgr.equalsIgnoreCase("Y")) {
            boolean isUnlimitedQuota = false;
            boolean isSpeedDowngradeFUP = false;
            long custPackRelId = -1L;
            String classValue = this.readClassAttribute();
            TreeMap<String, String> classParamMap = null;

            if (classValue != null) {
                try {
                    classParamMap = RSAUtility.decrypt(RSAUtility.KEY, classValue);
                } catch (Exception e) {
                    this.log.error(this.PACKET_IDENTIFIER + "Error parsing classMap:" + e, e);
                    classParamMap = null;
                }

                if (classParamMap != null) {
                    if (classParamMap.get("custpackid") != null) {
                        custPackRelId = Long.parseLong(classParamMap.get("custpackid"));
                    }

                    isUnlimitedQuota = "1".equalsIgnoreCase(classParamMap.get("qu"));
                    isSpeedDowngradeFUP = "1".equalsIgnoreCase(classParamMap.get("sd"));
                }
            } else {
                custPackRelId = this.findCustPackRelIdWithoutClassAttribute(cacheServerConfgAttribes);
            }

            this.log.debug(this.PACKET_IDENTIFIER + "Class Map:" + classParamMap);
            this.log.debug(this.PACKET_IDENTIFIER + "custPackRelId:" + custPackRelId);
            if (custPackRelId != -1L) {
                if (statusType.equalsIgnoreCase("Start")) {
                    this.log.info(this.PACKET_IDENTIFIER + "[START CALLED]");
                    new RecordAcctStartProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER).run();
                } else if (statusType.equalsIgnoreCase("Stop")) {
                    this.processStop(dbAccountingDrive, custPackRelId);
                } else {
                    CommonThreadPool.getInstance().getExecutor().execute(new RecordAcctInterimProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER, custPackRelId, isUnlimitedQuota, isSpeedDowngradeFUP));
                }
            } else {
                this.log.info(this.PACKET_IDENTIFIER + "custPackRelId Not found hence Skipping the request");
            }
        } else {
            this.log.debug(this.PACKET_IDENTIFIER + "Radius Accounting Request Packet is : " + this.request + ":Type:" + statusType + " Store Session" + this.strCDRSession);
        }

        this.accoutningResponse.setPacketType(5);
    }

    /** Returns the value of the Class attribute (25), or null when the packet does not carry it. */
    private String readClassAttribute() {
        try {
            return this.request.getAttribute(25).getAttributeValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Packets without a Class attribute: when ENABLE_NON_CLASSATTR_ACCCT is 1 the customer package is looked up
     * by user name (current plan first, then last active plan). Returns -1 when it cannot be found.
     */
    private long findCustPackRelIdWithoutClassAttribute(Cache cacheServerConfgAttribes) {
        long custPackRelId = -1L;
        Integer enableNonClassAttrAcct;
        try {
            enableNonClassAttrAcct = Integer.parseInt(cacheServerConfgAttribes.get("ENABLE_NON_CLASSATTR_ACCCT").getObjectValue().toString());
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error Reading ENABLE_NON_CLASSATTR_ACCCT:" + e, e);
            enableNonClassAttrAcct = null;
        }

        this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT:" + enableNonClassAttrAcct);
        String userName = null;

        try {
            userName = this.request.getAttribute("User-Name").getAttributeValue();
            userName = userName != null ? userName.trim() : null;
        } catch (Exception e) {
            this.log.info(this.PACKET_IDENTIFIER + "User-Name Not Found");
        }

        if (enableNonClassAttrAcct != null && enableNonClassAttrAcct.equals(1) && userName != null) {
            this.log.debug(this.PACKET_IDENTIFIER + "Checking customer package using ongoing package query");
            DBAuthenticationDriver authDriver = new DBAuthenticationDriver();
            customerData custdata = authDriver.getCustCurrentPlanDetails(userName.trim());
            this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, getCustCurrentPlanDetails, Custdata:" + custdata);
            if (custdata == null) {
                this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, getCustCurrentPlanDetails, NO RESULT FOUND");
                custdata = authDriver.getCustLastActivePlanDetails(userName.trim());
                this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, getCustLastActivePlanDetails, Custdata:" + custdata);
            }

            if (custdata != null) {
                custPackRelId = (long) custdata.getCustPlanRelId();
            } else {
                this.log.info(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, Customer not found");
            }
        }

        return custPackRelId;
    }

    /** Stop: on Admin-Reset the live session row is read first so the CDR keeps the original create date. */
    private void processStop(DBAccountingDriver dbAccountingDrive, long custPackRelId) {
        CDRData liveSessionData = null;

        try {
            String sessionId;
            try {
                sessionId = this.request.getAttribute("Acct-Session-Id").getAttributeValue();
            } catch (Exception e) {
                sessionId = null;
            }

            if (this.request.getAttributeValue("Acct-Terminate-Cause") != null
                    && this.request.getAttributeValue("Acct-Terminate-Cause").equalsIgnoreCase("Admin-Reset")) {
                try {
                    if (sessionId != null) {
                        liveSessionData = dbAccountingDrive.getLiveUserSessionDetails(sessionId);
                    }
                } catch (Exception e) {
                    this.log.error("Error processing Stop Admin-Reset Scenario:" + e, e);
                }
            }
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error processing Stop Admin-Reset Scenario:" + e, e);
        }

        RadiusRecordCDRPool.getInstance().getExecutor().execute(new RecordCDRProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER, liveSessionData, custPackRelId));
    }

    public void disconnectSession(String sessionId) {
        this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSession(), sessionId:" + sessionId);

        try {
            DBAccountingDriver dbDriver = new DBAccountingDriver(this.PACKET_IDENTIFIER);
            CDRData data = dbDriver.getLiveSessionDetails(sessionId);
            if (data != null) {
                this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSession(), Live Session Found, sessionId:" + sessionId);
                ArrayList<CDRData> dataList = new ArrayList<>();
                dataList.add(data);
                this.disconnectSessions(dataList);
            } else {
                this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSession(), Live Session Not Found, sessionId:" + sessionId);
            }
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error Into disconnectSession():" + e, e);
        }

    }

    public void disconnectUserSessions(String userName) {
        if (userName == null) {
            this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), userName is null");
            return;
        }

        this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), userName:" + userName);

        try {
            DBAccountingDriver dbDriver = new DBAccountingDriver(this.PACKET_IDENTIFIER);
            ArrayList<CDRData> cdrList = dbDriver.getAllLiveSessions(userName);
            this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), userName:" + userName + ",Live Sessions:" + cdrList.size());
            this.disconnectSessions(cdrList);
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error Into disconnectSessions(),userName:" + e, e);
        }

    }

    public void disconnectSessions(ArrayList<CDRData> cdrList) {
        if (cdrList == null) {
            return;
        }

        this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), records:" + cdrList.size());
        String coaOperation = CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "coaOperation").toString();

        try {
            for (CDRData cdrData : cdrList) {
                RadiusCOAPool.getCoaPool().getCOAExecutor().execute(new RadiusCoAProcessor(this.PACKET_IDENTIFIER, cdrData, coaOperation));
            }
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error Into disconnectSessions():" + e, e);
        }

    }
}
