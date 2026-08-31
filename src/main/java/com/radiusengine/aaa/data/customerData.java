/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.data;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class customerData {
    private String PACKET_IDENTIFIER;
    private String username;
    private String password;
    private String status;
    private int custid;
    private int balance;
    private Timestamp expirydate;
    private String checkitem;
    private HashMap<String, String> replyitem;
    private boolean authStatus;
    private int failcount;
    private Timestamp lastlogintime;
    private String strReplyMessage;
    private List<Integer> radiusprofile;
    private Timestamp lastpasswordupdate;
    private transient String cui;
    private String ASNNumber;
    private String BNGRouterInterface;
    private String BNGRouterName;
    private String IPPrefixes;
    private String IPV6Prefixes;
    private String LANIP;
    private String LANIPV6;
    private String LLAccountID;
    private String LLConnectionType;
    private String LLExpiryDate;
    private String LLMedium;
    private String LLServiceID;
    private String MACADDRESS;
    private String PeerIP;
    private String POOLIP;
    private String QOS;
    private String RDExport;
    private String RDValue;
    private String VLANID;
    private String VRFName;
    private String VSIID;
    private String VSIName;
    private String WANIP;
    private String WANIPV6;
    private String quotatype;
    private String quotaunit;
    private String totalquota;
    private String usedquota;
    private String totalquotakb;
    private String usedquotakb;
    private String timequotaunit;
    private String timetotalquota;
    private String timequotaused;
    private String timetotalquotasec;
    private String timeusedquotasec;
    private String unusedquota;
    private String unusedquotakb;
    private String timequotaunused;
    private String timeunusedquotasec;
    private String poolname;
    private int poolId;
    private String ipaddress;
    private String policyname;
    private String uploadspeed;
    private String downloadspeed;
    private String param1;
    private String param2;
    private String param3;
    private Integer maxSessionTime;
    private boolean basePlanExist = false;
    private boolean mactelflag = false;
    private boolean isFUPPlan = false;
    private boolean validUserExists = false;
    private boolean isMaxConcurSessionBreached = false;
    private boolean passwordsaveflag = false;
    private boolean dynamicAttrSupport = false;
    private boolean speedDowngradeFUP = false;
    private boolean isQuotaUnlimited = false;
    private boolean isIPOEEnabled = false;
    private String ipoeFramedIP;
    private Integer serviceareaid;
    private boolean isVlanEnable = false;
    private String networktype;
    private Integer partnerid;
    private String partnername;
    private String servicetype;
    private HashMap<String, String> qospolicyparam;
    private Integer custPlanRelId;
    private Integer currentPlanId;

    public customerData() {
    }

    public String getNetworktype() {
        return this.networktype;
    }

    public void setNetworktype(String networktype) {
        this.networktype = networktype;
    }

    public boolean isVlanEnable() {
        return this.isVlanEnable;
    }

    public void setVlanEnable(boolean vlanEnable) {
        this.isVlanEnable = vlanEnable;
    }

    public boolean isQuotaUnlimited() {
        return this.isQuotaUnlimited;
    }

    public void setQuotaUnlimited(boolean isQuotaUnlimited) {
        this.isQuotaUnlimited = isQuotaUnlimited;
    }

    public boolean isSpeedDowngradeFUP() {
        return this.speedDowngradeFUP;
    }

    public void setSpeedDowngradeFUP(boolean speedDownloadgradeFUP) {
        this.speedDowngradeFUP = speedDownloadgradeFUP;
    }

    public boolean isDynamicAttrSupport() {
        return this.dynamicAttrSupport;
    }

    public void setDynamicAttrSuppport(boolean dynamicAttrSupport) {
        this.dynamicAttrSupport = dynamicAttrSupport;
    }

    public HashMap<String, String> getQospolicyparam() {
        return this.qospolicyparam;
    }

    public void setQospolicyparam(HashMap<String, String> qospolicyparam) {
        this.qospolicyparam = qospolicyparam;
    }

    public boolean isMaxConcurSessionBreached() {
        return this.isMaxConcurSessionBreached;
    }

    public void setMaxConcurSessionBreached(boolean isMaxConcurSessionBreached) {
        this.isMaxConcurSessionBreached = isMaxConcurSessionBreached;
    }

    public boolean isPasswordsaveflag() {
        return this.passwordsaveflag;
    }

    public void setPasswordsaveflag(boolean passwordsaveflag) {
        this.passwordsaveflag = passwordsaveflag;
    }

    public List<Integer> getRadiusprofile() {
        return this.radiusprofile;
    }

    public void setRadiusprofile(List<Integer> radiusprofile) {
        this.radiusprofile = radiusprofile;
    }

    public Timestamp getLastpasswordupdate() {
        return this.lastpasswordupdate;
    }

    public void setLastpasswordupdate(Timestamp lastpasswordupdate) {
        this.lastpasswordupdate = lastpasswordupdate;
    }

    public String getStrReplyMessage() {
        return this.strReplyMessage;
    }

    public void setStrReplyMessage(String strReplyMessage) {
        this.strReplyMessage = strReplyMessage;
    }

    public Timestamp getLastlogintime() {
        return this.lastlogintime;
    }

    public void setLastlogintime(Timestamp lastlogintime) {
        this.lastlogintime = lastlogintime;
    }

    public int getFailcount() {
        return this.failcount;
    }

    public void setFailcount(int failcount) {
        this.failcount = failcount;
    }

    public int getCustid() {
        return this.custid;
    }

    public void setCustid(int custid) {
        this.custid = custid;
    }

    public String getCui() {
        return this.cui;
    }

    public void setCui(String cui) {
        this.cui = cui;
    }

    public boolean isAuthStatus() {
        return this.authStatus;
    }

    public void setAuthStatus(boolean authStatus) {
        this.authStatus = authStatus;
    }

    public void setReplyitem(HashMap<String, String> replyitem) {
        this.replyitem = replyitem;
    }

    public HashMap getReplyitem() {
        return this.replyitem;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMactelflag() {
        return this.mactelflag;
    }

    public void setMactelflag(boolean mactelflag) {
        this.mactelflag = mactelflag;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public Timestamp getExpirydate() {
        return this.expirydate;
    }

    public void setExpirydate(Timestamp expirydate) {
        this.expirydate = expirydate;
    }

    public String getCheckitem() {
        return this.checkitem;
    }

    public void setCheckitem(String checkitem) {
        this.checkitem = checkitem;
    }

    public String getASNNumber() {
        return this.ASNNumber;
    }

    public void setASNNumber(String aSNNumber) {
        this.ASNNumber = aSNNumber;
    }

    public String getBNGRouterInterface() {
        return this.BNGRouterInterface;
    }

    public void setBNGRouterInterface(String bNGRouterInterface) {
        this.BNGRouterInterface = bNGRouterInterface;
    }

    public String getBNGRouterName() {
        return this.BNGRouterName;
    }

    public void setBNGRouterName(String bNGRouterName) {
        this.BNGRouterName = bNGRouterName;
    }

    public String getIPPrefixes() {
        return this.IPPrefixes;
    }

    public void setIPPrefixes(String iPPrefixes) {
        this.IPPrefixes = iPPrefixes;
    }

    public String getIPV6Prefixes() {
        return this.IPV6Prefixes;
    }

    public void setIPV6Prefixes(String iPV6Prefixes) {
        this.IPV6Prefixes = iPV6Prefixes;
    }

    public String getLANIP() {
        return this.LANIP;
    }

    public void setLANIP(String lANIP) {
        this.LANIP = lANIP;
    }

    public String getLANIPV6() {
        return this.LANIPV6;
    }

    public void setLANIPV6(String lANIPV6) {
        this.LANIPV6 = lANIPV6;
    }

    public String getLLAccountID() {
        return this.LLAccountID;
    }

    public void setLLAccountID(String lLAccountID) {
        this.LLAccountID = lLAccountID;
    }

    public String getLLConnectionType() {
        return this.LLConnectionType;
    }

    public void setLLConnectionType(String lLConnectionType) {
        this.LLConnectionType = lLConnectionType;
    }

    public String getLLExpiryDate() {
        return this.LLExpiryDate;
    }

    public void setLLExpiryDate(String lLExpiryDate) {
        this.LLExpiryDate = lLExpiryDate;
    }

    public String getLLMedium() {
        return this.LLMedium;
    }

    public void setLLMedium(String lLMedium) {
        this.LLMedium = lLMedium;
    }

    public String getLLServiceID() {
        return this.LLServiceID;
    }

    public void setLLServiceID(String lLServiceID) {
        this.LLServiceID = lLServiceID;
    }

    public String getMACADDRESS() {
        return this.MACADDRESS;
    }

    public void setMACADDRESS(String mACADDRESS) {
        this.MACADDRESS = mACADDRESS;
    }

    public String getPeerIP() {
        return this.PeerIP;
    }

    public void setPeerIP(String peerIP) {
        this.PeerIP = peerIP;
    }

    public String getPOOLIP() {
        return this.POOLIP;
    }

    public void setPOOLIP(String pOOLIP) {
        this.POOLIP = pOOLIP;
    }

    public String getQOS() {
        return this.QOS;
    }

    public void setQOS(String qOS) {
        this.QOS = qOS;
    }

    public String getRDExport() {
        return this.RDExport;
    }

    public void setRDExport(String rDExport) {
        this.RDExport = rDExport;
    }

    public String getRDValue() {
        return this.RDValue;
    }

    public void setRDValue(String rDValue) {
        this.RDValue = rDValue;
    }

    public String getVLANID() {
        return this.VLANID;
    }

    public void setVLANID(String vLANID) {
        this.VLANID = vLANID;
    }

    public String getVRFName() {
        return this.VRFName;
    }

    public void setVRFName(String vRFName) {
        this.VRFName = vRFName;
    }

    public String getVSIID() {
        return this.VSIID;
    }

    public void setVSIID(String vSIID) {
        this.VSIID = vSIID;
    }

    public String getVSIName() {
        return this.VSIName;
    }

    public void setVSIName(String vSIName) {
        this.VSIName = vSIName;
    }

    public String getWANIP() {
        return this.WANIP;
    }

    public void setWANIP(String wANIP) {
        this.WANIP = wANIP;
    }

    public String getWANIPV6() {
        return this.WANIPV6;
    }

    public void setWANIPV6(String wANIPV6) {
        this.WANIPV6 = wANIPV6;
    }

    public String getQuotatype() {
        return this.quotatype;
    }

    public void setQuotatype(String quotatype) {
        this.quotatype = quotatype;
    }

    public String getQuotaunit() {
        return this.quotaunit;
    }

    public void setQuotaunit(String quotaunit) {
        this.quotaunit = quotaunit;
    }

    public String getTotalquota() {
        return this.totalquota;
    }

    public void setTotalquota(String totalquota) {
        this.totalquota = totalquota;
    }

    public String getUsedquota() {
        return this.usedquota;
    }

    public void setUsedquota(String usedquota) {
        this.usedquota = usedquota;
    }

    public String getTotalquotakb() {
        return this.totalquotakb;
    }

    public void setTotalquotakb(String totalquotakb) {
        this.totalquotakb = totalquotakb;
    }

    public String getUsedquotakb() {
        return this.usedquotakb;
    }

    public void setUsedquotakb(String usedquotakb) {
        this.usedquotakb = usedquotakb;
    }

    public String getTimequotaunit() {
        return this.timequotaunit;
    }

    public void setTimequotaunit(String timequotaunit) {
        this.timequotaunit = timequotaunit;
    }

    public String getTimetotalquota() {
        return this.timetotalquota;
    }

    public void setTimetotalquota(String timetotalquota) {
        this.timetotalquota = timetotalquota;
    }

    public String getTimequotaused() {
        return this.timequotaused;
    }

    public void setTimequotaused(String timequotaused) {
        this.timequotaused = timequotaused;
    }

    public String getTimetotalquotasec() {
        return this.timetotalquotasec;
    }

    public void setTimetotalquotasec(String timetotalquotasec) {
        this.timetotalquotasec = timetotalquotasec;
    }

    public String getTimeusedquotasec() {
        return this.timeusedquotasec;
    }

    public void setTimeusedquotasec(String timeusedquotasec) {
        this.timeusedquotasec = timeusedquotasec;
    }

    public String getUnusedquota() {
        return this.unusedquota;
    }

    public void setUnusedquota(String unusedquota) {
        this.unusedquota = unusedquota;
    }

    public String getUnusedquotakb() {
        return this.unusedquotakb;
    }

    public void setUnusedquotakb(String unusedquotakb) {
        this.unusedquotakb = unusedquotakb;
    }

    public String getTimequotaunused() {
        return this.timequotaunused;
    }

    public void setTimequotaunused(String timequotaunused) {
        this.timequotaunused = timequotaunused;
    }

    public String getTimeunusedquotasec() {
        return this.timeunusedquotasec;
    }

    public void setTimeunusedquotasec(String timeunusedquotasec) {
        this.timeunusedquotasec = timeunusedquotasec;
    }

    public String getPoolname() {
        return this.poolname;
    }

    public void setPoolname(String poolname) {
        this.poolname = poolname;
    }

    public int getPoolId() {
        return this.poolId;
    }

    public void setPoolId(int poolId) {
        this.poolId = poolId;
    }

    public String getIpaddress() {
        return this.ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public Integer getMaxSessionTime() {
        return this.maxSessionTime;
    }

    public void setMaxSessionTime(Integer sessionTime) {
        this.maxSessionTime = sessionTime;
    }

    public boolean isBasePlanExist() {
        return this.basePlanExist;
    }

    public void setBasePlanExist(boolean basePlanExist) {
        this.basePlanExist = basePlanExist;
    }

    public String toString() {
        return "customerData [PACKET_IDENTIFIER=" + this.PACKET_IDENTIFIER + ", username=" + this.username + ", password=" + this.password + ", status=" + this.status + ", custid=" + this.custid + ", balance=" + this.balance + ", expirydate=" + this.expirydate + ", checkitem=" + this.checkitem + ", replyitem=" + this.replyitem + ", authStatus=" + this.authStatus + ", failcount=" + this.failcount + ", lastlogintime=" + this.lastlogintime + ", strReplyMessage=" + this.strReplyMessage + ", radiusprofile=" + this.radiusprofile + ", lastpasswordupdate=" + this.lastpasswordupdate + ", ASNNumber=" + this.ASNNumber + ", BNGRouterInterface=" + this.BNGRouterInterface + ", BNGRouterName=, quotatype=" + this.quotatype + ", quotaunit=" + this.quotaunit + ", totalquota=" + this.totalquota + ", usedquota=" + this.usedquota + ", totalquotakb=" + this.totalquotakb + ", usedquotakb=" + this.usedquotakb + ", timequotaunit=" + this.timequotaunit + ", timetotalquota=" + this.timetotalquota + ", timequotaused=" + this.timequotaused + ", timetotalquotasec=" + this.timetotalquotasec + ", timeusedquotasec=" + this.timeusedquotasec + ", unusedquota=" + this.unusedquota + ", unusedquotakb=" + this.unusedquotakb + ", timequotaunused=" + this.timequotaunused + ", timeunusedquotasec=" + this.timeunusedquotasec + ", poolname=" + this.poolname + ", poolId=" + this.poolId + ", ipaddress=" + this.ipaddress + ", policyname=" + this.policyname + ", uploadspeed=" + this.uploadspeed + ", downloadspeed=" + this.downloadspeed + ", param1=" + this.param1 + ", param2=" + this.param2 + ", param3=" + this.param3 + ", maxSessionTime=" + this.maxSessionTime + ", basePlanExist=" + this.basePlanExist + ", mactelflag=" + this.mactelflag + ", isFUPPlan=" + this.isFUPPlan + ", validUserExists=" + this.validUserExists + ", isMaxConcurSessionBreached=" + this.isMaxConcurSessionBreached + ", passwordsaveflag=" + this.passwordsaveflag + ", dynamicAttrSupport=" + this.dynamicAttrSupport + ", speedDowngradeFUP=" + this.speedDowngradeFUP + ", isQuotaUnlimited=" + this.isQuotaUnlimited + ", qospolicyparam=" + this.qospolicyparam + ", custPlanRelId=" + this.custPlanRelId + ", currentPlanId=" + this.currentPlanId + "]";
    }

    public String getPolicyname() {
        return this.policyname;
    }

    public void setPolicyname(String policyname) {
        this.policyname = policyname;
    }

    public String getUploadspeed() {
        return this.uploadspeed;
    }

    public void setUploadspeed(String uploadspeed) {
        this.uploadspeed = uploadspeed;
    }

    public String getDownloadspeed() {
        return this.downloadspeed;
    }

    public void setDownloadspeed(String downloadspeed) {
        this.downloadspeed = downloadspeed;
    }

    public String getParam1() {
        return this.param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return this.param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam3() {
        return this.param3;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public boolean isFUPPlan() {
        return this.isFUPPlan;
    }

    public void setFUPPlan(boolean isFUPPlan) {
        this.isFUPPlan = isFUPPlan;
    }

    public Integer getCustPlanRelId() {
        return this.custPlanRelId;
    }

    public void setCustPlanRelId(Integer custPlanRelId) {
        this.custPlanRelId = custPlanRelId;
    }

    public Integer getCurrentPlanId() {
        return this.currentPlanId;
    }

    public void setCurrentPlanId(Integer currentPlanId) {
        this.currentPlanId = currentPlanId;
    }

    public boolean isValidUserExists() {
        return this.validUserExists;
    }

    public void setValidUserExists(boolean validUserExists) {
        this.validUserExists = validUserExists;
    }

    public String getPACKET_IDENTIFIER() {
        return this.PACKET_IDENTIFIER != null ? this.PACKET_IDENTIFIER : "";
    }

    public void setPACKET_IDENTIFIER(String pACKET_IDENTIFIER) {
        this.PACKET_IDENTIFIER = pACKET_IDENTIFIER;
    }

    public boolean isIPOEEnabled() {
        return this.isIPOEEnabled;
    }

    public void setIPOEEnabled(boolean isIPOEEnabled) {
        this.isIPOEEnabled = isIPOEEnabled;
    }

    public String getIpoeFramedIP() {
        return this.ipoeFramedIP;
    }

    public void setIpoeFramedIP(String ipoeFramedIP) {
        this.ipoeFramedIP = ipoeFramedIP;
    }

    public Integer getServiceareaid() {
        return this.serviceareaid;
    }

    public void setServiceareaid(Integer serviceareaid) {
        this.serviceareaid = serviceareaid;
    }

    public Integer getPartnerId() {
        return this.partnerid;
    }

    public void setPartnerId(Integer partnerid) {
        this.partnerid = partnerid;
    }

    public String getPartnerName() {
        return this.partnername;
    }

    public void setPartnerName(String partnername) {
        this.partnername = partnername;
    }

    public String getServicetype() {
        return this.servicetype;
    }

    public void setServicetype(String servicetype) {
        this.servicetype = servicetype;
    }
}
