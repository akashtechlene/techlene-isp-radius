/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.server;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.CustomerAttribute;
import com.radiusengine.aaa.data.ReplyItemData;
import com.radiusengine.aaa.data.ServerConfData;
import com.radiusengine.aaa.data.clientData;
import com.radiusengine.aaa.data.customerData;
import com.radiusengine.aaa.db.DBAuthenticationDriver;
import com.radiusengine.aaa.db.DataSource;
import com.radiusengine.aaa.util.RSAUtility;
import com.radiusengine.aaa.util.ValidateExpression;
import com.radiusengine.aaa.workers.AuthAuditProcessor;
import com.radiusengine.aaa.workers.CustAttribWorker;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.attribute.IntegerAttribute;
import org.tinyradius.attribute.IpAttribute;
import org.tinyradius.attribute.Ipv6PrefixAttribute;
import org.tinyradius.attribute.LongAttribute;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.dictionary.DefaultDictionary;
import org.tinyradius.dictionary.Dictionary;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.threadpool.CommonThreadPool;
import org.tinyradius.threadpool.RadiusAuthAuditPool;

public class RadiusAuthImpl {
    Logger log = LoggerFactory.getLogger("radiuslog");
    String PACKET_IDENTIFIER = "";
    AccessRequest request;
    InetSocketAddress client;
    long lngStartTime = 0L;

    public RadiusAuthImpl(AccessRequest request, InetSocketAddress client) {
        this.request = request;
        this.client = client;
        String strUsername = null;

        try {
            strUsername = request.getAttribute("User-Name").getAttributeValue();
            strUsername = strUsername != null ? strUsername.trim() : null;
        } catch (Exception var9) {
            this.log.info("User-Name Not Found");
        }

        Long lngPacketId = null;

        try {
            lngPacketId = (long)request.getPacketIdentifier();
        } catch (Exception var8) {
            lngPacketId = System.currentTimeMillis();
        }

        String sessionId = null;

        try {
            sessionId = request.getAttribute("Acct-Session-Id").getAttributeValue();
        } catch (Exception var7) {
            sessionId = String.valueOf(System.currentTimeMillis());
        }

        this.lngStartTime = System.currentTimeMillis();
        this.PACKET_IDENTIFIER = "[" + strUsername + "][" + lngPacketId + "][" + sessionId + "][RADAUTHIMPL]";
    }

