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

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
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
    private static final ThreadLocal<ScriptEngine> SCRIPT_ENGINE = ThreadLocal.withInitial(() -> new ScriptEngineManager().getEngineByName("nashorn"));
    private static final Map<String, Function<customerData, String>> DYNAMIC_VALUE_GETTERS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final Map<String, Function<customerData, String>> NON_DYNAMIC_QOS_GETTERS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        DYNAMIC_VALUE_GETTERS.put("ASNNumber", customerData::getASNNumber);
        DYNAMIC_VALUE_GETTERS.put("BNGRouterInterface", customerData::getBNGRouterInterface);
        DYNAMIC_VALUE_GETTERS.put("BNGRouterName", customerData::getBNGRouterName);
        DYNAMIC_VALUE_GETTERS.put("cui", customerData::getCui);
        DYNAMIC_VALUE_GETTERS.put("IPPrefixes", customerData::getIPPrefixes);
        DYNAMIC_VALUE_GETTERS.put("IPV6Prefixes", customerData::getIPV6Prefixes);
        DYNAMIC_VALUE_GETTERS.put("LANIP", customerData::getLANIP);
        DYNAMIC_VALUE_GETTERS.put("LANIPV6", customerData::getLANIPV6);
        DYNAMIC_VALUE_GETTERS.put("LLAccountID", customerData::getLLAccountID);
        DYNAMIC_VALUE_GETTERS.put("LLConnectionType", customerData::getLLConnectionType);
        DYNAMIC_VALUE_GETTERS.put("LLExpiryDate", customerData::getLLExpiryDate);
        DYNAMIC_VALUE_GETTERS.put("LLMedium", customerData::getLLMedium);
        DYNAMIC_VALUE_GETTERS.put("LLServiceID", customerData::getLLServiceID);
        DYNAMIC_VALUE_GETTERS.put("MACADDRESS", customerData::getMACADDRESS);
        DYNAMIC_VALUE_GETTERS.put("PeerIP", customerData::getPeerIP);
        DYNAMIC_VALUE_GETTERS.put("POOLIP", customerData::getPOOLIP);
        DYNAMIC_VALUE_GETTERS.put("QOS", customerData::getQOS);
        DYNAMIC_VALUE_GETTERS.put("RDExport", customerData::getRDExport);
        DYNAMIC_VALUE_GETTERS.put("RDValue", customerData::getRDValue);
        DYNAMIC_VALUE_GETTERS.put("VLANID", customerData::getVLANID);
        DYNAMIC_VALUE_GETTERS.put("VRFName", customerData::getVRFName);
        DYNAMIC_VALUE_GETTERS.put("VSIID", customerData::getVSIID);
        DYNAMIC_VALUE_GETTERS.put("VSIName", customerData::getVSIName);
        DYNAMIC_VALUE_GETTERS.put("WANIP", customerData::getWANIP);
        DYNAMIC_VALUE_GETTERS.put("WANIPV6", customerData::getWANIPV6);
        DYNAMIC_VALUE_GETTERS.put("quotatype", customerData::getQuotatype);
        DYNAMIC_VALUE_GETTERS.put("quotaunit", customerData::getQuotaunit);
        DYNAMIC_VALUE_GETTERS.put("totalquota", customerData::getTotalquota);
        DYNAMIC_VALUE_GETTERS.put("usedquota", customerData::getUsedquota);
        DYNAMIC_VALUE_GETTERS.put("totalquotakb", customerData::getTotalquotakb);
        DYNAMIC_VALUE_GETTERS.put("usedquotakb", customerData::getUsedquotakb);
        DYNAMIC_VALUE_GETTERS.put("timequotaunit", customerData::getTimequotaunit);
        DYNAMIC_VALUE_GETTERS.put("timetotalquota", customerData::getTimetotalquota);
        DYNAMIC_VALUE_GETTERS.put("timequotaused", customerData::getTimequotaused);
        DYNAMIC_VALUE_GETTERS.put("timetotalquotasec", customerData::getTimetotalquotasec);
        DYNAMIC_VALUE_GETTERS.put("timeusedquotasec", customerData::getTimeusedquotasec);
        DYNAMIC_VALUE_GETTERS.put("unusedquota", customerData::getUnusedquota);
        DYNAMIC_VALUE_GETTERS.put("unusedquotakb", customerData::getUnusedquotakb);
        DYNAMIC_VALUE_GETTERS.put("timequotaunused", customerData::getTimequotaunused);
        DYNAMIC_VALUE_GETTERS.put("timeunusedquotasec", customerData::getTimeunusedquotasec);
        DYNAMIC_VALUE_GETTERS.put("poolname", customerData::getPoolname);

        NON_DYNAMIC_QOS_GETTERS.put("policyname", customerData::getPolicyname);
        NON_DYNAMIC_QOS_GETTERS.put("uploadspeed", customerData::getUploadspeed);
        NON_DYNAMIC_QOS_GETTERS.put("downloadspeed", customerData::getDownloadspeed);
        NON_DYNAMIC_QOS_GETTERS.put("param1", customerData::getParam1);
        NON_DYNAMIC_QOS_GETTERS.put("param2", customerData::getParam2);
        NON_DYNAMIC_QOS_GETTERS.put("param3", customerData::getParam3);
    }

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
            lngPacketId = (long) request.getPacketIdentifier();
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
            } catch (Exception e) {
                acceptAll = false;
            }

            RadiusServerImpl impl = new RadiusServerImpl();

            try {
                clientData cltData = impl.identifyClient(this.client.getAddress().toString());
                this.request.removeAttributes(77);
                this.request.addAttribute("Connect-Info", cltData.getGroupname());
            } catch (Exception e) {
                this.log.error(this.PACKET_IDENTIFIER + "Error in processAccessRquest() : " + e, e);
            }

            this.log.debug("{}Access Request Packet is : {}", this.PACKET_IDENTIFIER, this.request);
            String strUsername = this.request.getAttribute("User-Name").getAttributeValue();
            String strPassword = this.request.getUserPassword();
            strUsername = strUsername != null ? strUsername.trim() : null;
            try (Connection conn = DataSource.getConnection()) {
                customerData custRetrunData = this.authenticateCustomer(conn, strUsername, strPassword);
                this.log.debug("{}Customer Data is  :{}", this.PACKET_IDENTIFIER, custRetrunData);
                if (custRetrunData.isAuthStatus() && acceptAll) {
                    custRetrunData.setPACKET_IDENTIFIER(this.PACKET_IDENTIFIER);
                    accessResponse.setPacketType(2);
                    this.processReplyItem(accessResponse, custRetrunData, this.client.getAddress().toString(), this.request);
                    custRetrunData.setStrReplyMessage("Welcome-All");
                    accessResponse.addAttribute("Reply-Message", custRetrunData.getStrReplyMessage());
                    this.submitAuthAudit(AAAConstant.REPLYMSG, accessResponse);
                    this.log.debug(this.PACKET_IDENTIFIER + "Final Data Dumping Username :" + this.request.getUserName() + ":Message:" + AAAConstant.REPLYMSG + "-All:Type:" + accessResponse.getPacketType() + ":IP:" + this.client.getAddress().toString());
                    long endTime = System.currentTimeMillis();
                    this.log.info(this.PACKET_IDENTIFIER + "AUTH ACCEPT-ALL  Time to Final Respond:" + (endTime - this.lngStartTime));
                    return accessResponse;
                }

                if (null == custRetrunData.getPassword() && !custRetrunData.isAuthStatus() && !custRetrunData.isPasswordsaveflag() && this.checkConfigurationForPasswordSaveOnFirstLogin()) {
                    custRetrunData = this.setPasswordForFirstAttempt(conn, custRetrunData, strPassword, strUsername);
                }

                if (custRetrunData.getUsername() == null) {
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
                if (custRetrunData.isAuthStatus()) {
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
                    Integer inactiveUserProfileId;

                    try {
                        CachingConfigurations.getInstance();
                        inactiveUserProfileId = parseInactiveUserProfileId(CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE));
                    } catch (Exception e) {
                        inactiveUserProfileId = null;
                    }

                    this.log.debug("{}INACTIVEUSER_PROFILEID:{}", this.PACKET_IDENTIFIER, inactiveUserProfileId);
                    if (inactiveUserProfileId != null && inactiveUserProfileId > 0) {
                        accessResponse.setPacketType(2);
                        this.processInactiveUserReplyItem(accessResponse, custRetrunData, this.client.getAddress().toString(), this.request);
                    }
                }

                boolean ipoe = custRetrunData.getNetworktype() != null && custRetrunData.getNetworktype().equalsIgnoreCase(AAAConstant.NETWORK_TYPE_IPOE);
                this.submitAuthAudit(custRetrunData.getStrReplyMessage() + (ipoe ? "-IPOE" : ""), accessResponse);
                this.log.debug(this.PACKET_IDENTIFIER + "Final Data Dumping Username :" + this.request.getUserName() + ":Message:" + custRetrunData.getStrReplyMessage() + ":Type:" + accessResponse.getPacketType() + ":IP:" + this.client.getAddress().toString());
            }
        } catch (Exception e) {
            accessResponse.addAttribute("Reply-Message", "Internal Error Authentication Fail");
            accessResponse.setPacketType(3);
            this.log.error(this.PACKET_IDENTIFIER + "ERROR:" + e, e);
        }

        long endTime = System.currentTimeMillis();
        this.log.info(this.PACKET_IDENTIFIER + "AUTH Time to Final Respond:" + (endTime - this.lngStartTime));
        return accessResponse;
    }

    /** Queues the auth audit row (username, reply message, packet type, client ip) on the async audit pool. */
    private void submitAuthAudit(String replyMessage, RadiusPacket accessResponse) {
        HashMap<String, String> hsAuthRes = new HashMap<>();
        hsAuthRes.put("username", this.request.getUserName());
        hsAuthRes.put("replymessage", replyMessage);
        hsAuthRes.put("packettype", Integer.toString(accessResponse.getPacketType()));
        hsAuthRes.put(AAAConstant.CLIENTIP, this.client.getAddress().toString());
        RadiusAuthAuditPool.getInstance().getExecutor().execute(new AuthAuditProcessor(hsAuthRes, this.PACKET_IDENTIFIER));
    }

    /** Marks authorization as failed: status false, reply message set, packet type Access-Reject. */
    private void rejectAuthorization(customerData custRetrunData, RadiusPacket accessResponse, String replyMessage, String logReason) {
        custRetrunData.setAuthStatus(false);
        custRetrunData.setStrReplyMessage(replyMessage);
        accessResponse.setPacketType(3);
        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]AUTHORIZATION STATUS:FAILED, REASON:" + logReason);
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
                this.rejectAuthorization(custRetrunData, accessResponse, "Mac Validation Failed", "MAC Validation Failed");
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

        this.log.debug("{}VLAN_RADIUS_PACK_ATTR:{},accessReqVlan:{}", this.PACKET_IDENTIFIER, VLAN_RADIUS_PACK_ATTR, accessReqVlan);
        if (VLAN_RADIUS_PACK_ATTR != null && request.getAttribute(VLAN_RADIUS_PACK_ATTR) != null && custRetrunData.isVlanEnable() && accessReqVlan != null) {
            boolean flag = false;
            flag = this.validateVlanBinding(conn, custRetrunData, accessReqVlan);
            if (!flag) {
                this.rejectAuthorization(custRetrunData, accessResponse, "Vlan Validation Failed", "Vlan Validation Failed");
                return;
            }
        }

        DBAuthenticationDriver dbAuthDrive = new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
        dbAuthDrive.populateAuthorizationDetails(conn, custRetrunData);
        this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]basePlanExists:" + custRetrunData.isBasePlanExist());
        if (!custRetrunData.isBasePlanExist()) {
            this.rejectAuthorization(custRetrunData, accessResponse, "Base Plan Doesn't Exist", "Base Plan Doesn't Exist");
        } else {
            if (!this.isCustMacMapped(conn, custRetrunData.getCustid()) && request.getAttribute(31) != null) {
                this.mapCustMac(conn, custRetrunData.getCustid(), request.getAttribute(31).getAttributeValue().toString());
            }

            if (VLAN_RADIUS_PACK_ATTR != null && !this.isVlanMapped(conn, custRetrunData.getCustid()) && request.getAttribute(VLAN_RADIUS_PACK_ATTR) != null) {
                this.mapCustVlan(conn, custRetrunData.getCustid(), request.getAttribute(VLAN_RADIUS_PACK_ATTR).getAttributeValue().toString());
            }

            this.log.debug("{}Customer Data is  :{}:Quota Type:{}: Data Quota :{}: Time Quota :{}", this.PACKET_IDENTIFIER, custRetrunData, custRetrunData.getQuotatype(), custRetrunData.getUnusedquota(), custRetrunData.getTimequotaunused());
            if (custRetrunData.getQuotatype() != null) {
                if (custRetrunData.getQuotatype().equalsIgnoreCase("Data")) {
                    if (Double.parseDouble(custRetrunData.getUnusedquota()) <= 0.0) {
                        this.rejectAuthorization(custRetrunData, accessResponse, "Quota Reached", "Quota Reached");
                        return;
                    }
                } else if (custRetrunData.getQuotatype().equalsIgnoreCase("Time")) {
                    if (Double.parseDouble(custRetrunData.getTimequotaunused()) <= 0.0) {
                        this.rejectAuthorization(custRetrunData, accessResponse, "Quota Reached", "Quota Reached");
                        return;
                    }
                } else {
                    if (!custRetrunData.getQuotatype().equalsIgnoreCase("Both")) {
                        this.rejectAuthorization(custRetrunData, accessResponse, "Invalid Plan", "Invalid Quota Type");
                        return;
                    }

                    if (Double.parseDouble(custRetrunData.getUnusedquota()) <= 0.0 || Double.parseDouble(custRetrunData.getTimequotaunused()) <= 0.0) {
                        this.rejectAuthorization(custRetrunData, accessResponse, "Quota Reached", "Quota Reached");
                        return;
                    }
                }
            }

            this.log.info(this.PACKET_IDENTIFIER + "[AUTHORIZE]Quota Available:" + custRetrunData.isAuthStatus());
            if (custRetrunData.getServiceareaid() != null) {
                dbAuthDrive.populateServiceAreaDetails(conn, custRetrunData);
                if (custRetrunData.getVLANID() != null && custRetrunData.getVLANID().length() > 0 && accessReqVlan != null && accessReqVlan.length() > 0) {
                    if (Arrays.stream(custRetrunData.getVLANID().split(",")).noneMatch(accessReqVlan::equalsIgnoreCase)) {
                        this.rejectAuthorization(custRetrunData, accessResponse, "Service Area Vlan Mismatch", "Service Area Vlan Mismatch");
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
            this.log.debug("{}In processReplyItem, Access Request:{}", this.PACKET_IDENTIFIER, accessRequest);
            this.log.debug("{}In processReplyItem, custRetrunData:{}", this.PACKET_IDENTIFIER, custRetrunData);
            List<Integer> radProfile = custRetrunData.getRadiusprofile();
            if (radProfile != null && radProfile.size() > 0) {
                this.log.debug("{}Radius Profile Found:{}", this.PACKET_IDENTIFIER, radProfile.size());
                Cache cacheGroups = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.GROUPPOLICY);
                ValidateExpression validate = new ValidateExpression(this.PACKET_IDENTIFIER);

                for (Integer profileId : radProfile) {
                    Element profileElement = cacheGroups.get(profileId);
                    if (profileElement == null) {
                        continue;
                    }

                    ConcurrentHashMap<String, ConcurrentHashMap<Integer, ReplyItemData>> profileCheck = profileCheckItems(profileElement);

                    this.log.debug("{}For Profile Id :{}:Data is {}", this.PACKET_IDENTIFIER, profileId, profileCheck);

                    for (Map.Entry<String, ConcurrentHashMap<Integer, ReplyItemData>> entry : profileCheck.entrySet()) {
                        if (validate.checkExpression(entry.getKey(), accessRequest, accessResponse, custRetrunData)) {
                            this.log.debug("{}Adding Data for :{}:Data is {}", this.PACKET_IDENTIFIER, entry.getKey(), entry.getValue());
                            this.addReplyItems(entry.getValue().values(), custRetrunData, accessRequest, accessResponse);
                        }
                    }
                }
            } else {
                this.log.debug("{}Radius Profile NOT Attached:", this.PACKET_IDENTIFIER);
            }

            TreeMap<String, String> classAttributeParamMap = new TreeMap<>();
            if (custRetrunData.getCustPlanRelId() != null) {
                classAttributeParamMap.put("custpackid", String.valueOf(custRetrunData.getCustPlanRelId()));
            }

            classAttributeParamMap.put("qu", custRetrunData.isQuotaUnlimited() ? "1" : "0");
            classAttributeParamMap.put("sd", custRetrunData.isSpeedDowngradeFUP() ? "1" : "0");
            accessResponse.addAttribute("Class", RSAUtility.encrypt(RSAUtility.KEY, classAttributeParamMap));
            this.log.debug("{}Final Response:{}", this.PACKET_IDENTIFIER, accessResponse);
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error in processReplyItem:" + e.getMessage(), e);
        }

    }

    public void processInactiveUserReplyItem(RadiusPacket accessResponse, customerData custRetrunData, String strClientAddr, RadiusPacket accessRequest) {
        try {
            CachingConfigurations.getInstance();
            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
            Integer inactiveUserProfileId = parseInactiveUserProfileId(cacheServerAttribe);
            this.log.debug("{}INACTIVEUSER_PROFILEID:{}", this.PACKET_IDENTIFIER, inactiveUserProfileId);
            if (inactiveUserProfileId != null) {
                Cache cacheGroups = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.GROUPPOLICY);
                Element profileElement = cacheGroups.get(inactiveUserProfileId);
                if (profileElement != null) {
                    ConcurrentHashMap<String, ConcurrentHashMap<Integer, ReplyItemData>> profileCheck = profileCheckItems(profileElement);
                    this.log.debug("{}For Profile Id :{}:Data is {}", this.PACKET_IDENTIFIER, inactiveUserProfileId, profileCheck);
                    ValidateExpression validate = new ValidateExpression(this.PACKET_IDENTIFIER);
                    for (Map.Entry<String, ConcurrentHashMap<Integer, ReplyItemData>> entry : profileCheck.entrySet()) {
                        if (validate.checkExpression(entry.getKey(), accessRequest)) {
                            this.log.debug("{}Adding Data for :{}:Data is {}", this.PACKET_IDENTIFIER, entry.getKey(), entry.getValue());
                            this.addReplyItems(entry.getValue().values(), custRetrunData, accessRequest, accessResponse);
                        }
                    }
                }

                TreeMap<String, String> classAttributeParamMap = new TreeMap<>();
                classAttributeParamMap.put("custpackid", "-2");
                accessResponse.addAttribute("Class", RSAUtility.encrypt(RSAUtility.KEY, classAttributeParamMap));
                accessResponse.setPacketType(2);
            } else {
                this.log.debug("{}INACTIVEUSER_PROFILEID Radius Profile NOT Attached:", this.PACKET_IDENTIFIER);
            }
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + " Error in processInactiveUserReplyItem:" + e.getMessage(), e);
        }

    }

    private static Integer parseInactiveUserProfileId(Cache cacheServerAttribe) {
        try {
            return Integer.parseInt(cacheServerAttribe.get(AAAConstant.INACTIVEUSER_PROFILEID).getObjectValue().toString());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<String, ConcurrentHashMap<Integer, ReplyItemData>> profileCheckItems(Element profileElement) {
        return (ConcurrentHashMap<String, ConcurrentHashMap<Integer, ReplyItemData>>) profileElement.getObjectValue();
    }

    private void addReplyItems(Collection<ReplyItemData> replyItems, customerData custRetrunData, RadiusPacket accessRequest, RadiusPacket accessResponse) {
        for (ReplyItemData replyItem : replyItems) {
            String attributeValue = replyItem.getAttributeValue();
            if (attributeValue.contains("{") && attributeValue.contains("}")) {
                String dynaReplyValue = this.getDynamicReplyValue(attributeValue, custRetrunData);
                this.log.debug("{}For dynaReplyValue :{}", this.PACKET_IDENTIFIER, dynaReplyValue);
                if (isNullOrEmptyValue(dynaReplyValue)) {
                    this.log.debug("{}Checking with-in Request parameters", this.PACKET_IDENTIFIER);
                    String attrName = this.getDynamicAttributeName(attributeValue);
                    RadiusAttribute requestAttribute = null;

                    try {
                        requestAttribute = accessRequest.getAttribute(attrName);
                    } catch (Exception e) {
                        this.log.info(this.PACKET_IDENTIFIER + "Attribute not found in access request:" + attrName + ":" + e, e);
                    }

                    if (accessRequest != null && attrName != null && requestAttribute != null) {
                        try {
                            dynaReplyValue = accessRequest.getAttributeValue(attrName);
                        } catch (Exception e) {
                            this.log.info("{}Dynamic Attribute not found in request parameters:{}", this.PACKET_IDENTIFIER, attrName);
                        }
                    }
                }

                if (!isNullOrEmptyValue(dynaReplyValue)) {
                    RadiusAttribute ra = this.getRadiusAttribute(replyItem.getAttribute(), dynaReplyValue);
                    if (ra != null) {
                        accessResponse.addAttribute(ra);
                    }
                } else {
                    this.log.info("{}Skipping,{}as dynaReplyValue is null{}", this.PACKET_IDENTIFIER, replyItem.getAttribute(), dynaReplyValue);
                }
            } else {
                this.log.debug("{}Static Attribute", this.PACKET_IDENTIFIER);
                RadiusAttribute ra = this.getRadiusAttribute(replyItem.getAttribute(), attributeValue);
                if (ra != null) {
                    accessResponse.addAttribute(ra);
                } else {
                    this.log.debug("{}Static Skipping,{}as radiusAttribute is null", this.PACKET_IDENTIFIER, replyItem.getAttribute());
                }
            }
        }
    }

    private static boolean isNullOrEmptyValue(String value) {
        return value == null || "null".equalsIgnoreCase(value) || "".equalsIgnoreCase(value);
    }

    public String removeFirstandLast(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.deleteCharAt(str.length() - 1);
        sb.deleteCharAt(0);
        return sb.toString();
    }

    public String getDynamicReplyValue(String attributeStr, customerData custReturnData) {
        StringBuilder resultStr = new StringBuilder();

        try {
            for (String segment : attributeStr.split("\"")) {
                if (segment.length() == 0) {
                    continue;
                }

                String str = this.resolveDynamicVariables(segment, custReturnData);
                if (str == null) {
                    continue;
                }

                try {
                    if (containsArithmeticSymbol(str)) {
                        ScriptEngine engine = SCRIPT_ENGINE.get();
                        resultStr.append(engine.eval(str, engine.createBindings()));
                    } else {
                        resultStr.append(str);
                    }
                } catch (ScriptException e) {
                    this.log.info("{}Dynamic value Evaluation Failed for :{}", this.PACKET_IDENTIFIER, str);
                    resultStr.append(str);
                }
            }

            return resultStr.toString();
        } catch (Exception e) {
            this.log.error(this.PACKET_IDENTIFIER + "Error in getDynamicReplyValue:" + e, e);
            return resultStr.toString();
        }
    }

    /**
     * Replaces every {name} in the segment with its value, or returns null if any name has no value.
     */
    private String resolveDynamicVariables(String segment, customerData custReturnData) {
        String str = segment;
        while (str.contains("{") && str.contains("}")) {
            String dynamicAttrName = this.getDynamicAttributeName(str);
            String dynamicValue = this.getDynamicValue(custReturnData, dynamicAttrName);
            this.log.debug("{}Dynamic value For:{}:is {}", this.PACKET_IDENTIFIER, dynamicAttrName, dynamicValue);
            if (dynamicAttrName == null || dynamicValue == null) {
                return null;
            }

            str = str.replace("{" + dynamicAttrName + "}", dynamicValue);
        }

        return str;
    }

    private static boolean containsArithmeticSymbol(String str) {
        return str.contains("(") || str.contains(")") || str.contains("/") || str.contains("*") || str.contains("+") || str.contains("-");
    }

    public String getDynamicAttributeName(String str) {
        int openIdx = str.indexOf("{");
        int closeIdx = str.indexOf("}");
        return str.substring(openIdx + 1, closeIdx);
    }

    public String replaceDynamicValue(String str, String attr) {
        int openIdx = str.indexOf("{");
        int closeIdx = str.indexOf("}");
        return str.substring(0, openIdx) + attr + str.substring(closeIdx + 1);
    }

    public String getDynamicValue(customerData custRetrunData, String attriValue) {
        Function<customerData, String> getter = DYNAMIC_VALUE_GETTERS.get(attriValue);
        if (getter != null) {
            return getter.apply(custRetrunData);
        }

        if (custRetrunData.isDynamicAttrSupport()) {
            HashMap<String, String> qosPolicyParam = custRetrunData.getQospolicyparam();
            if (null != qosPolicyParam && qosPolicyParam.size() > 0 && qosPolicyParam.containsKey(attriValue)) {
                return qosPolicyParam.get(attriValue);
            }
        } else {
            getter = NON_DYNAMIC_QOS_GETTERS.get(attriValue);
            if (getter != null) {
                return getter.apply(custRetrunData);
            }
        }

        if (attriValue.startsWith("$fn") && attriValue.contains("(") && attriValue.contains(")")) {
            return this.getFunctionValue(custRetrunData, attriValue);
        }

        return null;
    }

    private String getFunctionValue(customerData custRetrunData, String attriValue) {
        try {
            String strParamIdentifier = attriValue.substring(attriValue.indexOf("(") + 1, attriValue.indexOf(")"));
            String paramCollectionType = strParamIdentifier.substring(0, strParamIdentifier.indexOf("["));
            String parameter = strParamIdentifier.substring(strParamIdentifier.indexOf("['") + 2, strParamIdentifier.indexOf("']"));
            String funcAttributeValue = null;
            String returnValue = null;
            boolean isServerConfig = false;
            boolean isCustomerAttrib = false;
            if (!"".equalsIgnoreCase(paramCollectionType) && !"".equalsIgnoreCase(parameter)) {
                try {
                    if ("$SERVER_ATTR".equalsIgnoreCase(paramCollectionType)) {
                        CachingConfigurations.getInstance();
                        Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
                        funcAttributeValue = cacheServerAttribe.get(parameter).getObjectValue().toString();
                        isServerConfig = true;
                    } else if ("$LOCAL".equalsIgnoreCase(paramCollectionType)) {
                        funcAttributeValue = parameter;
                    }
                } catch (Exception e) {
                    funcAttributeValue = null;
                }
            }

            if (funcAttributeValue != null && attriValue.startsWith("$fn_any")) {
                String currentCustValue = null;
                CustomerAttribute currCustAttrib = null;
                if (isServerConfig) {
                    CachingConfigurations.getInstance();
                    Cache cacheCustServerAttribes = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCUSTATTRIBUTE);
                    if (cacheCustServerAttribes != null && cacheCustServerAttribes.get(parameter) != null) {
                        ServerConfData configData = (ServerConfData) cacheCustServerAttribes.get(parameter).getObjectValue();
                        isCustomerAttrib = true;
                        currCustAttrib = new DBAuthenticationDriver().getCustomerAttribute(custRetrunData.getCustid(), parameter);
                        if (currCustAttrib != null && "Y".equalsIgnoreCase(configData.getEnsureUnique())) {
                            currentCustValue = currCustAttrib.getAttributeValue();
                        }
                    }
                }

                this.log.info("{} In getDynamicValue(): $fn, paramCollectionType:{},Collection:{},Attribute:{},AttributeValue:{}", this.PACKET_IDENTIFIER, strParamIdentifier, paramCollectionType, parameter, funcAttributeValue);
                returnValue = this.getUniqueNonRepeatingValue(funcAttributeValue.split(","), currentCustValue);
                if (isCustomerAttrib) {
                    if (currCustAttrib == null) {
                        currCustAttrib = new CustomerAttribute();
                        currCustAttrib.setCustid(custRetrunData.getCustid());
                        currCustAttrib.setAttributeName(parameter);
                    }

                    currCustAttrib.setAttributeValue(returnValue);
                    CommonThreadPool.getInstance().getExecutor().execute(new CustAttribWorker(this.PACKET_IDENTIFIER, currCustAttrib));
                }
            }

            return returnValue;
        } catch (Exception e) {
            this.log.error("error in getDynamic Value for Function:{}", e.getMessage(), e);
            return null;
        }
    }

    private DBAuthenticationDriver driver() {
        return new DBAuthenticationDriver(this.PACKET_IDENTIFIER);
    }

    public boolean isCustMacMapped(Connection conn, int custid) {
        return this.driver().findCustMacMapping(conn, custid);
    }

    public void mapCustMac(Connection conn, int custid, String macAddress) {
        this.driver().setCustMacAddress(conn, custid, macAddress);
    }

    public int findCustMacMapping(Connection conn, String macAddress) {
        return this.driver().findMacCustMapping(conn, macAddress);
    }

    private customerData getCustomerFromCustid(Connection conn, int custid) {
        return this.driver().getCustomerEntryFromCustid(conn, custid);
    }

    private boolean validateMacBinding(Connection conn, customerData custReturnData, String macAddress) {
        return this.driver().validateMacBinding(conn, custReturnData, macAddress);
    }

    private boolean validateVlanBinding(Connection conn, customerData custReturnData, String vlanId) {
        return this.driver().validateVlanBinding(conn, custReturnData, vlanId);
    }

    public boolean isVlanMapped(Connection conn, int custid) {
        return this.driver().findVlanMapping(conn, custid);
    }

    public void mapCustVlan(Connection conn, int custid, String vlanId) {
        this.driver().setCustVlan(conn, custid, vlanId);
    }

    public RadiusAttribute getRadiusAttribute(String attributeName, String attributeValue) {
        RadiusAttribute ra = null;

        try {
            Dictionary dictionary = DefaultDictionary.getDefaultDictionary();
            AttributeType attrbType = dictionary.getAttributeTypeByName(attributeName);
            this.log.debug("{}IN getAttributeValue for attributeName:{},attributeValue:{},attrbType:{}", this.PACKET_IDENTIFIER, attributeName, attributeValue, attrbType);
            if (attributeValue != null) {
                ra = RadiusAttribute.createRadiusAttribute(dictionary, attrbType.getVendorId(), attrbType.getTypeCode());
                Class<?> attributeClass = attrbType.getAttributeClass();
                if (attributeClass == IntegerAttribute.class) {
                    ((IntegerAttribute) ra).setAttributeValue(Integer.parseInt(attributeValue));
                } else if (attributeClass == LongAttribute.class) {
                    ((LongAttribute) ra).setAttributeValue(Long.parseLong(attributeValue));
                } else if (attributeClass == RadiusAttribute.class) {
                    ra.setAttributeData(attributeValue.getBytes());
                } else if (attributeClass == IpAttribute.class) {
                    ((IpAttribute) ra).setAttributeValue(attributeValue);
                } else if (attributeClass == Ipv6PrefixAttribute.class) {
                    ((Ipv6PrefixAttribute) ra).setAttributeValue(attributeValue);
                } else {
                    ra.setAttributeValue(attributeValue);
                }
            }
        } catch (Exception e) {
            ra = null;
            this.log.error(this.PACKET_IDENTIFIER + "Error in getAttributeValue for attributeName:" + attributeName + ",attributeValue:" + attributeValue + "," + e, e);
        }

        return ra;
    }

    public int findCustIPMapping(Connection conn, String ipAddress) {
        return this.driver().findCustIPMapping(conn, ipAddress);
    }

    public String getUniqueNonRepeatingValue(String[] valueList, String currentVal) {
        String returnValue = null;

        try {
            if (currentVal == null) {
                currentVal = String.valueOf(Math.random());
            }

            if (valueList != null) {
                int maxRetries = valueList.length * 3;
                int retryCount = 0;

                do {
                    if (valueList.length > 1) {
                        returnValue = valueList[ThreadLocalRandom.current().nextInt(valueList.length)];
                    } else {
                        returnValue = valueList[0];
                    }

                    ++retryCount;
                } while (currentVal.equalsIgnoreCase(returnValue) && retryCount <= maxRetries);

                if (retryCount > maxRetries) {
                    this.log.info("{} In getUniqueNonRepatingValue Unique value not found after {} retry", this.PACKET_IDENTIFIER, maxRetries);
                    returnValue = null;
                }
            }
        } catch (Exception e) {
            this.log.error("{} Error in getUniqueNonRepatingValue {} {}", this.PACKET_IDENTIFIER, e.getMessage(), e);
        }

        return returnValue;
    }
}
