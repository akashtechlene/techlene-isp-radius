////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package com.radiusengine.bbb.server;
//
//import com.radiusengine.bbb.data.CDRData;
//import com.radiusengine.bbb.data.customerData;
//import com.radiusengine.bbb.db.DBAccountingDriver;
//import com.radiusengine.bbb.db.DBAuthenticationDriver;
//import com.radiusengine.bbb.util.RSAUtility;
//import com.radiusengine.bbb.workers.RecordAcctStartProcessor;
//import com.radiusengine.bbb.workers.RecordCDRProcessor;
//import java.util.ArrayList;
//import java.util.TreeMap;
//import java.util.concurrent.ConcurrentHashMap;
//import net.sf.ehcache.Cache;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.tinyradius.packet.AccountingRequest;
//import org.tinyradius.packet.RadiusPacket;
//import org.tinyradius.threadpool.RadiusCOAPool;
//import org.tinyradius.threadpool.RadiusRecordCDRPool;
//import org.tinyradius.workers.RadiusCoAProcessor;
//
//public class RadiusAcctImpl {
//    Logger log = LoggerFactory.getLogger("radiuslog");
//    Logger reqreslog = LoggerFactory.getLogger("reqresplog");
//    String PACKET_IDENTIFIER = "";
//    AccountingRequest request;
//    RadiusPacket accoutningResponse;
//    String strCDRSession;
//    String strSessionMgr;
//    ConcurrentHashMap acctFieldMapping;
//    long lngStartTime = 0L;
//
//    public RadiusAcctImpl() {
//    }
//
//    public RadiusAcctImpl(AccountingRequest request, RadiusPacket accoutningResponse, String strCDRSession, String strSessionMgr, ConcurrentHashMap acctFieldMapping) {
//        this.request = request;
//        this.accoutningResponse = accoutningResponse;
//        this.strCDRSession = strCDRSession;
//        this.strSessionMgr = strSessionMgr;
//        this.acctFieldMapping = acctFieldMapping;
//        String strUsername = null;
//
//        try {
//            strUsername = request.getAttribute("User-Name").getAttributeValue();
//            strUsername = strUsername != null ? strUsername.trim() : null;
//        } catch (Exception var12) {
//            this.log.info("User-Name Not Found");
//        }
//
//        Long lngPacketId = null;
//
//        try {
//            lngPacketId = (long)request.getPacketIdentifier();
//        } catch (Exception var11) {
//            lngPacketId = System.currentTimeMillis();
//        }
//
//        String sessionId = null;
//
//        try {
//            sessionId = request.getAttribute("Acct-Session-Id").getAttributeValue();
//        } catch (Exception var10) {
//            sessionId = String.valueOf(System.currentTimeMillis());
//        }
//
//        this.lngStartTime = System.currentTimeMillis();
//        this.PACKET_IDENTIFIER = "[" + strUsername + "][" + lngPacketId + "][" + sessionId + "][RADACCTIMPL]";
//    }
//
//    public void processAcctPacket() {
//        try {
//            this.log.debug(this.PACKET_IDENTIFIER + "Radius Accounting " + this.request.getAttributeValue("Acct-Status-Type") + " Request ,Store Session:" + this.strCDRSession + ",Session Manager:" + this.strSessionMgr + ":" + this.request);
//            CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
//            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache("serverConfig");
//            Cache cacheServerConfgAttribes = CachingConfigurations.getCacheManagerData().getCache("serverAttribe");
//            boolean acceptAll = false;
//
//            try {
//                acceptAll = Boolean.parseBoolean(cacheServerAttribe.get("accept_all").getObjectValue().toString());
//            } catch (Exception var23) {
//                acceptAll = false;
//            }
//
//            if (acceptAll) {
//                this.accoutningResponse.setPacketType(5);
//                long endTime = System.currentTimeMillis();
//                this.log.info(this.PACKET_IDENTIFIER + "ACCT ACCEPT-ALL Time to Final Respond:" + (endTime - this.lngStartTime));
//                return;
//            }
//
//            if (!this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Start") && !this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Stop") && !this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Alive") && !this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Interim-Update")) {
//                if (!this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Accounting-On") && !this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Accounting-Off")) {
//                    this.log.debug(this.PACKET_IDENTIFIER + "Dropping Unsupported Accounting Packet:" + this.request.getAttributeValue("Acct-Status-Type"));
//                } else {
//                    this.log.debug(this.PACKET_IDENTIFIER + " Accounting On/Off Response " + this.request.getAttributeValue("Acct-Status-Type"));
//                    this.accoutningResponse.setPacketType(5);
//                }
//            } else {
//                DBAccountingDriver dbAccountingDrive = new DBAccountingDriver(this.PACKET_IDENTIFIER);
//                if (this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Stop") && this.strSessionMgr.equalsIgnoreCase("N") && this.strCDRSession.equalsIgnoreCase("Y")) {
//                    RadiusRecordCDRPool.getInstance().getExecutor().execute(new RecordCDRProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER, (CDRData)null, -1L));
//                }
//
//                if (this.strSessionMgr.equalsIgnoreCase("Y")) {
//                    String classValue = null;
//                    boolean isUnlimitedQuota = false;
//                    boolean isSpeedDowngradeFUP = false;
//                    long custPackRelId = -1L;
//
//                    try {
//                        classValue = this.request.getAttribute(25).getAttributeValue();
//                    } catch (Exception var22) {
//                        classValue = null;
//                    }
//
//                    TreeMap<String, String> classParamMap = null;
//                    Exception e;
//                    DBAuthenticationDriver cdrData;
//                    String sessionId;
//                    if (classValue != null) {
//                        try {
//                            classParamMap = RSAUtility.decrypt("NeoFruition@2021", classValue);
//                        } catch (Exception var21) {
//                            e = var21;
//                            this.log.error(this.PACKET_IDENTIFIER + "Error parsing classMap:" + e, e);
//                            classParamMap = null;
//                        }
//
//                        if (classParamMap != null) {
//                            if (classParamMap.containsKey("custpackid") && classParamMap.get("custpackid") != null) {
//                                custPackRelId = Long.parseLong((String)classParamMap.get("custpackid"));
//                            }
//
//                            if (classParamMap.containsKey("qu") && classParamMap.get("qu") != null && "1".equalsIgnoreCase((String)classParamMap.get("qu"))) {
//                                isUnlimitedQuota = true;
//                            }
//
//                            if (classParamMap.containsKey("sd") && classParamMap.get("sd") != null && "1".equalsIgnoreCase((String)classParamMap.get("sd"))) {
//                                isSpeedDowngradeFUP = true;
//                            }
//                        }
//                    } else {
//                        e = null;
//
//                        Integer ENABLE_NON_CLASSATTR_ACCCT;
//                        try {
//                            ENABLE_NON_CLASSATTR_ACCCT = Integer.parseInt(cacheServerConfgAttribes.get("ENABLE_NON_CLASSATTR_ACCCT").getObjectValue().toString());
//                        } catch (Exception var20) {
//                            e = var20;
//                            this.log.error(this.PACKET_IDENTIFIER + "Error Reading ENABLE_NON_CLASSATTR_ACCCT:" + e, e);
//                            ENABLE_NON_CLASSATTR_ACCCT = null;
//                        }
//
//                        this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT:" + ENABLE_NON_CLASSATTR_ACCCT);
//                        sessionId = null;
//
//                        try {
//                            sessionId = this.request.getAttribute("User-Name").getAttributeValue();
//                            sessionId = sessionId != null ? sessionId.trim() : null;
//                        } catch (Exception var19) {
//                            this.log.info(this.PACKET_IDENTIFIER + "User-Name Not Found");
//                        }
//
//                        if (ENABLE_NON_CLASSATTR_ACCCT != null && ENABLE_NON_CLASSATTR_ACCCT.equals(1) && sessionId != null) {
//                            this.log.debug(this.PACKET_IDENTIFIER + "Checking customer package using ongoing package query");
//                            cdrData = new DBAuthenticationDriver();
//                            customerData custdata = cdrData.getCustCurrentPlanDetails(sessionId.trim());
//                            this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, getCustCurrentPlanDetails, Custdata:" + custdata);
//                            if (custdata == null) {
//                                this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, getCustCurrentPlanDetails, NO RESULT FOUND");
//                                custdata = cdrData.getCustLastActivePlanDetails(sessionId.trim());
//                                this.log.debug(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, getCustLastActivePlanDetails, Custdata:" + custdata);
//                            }
//
//                            if (custdata != null) {
//                                custPackRelId = (long)custdata.getCustPlanRelId();
//                            } else {
//                                this.log.info(this.PACKET_IDENTIFIER + "ENABLE_NON_CLASSATTR_ACCCT, Customer not found");
//                            }
//                        }
//                    }
//
//                    this.log.debug(this.PACKET_IDENTIFIER + "Class Map:" + classParamMap);
//                    this.log.debug(this.PACKET_IDENTIFIER + "custPackRelId:" + custPackRelId);
//                    if (custPackRelId != -1L) {
//                        if (this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Start")) {
//                            this.log.info(this.PACKET_IDENTIFIER + "[START CALLED]");
//                            RecordAcctStartProcessor processor = new RecordAcctStartProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER);
//                            processor.run();
//                        } else if (this.request.getAttributeValue("Acct-Status-Type").equalsIgnoreCase("Stop")) {
//                            CDRData liveSessionData = null;
//
//                            try {
//                                e = null;
//
//                                try {
//                                    sessionId = this.request.getAttribute("Acct-Session-Id").getAttributeValue();
//                                } catch (Exception var17) {
//                                    sessionId = null;
//                                }
//
//                                if (this.request.getAttributeValue("Acct-Terminate-Cause") != null && this.request.getAttributeValue("Acct-Terminate-Cause").equalsIgnoreCase("Admin-Reset")) {
//                                    try {
//                                        if (sessionId != null) {
//                                            liveSessionData = dbAccountingDrive.getLiveUserSessionDetails(sessionId);
//                                        }
//                                    } catch (Exception var16) {
//                                        this.log.error("Error processing Stop Admin-Reset Scenario:" + var16, var16);
//                                    }
//                                }
//                            } catch (Exception var18) {
//                                e = var18;
//                                this.log.error(this.PACKET_IDENTIFIER + "Error processing Stop Admin-Reset Scenario:" + e, e);
//                            }
//
//                            RadiusRecordCDRPool.getInstance().getExecutor().execute(new RecordCDRProcessor(this.request, this.acctFieldMapping, this.PACKET_IDENTIFIER, liveSessionData, custPackRelId));
//                        } else {
//                            this.log.info(this.PACKET_IDENTIFIER + "[INTERIM CALLED]");
//                            dbAccountingDrive.updateDBSesion(this.request, this.acctFieldMapping);
//                            String coaOperation = CachingConfigurations.getCachedValue("serverConfig", "coaOperation").toString();
//                            if (custPackRelId != -1L && custPackRelId != -2L && !isUnlimitedQuota && !isSpeedDowngradeFUP) {
//                                ArrayList cdrList = dbAccountingDrive.getCustomerCoAStatus(this.request.getAttributeValue("User-Name"), custPackRelId);
//                                if (cdrList != null) {
//                                    this.log.info(this.PACKET_IDENTIFIER + "COA triggered");
//                                    cdrData = null;
//
//                                    for(int i = 0; i < cdrList.size(); ++i) {
//                                        com.radiusengine.aaa.data.CDRData cdrData1 = (com.radiusengine.aaa.data.CDRData) cdrList.get(i);
//                                        RadiusCOAPool.getCoaPool().getCOAExecutor().execute(new RadiusCoAProcessor(this.PACKET_IDENTIFIER, cdrData1, coaOperation));
//                                    }
//                                }
//                            } else {
//                                this.log.info(this.PACKET_IDENTIFIER + "COA status check skipped::custPackRelId:" + custPackRelId + ",isUnlimitedQuota:" + isUnlimitedQuota + ",isSpeedDowngradeFUP:" + isSpeedDowngradeFUP);
//                            }
//                        }
//                    } else {
//                        this.log.info(this.PACKET_IDENTIFIER + "custPackRelId Not found hence Skipping the request");
//                    }
//                } else {
//                    this.log.debug(this.PACKET_IDENTIFIER + "Radius Accounting Request Packet is : " + this.request + ":Type:" + this.request.getAttributeValue("Acct-Status-Type") + " Store Session" + this.strCDRSession);
//                }
//
//                this.accoutningResponse.setPacketType(5);
//            }
//        } catch (Exception var24) {
//            Exception e = var24;
//            this.log.error(this.PACKET_IDENTIFIER + "Error Processing Accounting Request:" + e, e);
//        }
//
//        long endTime = System.currentTimeMillis();
//        this.log.info(this.PACKET_IDENTIFIER + "ACCT Time to Final Respond:" + (endTime - this.lngStartTime));
//    }
//
//    public void disconnectSession(String sessionId) {
//        this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSession(), sessionId:" + sessionId);
//
//        try {
//            DBAccountingDriver dbDriver = new DBAccountingDriver(this.PACKET_IDENTIFIER);
//            CDRData data = dbDriver.getLiveSessionDetails(sessionId);
//            if (data != null) {
//                this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSession(), Live Session Found, sessionId:" + sessionId);
//                ArrayList<CDRData> dataList = new ArrayList();
//                dataList.add(data);
//                this.disconnectSessions(dataList);
//            } else {
//                this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSession(), Live Session Not Found, sessionId:" + sessionId);
//            }
//        } catch (Exception var5) {
//            Exception e = var5;
//            this.log.error(this.PACKET_IDENTIFIER + "Error Into disconnectSession():" + e, e);
//        }
//
//    }
//
//    public void disconnectUserSessions(String userName) {
//        if (userName != null) {
//            this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), userName:" + userName);
//
//            try {
//                DBAccountingDriver dbDriver = new DBAccountingDriver(this.PACKET_IDENTIFIER);
//                ArrayList<CDRData> cdrList = dbDriver.getAllLiveSessions(userName);
//                this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), userName:" + userName + ",Live Sessions:" + cdrList.size());
//                this.disconnectSessions(cdrList);
//            } catch (Exception var4) {
//                Exception e = var4;
//                this.log.error(this.PACKET_IDENTIFIER + "Error Into disconnectSessions(),userName:" + e, e);
//            }
//        } else {
//            this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), userName is null");
//        }
//
//    }
//
//    public void disconnectSessions(ArrayList<CDRData> cdrList) {
//        if (cdrList != null) {
//            this.log.debug(this.PACKET_IDENTIFIER + "Into disconnectSessions(), records:" + cdrList.size());
//            String coaOperation = CachingConfigurations.getCachedValue("serverConfig", "coaOperation").toString();
//
//            try {
//                for(int i = 0; i < cdrList.size(); ++i) {
//                    CDRData cdrData = (CDRData)cdrList.get(i);
//                    RadiusCOAPool.getCoaPool().getCOAExecutor().execute(new RadiusCoAProcessor(this.PACKET_IDENTIFIER, cdrData, coaOperation));
//                }
//            } catch (Exception var5) {
//                Exception e = var5;
//                this.log.error(this.PACKET_IDENTIFIER + "Error Into disconnectSessions():" + e, e);
//            }
//        }
//
//    }
//}