    public RadiusPacket processAccessRquest() {
        RadiusPacket accessResponse = new RadiusPacket(3, this.request.getPacketIdentifier());

        try {
            CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCACHING);
            boolean acceptAll = false;

            try {
                acceptAll = Boolean.parseBoolean(cacheServerAttribe.get(AAAConstant.ACCEPT_ALL).getObjectValue().toString());
            } catch (Exception var14) {
                acceptAll = false;
            }

            RadiusServerImpl impl = new RadiusServerImpl();

            try {
                clientData cltData = impl.identifyClient(this.client.getAddress().toString());
                this.request.removeAttributes(77);
                this.request.addAttribute("Connect-Info", cltData.getGroupname());
            } catch (Exception var13) {
                Exception e = var13;
                this.log.error(this.PACKET_IDENTIFIER + "Error in processAccessRquest() : " + e, e);
            }

            this.log.debug(this.PACKET_IDENTIFIER + "Access Request Packet is : " + this.request);
            String strUsername = this.request.getAttribute("User-Name").getAttributeValue();
            String strPassword = this.request.getUserPassword();
            strUsername = strUsername != null ? strUsername.trim() : null;
            try (Connection conn = DataSource.getConnection()) {
            customerData custRetrunData = this.authenticateCustomer(conn, strUsername, strPassword);
            this.log.debug(this.PACKET_IDENTIFIER + "Customer Data is  :" + custRetrunData.toString());
            HashMap hsAuthRes;
            if (custRetrunData.isAuthStatus() && acceptAll) {
                custRetrunData.setPACKET_IDENTIFIER(this.PACKET_IDENTIFIER);
                accessResponse.setPacketType(2);
                this.processReplyItem(accessResponse, custRetrunData, this.client.getAddress().toString(), this.request);
                custRetrunData.setStrReplyMessage("Welcome-All");
                accessResponse.addAttribute("Reply-Message", custRetrunData.getStrReplyMessage());
                hsAuthRes = new HashMap();
                hsAuthRes.put("username", this.request.getUserName());
                hsAuthRes.put("replymessage", AAAConstant.REPLYMSG);
                hsAuthRes.put("packettype", Integer.toString(accessResponse.getPacketType()));
                hsAuthRes.put(AAAConstant.CLIENTIP, this.client.getAddress().toString());
                RadiusAuthAuditPool.getInstance().getExecutor().execute(new AuthAuditProcessor(hsAuthRes, this.PACKET_IDENTIFIER));
                this.log.debug(this.PACKET_IDENTIFIER + "Final Data Dumping Username :" + this.request.getUserName() + ":Message:" + AAAConstant.REPLYMSG + "-All:Type:" + accessResponse.getPacketType() + ":IP:" + this.client.getAddress().toString());
                long endTime = System.currentTimeMillis();
                this.log.info(this.PACKET_IDENTIFIER + "AUTH ACCEPT-ALL  Time to Final Respond:" + (endTime - this.lngStartTime));
                return accessResponse;
            }

            if (null != custRetrunData && null == custRetrunData.getPassword() && !custRetrunData.isAuthStatus() && !custRetrunData.isPasswordsaveflag() && this.checkConfigurationForPasswordSaveOnFirstLogin()) {
                custRetrunData = this.setPasswordForFirstAttempt(conn, custRetrunData, strPassword, strUsername);
            }

            if (custRetrunData != null && custRetrunData.getUsername() == null) {
                int custid = this.findCustMacMapping(conn, strUsername);
                if (custid != 0) {
                    this.log.debug(this.PACKET_IDENTIFIER + "MAC MAPPING FOUND: Cust is:" + custid);
                } else {
                    custid = this.findCustIPMapping(conn, strUsername);
                    if (custid != 0) {
                        this.log.debug(this.PACKET_IDENTIFIER + "IP MAPPING FOUND: Cust is:" + custid);
                    }
                }

                if (custid != 0) {
                    this.log.debug(this.PACKET_IDENTIFIER + "Cust ID is:" + custid);
                    customerData custDBData = this.getCustomerFromCustid(conn, custid);
                    if (custDBData != null && custDBData.getUsername() != null) {
                        custRetrunData = this.authenticateCustomer(conn, custDBData.getUsername(), custDBData.getPassword());
                        if (custRetrunData.getNetworktype() != null && custRetrunData.getNetworktype().length() > 0 && custRetrunData.getNetworktype().equalsIgnoreCase(AAAConstant.NETWORK_TYPE_IPOE)) {
                            custRetrunData.setCui(custDBData.getUsername());
                            this.request.removeAttributes(1);
                            this.request.addAttribute("User-Name", custDBData.getUsername());
                            this.PACKET_IDENTIFIER = this.PACKET_IDENTIFIER + "[" + custDBData.getUsername() + "]";
                        } else {
                            custRetrunData.setAuthStatus(false);
                            DBAuthenticationDriver dbAuth = new DBAuthenticationDriver();
                            if (!dbAuth.checkServerConf(AAAConstant.IS_DHCP_PPPOE_VALID_USER, AAAConstant.ATTRVALUEFORCHECK)) {
                                custRetrunData.setValidUserExists(false);
                            }

                            custRetrunData.setStrReplyMessage(AAAConstant.REPLYMSG_FAIL);
                        }
                    }
                }
            }

            this.log.info(this.PACKET_IDENTIFIER + "Authentication Status:" + custRetrunData.isAuthStatus());
            if (custRetrunData != null && custRetrunData.isAuthStatus()) {
                custRetrunData.setPACKET_IDENTIFIER(this.PACKET_IDENTIFIER);
                accessResponse.setPacketType(2);
                this.authorizeCustomer(conn, custRetrunData, this.request, accessResponse);
                if (custRetrunData.isAuthStatus()) {
                    this.log.info(this.PACKET_IDENTIFIER + "AUTHORIZATION STATUS:SUCCESS");
                    this.popuplateAccessResponseDetails(conn, custRetrunData);
                    this.log.info(this.PACKET_IDENTIFIER + "ALL DATABASE OPERATIONS ARE OVER");
                    long endTime = System.currentTimeMillis();
                    this.log.info(this.PACKET_IDENTIFIER + "Time to DB Operations:" + (endTime - this.lngStartTime));
                    this.log.info(this.PACKET_IDENTIFIER + "Final Customer Data is After Authorization :" + custRetrunData.toString());
                    if (custRetrunData.getIpaddress() != null) {
                        this.log.info(this.PACKET_IDENTIFIER + "[ALLOCATION]Static IP:" + custRetrunData.getIpaddress());
                        accessResponse.addAttribute("Framed-IP-Address", custRetrunData.getIpaddress());
                    }

                    if (custRetrunData.getMaxSessionTime() != null) {
                        this.log.info(this.PACKET_IDENTIFIER + "[ALLOCATION]Session-Timeout:" + custRetrunData.getMaxSessionTime());
                        accessResponse.addAttribute("Session-Timeout", String.valueOf(custRetrunData.getMaxSessionTime()));
                    }

                    this.processReplyItem(accessResponse, custRetrunData, this.client.getAddress().toString(), this.request);
                    custRetrunData.setStrReplyMessage(AAAConstant.REPLYMSG);
                    accessResponse.addAttribute("Reply-Message", custRetrunData.getStrReplyMessage());
                } else {
                    this.log.info(this.PACKET_IDENTIFIER + "AUTHORIZATION STATUS:FAILED");
                }
            }

            if (!custRetrunData.isAuthStatus() && !custRetrunData.isMaxConcurSessionBreached() && custRetrunData.isValidUserExists()) {
                Integer INACTIVEUSER_PROFILEID = null;

                try {
                    CachingConfigurations servercacheConfig = CachingConfigurations.getInstance();
                    Cache cacheServerAttribelocal = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
                    INACTIVEUSER_PROFILEID = Integer.parseInt(cacheServerAttribelocal.get(AAAConstant.INACTIVEUSER_PROFILEID).getObjectValue().toString());
                } catch (Exception var12) {
                    INACTIVEUSER_PROFILEID = null;
                }

                this.log.debug(this.PACKET_IDENTIFIER + "INACTIVEUSER_PROFILEID:" + INACTIVEUSER_PROFILEID);
                if (INACTIVEUSER_PROFILEID != null && INACTIVEUSER_PROFILEID > 0) {
                    accessResponse.setPacketType(2);
                    this.processInactiveUserReplyItem(accessResponse, custRetrunData, this.client.getAddress().toString(), this.request);
                }
            }

            hsAuthRes = new HashMap();
            hsAuthRes.put("username", this.request.getUserName());
            hsAuthRes.put("replymessage", custRetrunData.getStrReplyMessage() + (custRetrunData.getNetworktype() != null && custRetrunData.getNetworktype().equalsIgnoreCase(AAAConstant.NETWORK_TYPE_IPOE) ? "-IPOE" : ""));
            hsAuthRes.put("packettype", Integer.toString(accessResponse.getPacketType()));
            hsAuthRes.put(AAAConstant.CLIENTIP, this.client.getAddress().toString());
            RadiusAuthAuditPool.getInstance().getExecutor().execute(new AuthAuditProcessor(hsAuthRes, this.PACKET_IDENTIFIER));
            this.log.debug(this.PACKET_IDENTIFIER + "Final Data Dumping Username :" + this.request.getUserName() + ":Message:" + custRetrunData.getStrReplyMessage() + ":Type:" + accessResponse.getPacketType() + ":IP:" + this.client.getAddress().toString());
            }
        } catch (Exception var15) {
            Exception e = var15;
            accessResponse.addAttribute("Reply-Message", "Internal Error Authentication Fail");
            accessResponse.setPacketType(3);
            this.log.error(this.PACKET_IDENTIFIER + "ERROR:" + e, e);
        }

        long endTime = System.currentTimeMillis();
        this.log.info(this.PACKET_IDENTIFIER + "AUTH Time to Final Respond:" + (endTime - this.lngStartTime));
        return accessResponse;
    }

    private boolean checkConfigurationForPasswordSaveOnFirstLogin() {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthDriver.checkServerConfForPasswordSave();
    }

    private customerData setPasswordForFirstAttempt(Connection conn, customerData custData, String strPassword, String strUsername) {
        DBAuthenticationDriver dbAuthenticationDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthenticationDriver.setPasswordOnFirstLogin(conn, custData, strPassword, strUsername);
    }

    public customerData authenticateCustomer(Connection conn, String strUsername, String strPassword) {
        DBAuthenticationDriver dbAuthDrive = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        customerData custData = new customerData();
        custData.setUsername(strUsername);
        custData.setPassword(strPassword);
        customerData custRetrunData = dbAuthDrive.authenticateCustomer(conn, custData);
        return custRetrunData;
    }

    public void authorizeCustomer(Connection conn, customerData custRetrunData, AccessRequest request, RadiusPacket accessResponse) {
        this.log.debug(this.PACKET_IDENTIFIER + "authorizeCustomer () Called:");
        if (request.getAttribute(31) != null && custRetrunData.isMactelflag()) {
            boolean flag = this.validateMacBinding(conn, custRetrunData, request.getAttribute(31).getAttributeValue().toString());
            if (!flag) {
                custRetrunData.setAuthStatus(false);
                custRetrunData.setStrReplyMessage("Mac Validation Failed");
                accessResponse.setPacketType(3);
                this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:MAC Validation Failed");
                return;
            }
        }

        Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
        Integer VLAN_RADIUS_PACK_ATTR = null;
        String accessReqVlan = null;

        try {
            VLAN_RADIUS_PACK_ATTR = Integer.parseInt(cacheServerAttribe.get(AAAConstant.VLAN_RADIUS_PACK_ATTR).getObjectValue().toString());
        } catch (Exception var10) {
            this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]VLAN CHECK FAILED: ATTRIBUTE NOT SET: ");
            VLAN_RADIUS_PACK_ATTR = null;
        }

        try {
            if (VLAN_RADIUS_PACK_ATTR != null) {
                accessReqVlan = request.getAttribute(VLAN_RADIUS_PACK_ATTR).getAttributeValue().toString();
            }
        } catch (Exception var9) {
            this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]VLAN CHECK FAILED: ATTRIBUTE NOT FOUND IN REQUEST:" + VLAN_RADIUS_PACK_ATTR);
            accessReqVlan = null;
        }

        this.log.debug(this.PACKET_IDENTIFIER + "VLAN_RADIUS_PACK_ATTR:" + VLAN_RADIUS_PACK_ATTR, "accessReqVlan:" + accessReqVlan);
        if (request.getAttribute(VLAN_RADIUS_PACK_ATTR) != null && custRetrunData.isVlanEnable() && accessReqVlan != null) {
            boolean flag = false;
            flag = this.validateVlanBinding(conn, custRetrunData, accessReqVlan);
            if (!flag) {
                custRetrunData.setAuthStatus(false);
                custRetrunData.setStrReplyMessage("Vlan Validation Failed");
                accessResponse.setPacketType(3);
                this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Vlan Validation Failed");
                return;
            }
        }

        DBAuthenticationDriver dbAuthDrive = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        dbAuthDrive.populateAuthorizationDetails(conn, custRetrunData);
        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]basePlanExists:" + custRetrunData.isBasePlanExist());
        if (!custRetrunData.isBasePlanExist()) {
            custRetrunData.setAuthStatus(false);
            custRetrunData.setStrReplyMessage("Base Plan Doesn't Exist");
            accessResponse.setPacketType(3);
            this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Base Plan Doesn't Exist");
        } else {
            if (!this.isCustMacMapped(conn, custRetrunData.getCustid()) && request.getAttribute(31) != null) {
                this.mapCustMac(conn, custRetrunData.getCustid(), request.getAttribute(31).getAttributeValue().toString());
            }

            if (!this.isVlanMapped(conn, custRetrunData.getCustid()) && request.getAttribute(VLAN_RADIUS_PACK_ATTR) != null) {
                this.mapCustVlan(conn, custRetrunData.getCustid(), request.getAttribute(VLAN_RADIUS_PACK_ATTR).getAttributeValue().toString());
            }

            this.log.debug(this.PACKET_IDENTIFIER + "Customer Data is  :" + custRetrunData.toString() + ":Quota Type:" + custRetrunData.getQuotatype() + ": Data Quota :" + custRetrunData.getUnusedquota() + ": Time Quota :" + custRetrunData.getTimequotaunused());
            if (custRetrunData.getQuotatype() != null) {
                if (custRetrunData.getQuotatype().equalsIgnoreCase("Data")) {
                    if (Double.parseDouble(custRetrunData.getUnusedquota()) <= 0.0) {
                        custRetrunData.setAuthStatus(false);
                        custRetrunData.setStrReplyMessage("Quota Reached");
                        accessResponse.setPacketType(3);
                        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Quota Reached");
                        return;
                    }
                } else if (custRetrunData.getQuotatype().equalsIgnoreCase("Time")) {
                    if (Double.parseDouble(custRetrunData.getTimequotaunused()) <= 0.0) {
                        custRetrunData.setAuthStatus(false);
                        custRetrunData.setStrReplyMessage("Quota Reached");
                        accessResponse.setPacketType(3);
                        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Quota Reached");
                        return;
                    }
                } else {
                    if (!custRetrunData.getQuotatype().equalsIgnoreCase("Both")) {
                        custRetrunData.setAuthStatus(false);
                        custRetrunData.setStrReplyMessage("Invalid Plan");
                        accessResponse.setPacketType(3);
                        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Invalid Quota Type");
                        return;
                    }

                    if (Double.parseDouble(custRetrunData.getUnusedquota()) <= 0.0 || Double.parseDouble(custRetrunData.getTimequotaunused()) <= 0.0) {
                        custRetrunData.setAuthStatus(false);
                        custRetrunData.setStrReplyMessage("Quota Reached");
                        accessResponse.setPacketType(3);
                        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Quota Reached");
                        return;
                    }
                }
            }

            this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]Quota Available:" + custRetrunData.isAuthStatus());
            if (custRetrunData.getServiceareaid() != null) {
                dbAuthDrive.populateServiceAreaDetails(conn, custRetrunData);
                if (custRetrunData.getVLANID() != null && custRetrunData.getVLANID().length() > 0 && accessReqVlan != null && accessReqVlan.length() > 0) {
                    List<String> vlanIdList = Arrays.asList(custRetrunData.getVLANID().split(","));
                    Stream<String> var10001 = vlanIdList.stream();
                    accessReqVlan.getClass();
                    if (null == var10001.filter(accessReqVlan::equalsIgnoreCase).findAny().orElse(null)) {
                        custRetrunData.setAuthStatus(false);
                        custRetrunData.setStrReplyMessage("Service Area Vlan Mismatch");
                        accessResponse.setPacketType(3);
                        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:Service Area Vlan Mismatch");
                        return;
                    }
                }
            } else {
                this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]Customer Zone Check ByPassed:" + custRetrunData.getServiceareaid());
            }

        }
    }

    public void authorizeCustomer(customerData custRetrunData) {
        DBAuthenticationDriver dbAuthDrive = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        dbAuthDrive.populateAuthorizationDetails(custRetrunData);
    }

    public void popuplateAccessResponseDetails(Connection conn, customerData custRetrunData) {
        DBAuthenticationDriver dbAuthDrive = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        dbAuthDrive.populatePoolName(conn, custRetrunData);
        dbAuthDrive.populateStaticIP(conn, custRetrunData);
        dbAuthDrive.populateRadiusProfiles(conn, custRetrunData);
    }

    public void processReplyItem(RadiusPacket accessResponse, customerData custRetrunData, String strClientAddr, RadiusPacket accessRequest) {
        try {
            this.log.debug(this.PACKET_IDENTIFIER + "In processReplyItem, Access Request:" + accessRequest);
            this.log.debug(this.PACKET_IDENTIFIER + "In processReplyItem, custRetrunData:" + custRetrunData);
            int j = 0;
            if (custRetrunData.getRadiusprofile() != null && custRetrunData.getRadiusprofile().size() > 0) {
                this.log.debug(this.PACKET_IDENTIFIER + "Radius Profile Found:" + custRetrunData.getRadiusprofile().size());
                List<Integer> radProfile = custRetrunData.getRadiusprofile();
                Cache cacheGroups = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.GROUPPOLICY);

                label114:
                for(int i = 0; i < radProfile.size(); ++i) {
                    if (cacheGroups.get((Serializable)radProfile.get(i)) != null) {
                        ConcurrentHashMap profileCheck = (ConcurrentHashMap)cacheGroups.get((Serializable)radProfile.get(i)).getObjectValue();
                        this.log.debug(this.PACKET_IDENTIFIER + "For Profile Id :" + radProfile.get(i) + ":Data is " + profileCheck);
                        Iterator<Map.Entry> itr = profileCheck.entrySet().iterator();
                        RadiusAttribute ra = null;

                        while(true) {
                            Map.Entry entry;
                            ValidateExpression validate;
                            do {
                                if (!itr.hasNext()) {
                                    continue label114;
                                }

                                entry = itr.next();
                                validate = new ValidateExpression(this.PACKET_IDENTIFIER);
                            } while(!validate.checkExpression((String)entry.getKey(), accessRequest, accessResponse, custRetrunData));

                            this.log.debug(this.PACKET_IDENTIFIER + "Adding Data for :" + (String)entry.getKey() + ":Data is " + entry.getValue());
                            ConcurrentHashMap profileReply = (ConcurrentHashMap)entry.getValue();

                            for(Iterator<Map.Entry> itrReply = profileReply.entrySet().iterator(); itrReply.hasNext(); ++j) {
                                ra = null;
                                Map.Entry<Integer, ReplyItemData> entryReply = itrReply.next();
                                ReplyItemData replyItem = (ReplyItemData)entryReply.getValue();
                                if (replyItem.getAttributeValue().contains("{") && replyItem.getAttributeValue().contains("}")) {
                                    String dynaReplyValue = this.getDynamicReplyValue(replyItem.getAttributeValue(), custRetrunData);
                                    this.log.debug(this.PACKET_IDENTIFIER + "For dynaReplyValue :" + dynaReplyValue);
                                    if (dynaReplyValue == null || "null".equalsIgnoreCase(dynaReplyValue) || "".equalsIgnoreCase(dynaReplyValue)) {
                                        this.log.debug(this.PACKET_IDENTIFIER + "Checking with-in Request parameters");
                                        String attrName = this.getDynamicAttributeName(replyItem.getAttributeValue());
                                        RadiusAttribute rAttr = null;

                                        try {
                                            rAttr = accessRequest.getAttribute(attrName);
                                        } catch (Exception e) {
                                            this.log.info(this.PACKET_IDENTIFIER + "Attribute not found in access request:" + attrName + ":" + e, e);
                                            rAttr = null;
                                        }

                                        if (accessRequest != null && attrName != null && rAttr != null) {
                                            try {
                                                dynaReplyValue = accessRequest.getAttributeValue(attrName);
                                            } catch (Exception var22) {
                                                this.log.info(this.PACKET_IDENTIFIER + "Dynamic Attribute not found in request parameters:" + attrName);
                                            }
                                        }
                                    }

                                    if (dynaReplyValue != null && !"null".equalsIgnoreCase(dynaReplyValue) && !"".equalsIgnoreCase(dynaReplyValue)) {
                                        ra = this.getRadiusAttribute(replyItem.getAttribute(), dynaReplyValue);
                                        if (ra != null) {
                                            accessResponse.addAttribute(ra);
                                        }
                                    } else {
                                        this.log.info(this.PACKET_IDENTIFIER + "Skipping," + replyItem.getAttribute() + "as dynaReplyValue is null" + dynaReplyValue);
                                    }
                                } else {
                                    this.log.debug(this.PACKET_IDENTIFIER + "Static Attribute");
                                    ra = this.getRadiusAttribute(replyItem.getAttribute(), replyItem.getAttributeValue());
                                    if (ra != null) {
                                        accessResponse.addAttribute(ra);
                                    } else {
                                        this.log.debug(this.PACKET_IDENTIFIER + "Static Skipping," + replyItem.getAttribute() + "as radiusAttribute is null");
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                this.log.debug(this.PACKET_IDENTIFIER + "Radius Profile NOT Attached:");
            }

            TreeMap<String, String> classAttributeParamMap = new TreeMap();
            if (custRetrunData.getCustPlanRelId() != null) {
                classAttributeParamMap.put("custpackid", String.valueOf(custRetrunData.getCustPlanRelId()));
            }

            classAttributeParamMap.put("qu", custRetrunData.isQuotaUnlimited() ? "1" : "0");
            classAttributeParamMap.put("sd", custRetrunData.isSpeedDowngradeFUP() ? "1" : "0");
            if (classAttributeParamMap.size() > 0) {
                accessResponse.addAttribute("Class", RSAUtility.encrypt(RSAUtility.KEY, classAttributeParamMap));
            }

            this.log.debug(this.PACKET_IDENTIFIER + "Final Response:" + accessResponse);
        } catch (Exception var24) {
            Exception e = var24;
            this.log.error(this.PACKET_IDENTIFIER + "Error in processReplyItem:" + e.getMessage(), e);
        }

    }

    public void processInactiveUserReplyItem(RadiusPacket accessResponse, customerData custRetrunData, String strClientAddr, RadiusPacket accessRequest) {
        try {
            CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
            Integer INACTIVEUSER_PROFILEID = null;

            try {
                INACTIVEUSER_PROFILEID = Integer.parseInt(cacheServerAttribe.get(AAAConstant.INACTIVEUSER_PROFILEID).getObjectValue().toString());
            } catch (Exception var24) {
                INACTIVEUSER_PROFILEID = null;
            }

            this.log.debug(this.PACKET_IDENTIFIER + "INACTIVEUSER_PROFILEID:" + INACTIVEUSER_PROFILEID);
            if (INACTIVEUSER_PROFILEID != null) {
                Cache cacheGroups = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.GROUPPOLICY);
                if (cacheGroups.get(INACTIVEUSER_PROFILEID) != null) {
                    ConcurrentHashMap profileCheck = (ConcurrentHashMap)cacheGroups.get(INACTIVEUSER_PROFILEID).getObjectValue();
                    this.log.debug(this.PACKET_IDENTIFIER + "For Profile Id :" + INACTIVEUSER_PROFILEID + ":Data is " + profileCheck);
                    Iterator<Map.Entry> itr = profileCheck.entrySet().iterator();
                    RadiusAttribute ra = null;

                    label100:
                    while(true) {
                        Map.Entry entry;
                        ValidateExpression validate;
                        do {
                            if (!itr.hasNext()) {
                                break label100;
                            }

                            entry = itr.next();
                            validate = new ValidateExpression(this.PACKET_IDENTIFIER);
                        } while(!validate.checkExpression((String)entry.getKey(), accessRequest));

                        this.log.debug(this.PACKET_IDENTIFIER + "Adding Data for :" + (String)entry.getKey() + ":Data is " + entry.getValue());
                        ConcurrentHashMap profileReply = (ConcurrentHashMap)entry.getValue();
                        Iterator<Map.Entry> itrReply = profileReply.entrySet().iterator();

                        while(true) {
                            while(true) {
                                while(true) {
                                    if (!itrReply.hasNext()) {
                                        continue label100;
                                    }

                                    ra = null;
                                    Map.Entry<Integer, ReplyItemData> entryReply = itrReply.next();
                                    ReplyItemData replyItem = (ReplyItemData)entryReply.getValue();
                                    if (replyItem.getAttributeValue().contains("{") && replyItem.getAttributeValue().contains("}")) {
                                        String dynaReplyValue = this.getDynamicReplyValue(replyItem.getAttributeValue(), custRetrunData);
                                        this.log.debug(this.PACKET_IDENTIFIER + "For dynaReplyValue :" + dynaReplyValue);
                                        if (dynaReplyValue == null || "null".equalsIgnoreCase(dynaReplyValue) || "".equalsIgnoreCase(dynaReplyValue)) {
                                            String attrName = this.getDynamicAttributeName(replyItem.getAttributeValue());
                                            RadiusAttribute rAttr = null;

                                            try {
                                                rAttr = accessRequest.getAttribute(attrName);
                                            } catch (Exception var23) {
                                                Exception e = var23;
                                                this.log.info(this.PACKET_IDENTIFIER + "Attribute not found in access request:" + attrName + ":" + e, e);
                                                rAttr = null;
                                            }

                                            if (accessRequest != null && attrName != null && rAttr != null) {
                                                try {
                                                    dynaReplyValue = accessRequest.getAttributeValue(attrName);
                                                } catch (Exception var22) {
                                                    this.log.info(this.PACKET_IDENTIFIER + "Dynamic Attribute not found in request parameters:" + attrName);
                                                }
                                            }
                                        }

                                        if (dynaReplyValue != null && !"null".equalsIgnoreCase(dynaReplyValue) && !"".equalsIgnoreCase(dynaReplyValue)) {
                                            ra = this.getRadiusAttribute(replyItem.getAttribute(), dynaReplyValue);
                                            if (ra != null) {
                                                accessResponse.addAttribute(ra);
                                            }
                                        } else {
                                            this.log.info(this.PACKET_IDENTIFIER + "Skipping," + replyItem.getAttribute() + "as dynaReplyValue is null" + dynaReplyValue);
                                        }
                                    } else {
                                        ra = this.getRadiusAttribute(replyItem.getAttribute(), replyItem.getAttributeValue());
                                        if (ra != null) {
                                            accessResponse.addAttribute(ra);
                                        } else {
                                            this.log.debug(this.PACKET_IDENTIFIER + "Static Skipping," + replyItem.getAttribute() + "as radiusAttribute is null");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TreeMap<String, String> classAttributeParamMap = new TreeMap();
                classAttributeParamMap.put("custpackid", "-2");
                if (classAttributeParamMap.size() > 0) {
                    accessResponse.addAttribute("Class", RSAUtility.encrypt("NeoFruition@2021", classAttributeParamMap));
                }

                accessResponse.setPacketType(2);
            } else {
                this.log.debug(this.PACKET_IDENTIFIER + "INACTIVEUSER_PROFILEID Radius Profile NOT Attached:");
            }
        } catch (Exception var25) {
            Exception e = var25;
            this.log.error(this.PACKET_IDENTIFIER + " Error in processInactiveUserReplyItem:" + e.getMessage(), e);
        }

    }

    public String removeFirstandLast(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.deleteCharAt(str.length() - 1);
        sb.deleteCharAt(0);
        return sb.toString();
    }

    public String getDynamicReplyValue(String attributeStr, customerData custReturnData) {
        List<String> strList = Arrays.asList(attributeStr.split("\""));
        StringBuilder resultStr = new StringBuilder();

        try {
            Iterator var13 = strList.iterator();

            while(true) {
                String str;
                do {
                    if (!var13.hasNext()) {
                        return resultStr.toString();
                    }

                    str = (String)var13.next();
                } while(str.length() == 0);

                String dyanaAttrName = null;

                String dynamicVar;
                for(String dynaReplyValue = null; str.contains("{") && str.contains("}"); str = str.replace(dynamicVar, dynaReplyValue)) {
                    dyanaAttrName = null;
                    dynaReplyValue = null;
                    dyanaAttrName = this.getDynamicAttributeName(str);
                    dynaReplyValue = this.getDynamicValue(custReturnData, dyanaAttrName);
                    this.log.debug(this.PACKET_IDENTIFIER + "Dynamic value For:" + dyanaAttrName + ":is " + dynaReplyValue);
                    dynamicVar = "{" + dyanaAttrName + "}";
                    if (dyanaAttrName == null || dynaReplyValue == null) {
                        str = null;
                        break;
                    }
                }

                try {
                    if (str != null && (str.contains("(") || str.contains(")") || str.contains("/") || str.contains("*") || str.contains("+") || str.contains("-"))) {
                        ScriptEngineManager manager = new ScriptEngineManager();
                        ScriptEngine engine = manager.getEngineByName("nashorn");
                        resultStr.append(engine.eval(str));
                    } else if (str != null) {
                        resultStr.append(str);
                    }
                } catch (ScriptException var11) {
                    this.log.info(this.PACKET_IDENTIFIER + "Dynamic value Evaluation Failed for :" + str);
                    resultStr.append(str);
                }
            }
        } catch (Exception var12) {
            Exception e = var12;
            this.log.error(this.PACKET_IDENTIFIER + "Error in getDynamicReplyValue:" + e, e);
            return resultStr.toString();
        }
    }

    public String getDynamicAttributeName(String str) {
        int openIdx = str.indexOf("{");
        int closeIdx = str.indexOf("}");
        String attrStr = (String)str.subSequence(openIdx + 1, closeIdx);
        return attrStr;
    }

    public String replaceDynamicValue(String str, String attr) {
        StringBuilder sb = new StringBuilder();
        int openIdx = str.indexOf("{");
        int closeIdx = str.indexOf("}");
        String subStr1 = (String)str.subSequence(0, openIdx);
        String subStr2 = (String)str.subSequence(closeIdx + 1, str.length());
        sb.append(subStr1).append(attr).append(subStr2);
        return sb.toString();
    }

    public String getDynamicValue(customerData custRetrunData, String attriValue) {
        if (attriValue.equalsIgnoreCase("ASNNumber")) {
            return custRetrunData.getASNNumber();
        } else if (attriValue.equalsIgnoreCase("BNGRouterInterface")) {
            return custRetrunData.getBNGRouterInterface();
        } else if (attriValue.equalsIgnoreCase("BNGRouterName")) {
            return custRetrunData.getBNGRouterName();
        } else if (attriValue.equalsIgnoreCase("cui")) {
            return custRetrunData.getCui();
        } else if (attriValue.equalsIgnoreCase("IPPrefixes")) {
            return custRetrunData.getIPPrefixes();
        } else if (attriValue.equalsIgnoreCase("IPV6Prefixes")) {
            return custRetrunData.getIPV6Prefixes();
        } else if (attriValue.equalsIgnoreCase("LANIP")) {
            return custRetrunData.getLANIP();
        } else if (attriValue.equalsIgnoreCase("LANIPV6")) {
            return custRetrunData.getLANIPV6();
        } else if (attriValue.equalsIgnoreCase("LLAccountID")) {
            return custRetrunData.getLLAccountID();
        } else if (attriValue.equalsIgnoreCase("LLConnectionType")) {
            return custRetrunData.getLLConnectionType();
        } else if (attriValue.equalsIgnoreCase("LLExpiryDate")) {
            return custRetrunData.getLLExpiryDate();
        } else if (attriValue.equalsIgnoreCase("LLMedium")) {
            return custRetrunData.getLLMedium();
        } else if (attriValue.equalsIgnoreCase("LLServiceID")) {
            return custRetrunData.getLLServiceID();
        } else if (attriValue.equalsIgnoreCase("MACADDRESS")) {
            return custRetrunData.getMACADDRESS();
        } else if (attriValue.equalsIgnoreCase("PeerIP")) {
            return custRetrunData.getPeerIP();
        } else if (attriValue.equalsIgnoreCase("POOLIP")) {
            return custRetrunData.getPOOLIP();
        } else if (attriValue.equalsIgnoreCase("QOS")) {
            return custRetrunData.getQOS();
        } else if (attriValue.equalsIgnoreCase("RDExport")) {
            return custRetrunData.getRDExport();
        } else if (attriValue.equalsIgnoreCase("RDValue")) {
            return custRetrunData.getRDValue();
        } else if (attriValue.equalsIgnoreCase("VLANID")) {
            return custRetrunData.getVLANID();
        } else if (attriValue.equalsIgnoreCase("VRFName")) {
            return custRetrunData.getVRFName();
        } else if (attriValue.equalsIgnoreCase("VSIID")) {
            return custRetrunData.getVSIID();
        } else if (attriValue.equalsIgnoreCase("VSIName")) {
            return custRetrunData.getVSIName();
        } else if (attriValue.equalsIgnoreCase("WANIP")) {
            return custRetrunData.getWANIP();
        } else if (attriValue.equalsIgnoreCase("WANIPV6")) {
            return custRetrunData.getWANIPV6();
        } else if (attriValue.equalsIgnoreCase("quotatype")) {
            return custRetrunData.getQuotatype();
        } else if (attriValue.equalsIgnoreCase("quotaunit")) {
            return custRetrunData.getQuotaunit();
        } else if (attriValue.equalsIgnoreCase("totalquota")) {
            return custRetrunData.getTotalquota();
        } else if (attriValue.equalsIgnoreCase("usedquota")) {
            return custRetrunData.getUsedquota();
        } else if (attriValue.equalsIgnoreCase("totalquotakb")) {
            return custRetrunData.getTotalquotakb();
        } else if (attriValue.equalsIgnoreCase("usedquotakb")) {
            return custRetrunData.getUsedquotakb();
        } else if (attriValue.equalsIgnoreCase("timequotaunit")) {
            return custRetrunData.getTimequotaunit();
        } else if (attriValue.equalsIgnoreCase("timetotalquota")) {
            return custRetrunData.getTimetotalquota();
        } else if (attriValue.equalsIgnoreCase("timequotaused")) {
            return custRetrunData.getTimequotaused();
        } else if (attriValue.equalsIgnoreCase("timetotalquotasec")) {
            return custRetrunData.getTimetotalquotasec();
        } else if (attriValue.equalsIgnoreCase("timeusedquotasec")) {
            return custRetrunData.getTimeusedquotasec();
        } else if (attriValue.equalsIgnoreCase("unusedquota")) {
            return custRetrunData.getUnusedquota();
        } else if (attriValue.equalsIgnoreCase("unusedquotakb")) {
            return custRetrunData.getUnusedquotakb();
        } else if (attriValue.equalsIgnoreCase("timequotaunused")) {
            return custRetrunData.getTimequotaunused();
        } else if (attriValue.equalsIgnoreCase("timeunusedquotasec")) {
            return custRetrunData.getTimeunusedquotasec();
        } else if (attriValue.equalsIgnoreCase("poolname")) {
            return custRetrunData.getPoolname();
        } else {
            if (custRetrunData.isDynamicAttrSupport()) {
                if (null != custRetrunData.getQospolicyparam() && custRetrunData.getQospolicyparam().size() > 0 && custRetrunData.getQospolicyparam().containsKey(attriValue)) {
                    return (String)custRetrunData.getQospolicyparam().get(attriValue);
                }
            } else {
                if (attriValue.equalsIgnoreCase("policyname")) {
                    return custRetrunData.getPolicyname();
                }

                if (attriValue.equalsIgnoreCase("uploadspeed")) {
                    return custRetrunData.getUploadspeed();
                }

                if (attriValue.equalsIgnoreCase("downloadspeed")) {
                    return custRetrunData.getDownloadspeed();
                }

                if (attriValue.equalsIgnoreCase("param1")) {
                    return custRetrunData.getParam1();
                }

                if (attriValue.equalsIgnoreCase("param2")) {
                    return custRetrunData.getParam2();
                }

                if (attriValue.equalsIgnoreCase("param3")) {
                    return custRetrunData.getParam3();
                }
            }

            if (attriValue.startsWith("$fn") && attriValue.contains("(") && attriValue.contains(")")) {
                try {
                    String strParamIdentifier = attriValue.substring(attriValue.indexOf("(") + 1, attriValue.indexOf(")"));
                    String paramCollectionType = strParamIdentifier.substring(0, strParamIdentifier.indexOf("["));
                    String parameter = strParamIdentifier.substring(strParamIdentifier.indexOf("['") + 2, strParamIdentifier.indexOf("']"));
                    String funcAttributeValue = null;
                    String returnValue = null;
                    boolean isServerConfig = false;
                    boolean isCustomerAttrib = false;
                    if (paramCollectionType != null && !"".equalsIgnoreCase(paramCollectionType) && parameter != null && !"".equalsIgnoreCase(parameter)) {
                        try {
                            if ("$SERVER_ATTR".equalsIgnoreCase(paramCollectionType)) {
                                CachingConfigurations serverattrConfig = CachingConfigurations.getInstance();
                                Cache cacheServerAttribelocal = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
                                funcAttributeValue = cacheServerAttribelocal.get(parameter).getObjectValue().toString();
                                isServerConfig = true;
                            } else if ("$LOCAL".equalsIgnoreCase(paramCollectionType)) {
                                funcAttributeValue = parameter;
                            }
                        } catch (Exception var16) {
                            funcAttributeValue = null;
                        }
                    }

                    if (funcAttributeValue != null && attriValue.startsWith("$fn_any")) {
                        String currentCustValue = null;
                        CustomerAttribute currCustAttrib = null;
                        CachingConfigurations splitArray;
                        if (isServerConfig) {
                            splitArray = CachingConfigurations.getInstance();
                            Cache cacheCustServerAttribes = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCUSTATTRIBUTE);
                            if (cacheCustServerAttribes != null && cacheCustServerAttribes.get(parameter) != null) {
                                ServerConfData configData = (ServerConfData)cacheCustServerAttribes.get(parameter).getObjectValue();
                                isCustomerAttrib = true;
                                DBAuthenticationDriver driver = new DBAuthenticationDriver();
                                currCustAttrib = driver.getCustomerAttribute(custRetrunData.getCustid(), parameter);
                                if (currCustAttrib != null && "Y".equalsIgnoreCase(configData.getEnsureUnique())) {
                                    currentCustValue = currCustAttrib.getAttributeValue();
                                }
                            }
                        }

                        splitArray = null;
                        this.log.info("{} In getDynamicValue(): $fn, paramCollectionType:{},Collection:{},Attribute:{},AttributeValue:{}", new Object[]{this.PACKET_IDENTIFIER, strParamIdentifier, paramCollectionType, parameter, funcAttributeValue});
                        String[] splitArray1 = funcAttributeValue.split(",");
                        returnValue = this.getUniqueNonRepeatingValue(splitArray1, currentCustValue);
                        if (isCustomerAttrib) {
                            if (currCustAttrib == null) {
                                currCustAttrib = new CustomerAttribute();
                                currCustAttrib.setCustid(custRetrunData.getCustid());
                                currCustAttrib.setAttributeName(parameter);
                            }

                            currCustAttrib.setAttributeValue(returnValue);
                            CustAttribWorker worker = new CustAttribWorker(this.PACKET_IDENTIFIER, currCustAttrib);
                            CommonThreadPool.getInstance().getExecutor().execute(worker);
                        }
                    }

                    return returnValue;
                } catch (Exception var17) {
                    Exception e = var17;
                    this.log.error("error in getDynamic Value for Function:{}", e.getMessage(), e);
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public boolean isCustMacMapped(Connection conn, int custid) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        boolean result = dbAuthDriver.findCustMacMapping(conn, custid);
        return result;
    }

    public void mapCustMac(Connection conn, int custid, String macAddress) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        dbAuthDriver.setCustMacAddress(conn, custid, macAddress);
    }

    public int findCustMacMapping(Connection conn, String macAddress) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        int custid = dbAuthDriver.findMacCustMapping(conn, macAddress);
        return custid;
    }

    private customerData getCustomerFromCustid(Connection conn, int custid) {
        DBAuthenticationDriver dbAuthDrive = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthDrive.getCustomerEntryFromCustid(conn, custid);
    }

    private boolean validateMacBinding(Connection conn, customerData custReturnData, String macAddress) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthDriver.validateMacBinding(conn, custReturnData, macAddress);
    }

    private boolean validateVlanBinding(Connection conn, customerData custReturnData, String vlanId) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthDriver.validateVlanBinding(conn, custReturnData, vlanId);
    }

    public boolean isVlanMapped(Connection conn, int custid) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthDriver.findVlanMapping(conn, custid);
    }

    public void mapCustVlan(Connection conn, int custid, String vlanId) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        dbAuthDriver.setCustVlan(conn, custid, vlanId);
    }

    public RadiusAttribute getRadiusAttribute(String attributeName, String attributeValue) {
        RadiusAttribute ra = null;

        try {
            Dictionary dictionary = DefaultDictionary.getDefaultDictionary();
            AttributeType attrbType = dictionary.getAttributeTypeByName(attributeName);
            this.log.debug(this.PACKET_IDENTIFIER + "IN getAttributeValue for attributeName:" + attributeName + ",attributeValue:" + attributeValue + ",attrbType:" + attrbType);
            if (attributeValue != null) {
                ra = RadiusAttribute.createRadiusAttribute(dictionary, attrbType.getVendorId(), attrbType.getTypeCode());
                if (attrbType.getAttributeClass().toString().equals("class org.tinyradius.attribute.IntegerAttribute")) {
                    ((IntegerAttribute)ra).setAttributeValue(Integer.parseInt(attributeValue));
                } else if (attrbType.getAttributeClass().toString().equals("class org.tinyradius.attribute.LongAttribute")) {
                    ((LongAttribute)ra).setAttributeValue(Long.parseLong(attributeValue));
                } else if (attrbType.getAttributeClass().toString().equals("class org.tinyradius.attribute.RadiusAttribute")) {
                    ra.setAttributeData(attributeValue.getBytes());
                } else if (attrbType.getAttributeClass().toString().equals("class org.tinyradius.attribute.IpAttribute")) {
                    ((IpAttribute)ra).setAttributeValue(attributeValue);
                } else if (attrbType.getAttributeClass().toString().equals("class org.tinyradius.attribute.Ipv6PrefixAttribute")) {
                    ((Ipv6PrefixAttribute)ra).setAttributeValue(attributeValue);
                } else {
                    ra.setAttributeValue(attributeValue);
                }
            }
        } catch (Exception var6) {
            Exception e = var6;
            ra = null;
            this.log.error(this.PACKET_IDENTIFIER + "Error in getAttributeValue for attributeName:" + attributeName + ",attributeValue:" + attributeValue + "," + e, e);
        }

        return ra;
    }

    public int findCustIPMapping(Connection conn, String ipAddress) {
        DBAuthenticationDriver dbAuthDriver = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        return dbAuthDriver.findCustIPMapping(conn, ipAddress);
    }

    public String getUniqueNonRepeatingValue(String[] valueList, String currentVal) {
        String returnValue = null;

        try {
            if (currentVal == null) {
                currentVal = String.valueOf(Math.random());
            }

            if (valueList != null) {
                int retryCount = 0;

                do {
                    if (valueList.length > 1) {
                        int rand = (new Random()).nextInt(valueList.length - 0);
                        returnValue = valueList[rand];
                    } else {
                        returnValue = valueList[0];
                    }

                    ++retryCount;
                } while(currentVal.equalsIgnoreCase(returnValue) && retryCount <= valueList.length * 3);

                if (retryCount > valueList.length * 3) {
                    this.log.info("{} In getUniqueNonRepatingValue Unique value not found after {} retry", this.PACKET_IDENTIFIER, valueList.length * 3);
                    returnValue = null;
                }
            }
        } catch (Exception var6) {
            Exception e = var6;
            this.log.error("{} Error in getUniqueNonRepatingValue {} {}", new Object[]{this.PACKET_IDENTIFIER, e.getMessage(), e});
        }

        return returnValue;
    }
}
