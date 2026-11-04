/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.db;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.CustomerAttribute;
import com.radiusengine.aaa.data.customerData;
import com.radiusengine.aaa.server.CachingConfigurations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBAuthenticationDriver {
    private String PACKET_IDENTIFIER = "[DBAuthenticationDriver]";
    private static String strRadiusCustomerQuery = "select custid,username,password,cstatus,last_login_time,failcount,last_password_change,defaultpoolid,setpasswordfromradiusflag,mactelflag,servicearea_id,vlanflag,networktype,c.partnerid,partnername,servicetype from tblcustomers c inner join tblpartners t  on c.partnerid =t.PARTNERID where username=? and is_deleted=0";
    private static String strRadiusCustomerUpdateQuery = "update tblcustomers SET failcount=?,last_login_time=? where username=? and is_deleted=0";
    private static String strCheckMacAddressQuery = "select custid,macaddress from tblcustmacmapping where is_deleted = 0 and custid = ?";
    private static String strCheckVlanQuery = "select custid,VLANID from tblcustomers where is_deleted = 0 and custid = ? and (VLANID is not null or VLANID != '')";
    private static String strRadiusAuthenticationResponse = "insert into tblauthresponse(username,replymessage,packettype,clientip,clientgroup) values(?,?,?,?,?)";
    private static String strGetCustomerAttribute = "select custattrid,attribname,attribvalue,custid from tblcustattribs where custid=? and attribname=?";
    private static String strRadiusAuthorization = "select pl.planid,pl.custpackageid,basepolicyname,baseuploadspeed,basedownloadspeed,baseparam1,baseparam2,baseparam3,thpolicyname,thuploadspeed,thdownloadspeed,thparam1,thparam2,thparam3,plangroup,datacategory,qt.quotatype,qt.quotaunit,totalquota,usedquota,totalquotakb,usedquotakb,timequotaunit,timetotalquota,timequotaused,timetotalquotasec,timeusedquotasec,pl.startdate,pl.enddate,pl.expirydate, TIMESTAMPDIFF(SECOND, sysdate(),expirydate) sessiontime, speeddowngradeflag  from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp  where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id and pl.custpackageid=qt.custpackageid and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.DATA_SERVICE_ID).toString() + "' and pl.custid=?  and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() order by pl.expirydate asc limit 1";
    public static String strRadiusAuthorizationWithDynamicAttribute = "select pl.planid,pl.custpackageid,basepolicyname,plangroup,datacategory,qt.quotatype,qt.quotaunit,totalquota,usedquota,totalquotakb,usedquotakb,timequotaunit,timetotalquota,timequotaused,timetotalquotasec,timeusedquotasec,pl.startdate,pl.enddate,pl.expirydate, TIMESTAMPDIFF(SECOND, sysdate(),expirydate) sessiontime , qp.id,speeddowngradeflag from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp  where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id and pl.custpackageid=qt.custpackageid and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and pl.custid=?  and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() order by pl.expirydate asc limit 1";
    private static String strGetActivePlanDetails = "select pl.planid,pl.custpackageid,basepolicyname,baseuploadspeed,basedownloadspeed,baseparam1,baseparam2,baseparam3,thpolicyname,thuploadspeed,thdownloadspeed,thparam1,thparam2,thparam3,plangroup,datacategory,qt.quotatype,qt.quotaunit,totalquota,usedquota,totalquotakb,usedquotakb,timequotaunit,timetotalquota,timequotaused,timetotalquotasec,timeusedquotasec,pl.startdate,pl.enddate,pl.expirydate, TIMESTAMPDIFF(SECOND, sysdate(),pl.expirydate) sessiontime   from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp, tblcustomers c   where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id  and pl.custpackageid=qt.custpackageid  and pl.custid=c.custid and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() and c.username=? and c.is_deleted=0 order by pl.expirydate desc limit 1";
    private static String strGetActivePlanDetailsWithDynamicAttribute = "select pl.planid,pl.custpackageid,basepolicyname,plangroup,datacategory,qt.quotatype,qt.quotaunit,totalquota,usedquota,totalquotakb,usedquotakb,timequotaunit,timetotalquota,timequotaused,timetotalquotasec,timeusedquotasec,pl.startdate,pl.enddate,pl.expirydate, TIMESTAMPDIFF(SECOND, sysdate(),pl.expirydate) sessiontime , qp.id from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp, tblcustomers c   where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id  and pl.custpackageid=qt.custpackageid  and pl.custid=c.custid and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() and c.username=? and c.is_deleted=0 order by pl.expirydate desc limit 1";
    private static String strGetLastActivePlanDetails = "select pl.planid,pl.custpackageid,basepolicyname,baseuploadspeed,basedownloadspeed,baseparam1,baseparam2,baseparam3,thpolicyname,thuploadspeed,thdownloadspeed,thparam1,thparam2,thparam3,plangroup,datacategory,qt.quotatype,qt.quotaunit,totalquota,usedquota,totalquotakb,usedquotakb,timequotaunit, timetotalquota,timequotaused,timetotalquotasec,timeusedquotasec,pl.startdate,pl.enddate,pl.expirydate, TIMESTAMPDIFF(SECOND, sysdate(),pl.expirydate) sessiontime, case when pl.enddate is not null then pl.enddate else pl.expirydate end  as planenddate  from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp, tblcustomers c  where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id and pl.custpackageid=qt.custpackageid  and pl.custid=c.custid and c.is_deleted=0 and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and c.username=? and pl.startdate <= sysdate() order by planenddate desc limit 1";
    private static String strGetLastActivePlanDetailsWithDynamicAttribute = "select pl.planid,pl.custpackageid,basepolicyname,plangroup,datacategory,qt.quotatype,qt.quotaunit,totalquota,usedquota,totalquotakb,usedquotakb,timequotaunit,timetotalquota,timequotaused,timetotalquotasec,timeusedquotasec,pl.startdate,pl.enddate,pl.expirydate, TIMESTAMPDIFF(SECOND, sysdate(),pl.expirydate) sessiontime , qp.id, case when pl.enddate is not null then pl.enddate else pl.expirydate end  as planenddate from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp, tblcustomers c   where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id  and pl.custpackageid=qt.custpackageid  and pl.custid=c.custid and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and pl.startdate <= sysdate() and c.username=? and c.is_deleted=0 order by planenddate desc limit 1";
    private static String qosPolicyParamQuery = "select  qosparam.radius_pol_param_name  , qosparam.base_param_value , qosparam.th_param_value \nfrom tblqospolicyparam qosparam where is_delete = 0 and qosparam.qos_pol_id =  ?";
    private static String strCheckBasePlanExists = "select planid from tblcustpackagerel pl, tblmpostpaidplan plan  where plan.POSTPAIDPLANID =pl.planid and custid=? and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and plan.plangroup in (" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.BASE_PLAN_GROUP).toString() + ") and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() order by pl.expirydate asc limit 1;";
    private static String strRadiusUpdateQuota = "update tblcustquotadtls set usedquota=usedquota+?,usedquotakb=usedquotakb+?,timequotaused=timequotaused+?,timeusedquotasec=timeusedquotasec+? where custid=? and custpackageid=?";
    private static String strRadiusUpdateQuotaWithBWFlag = "update tblcustquotadtls set usedquota=usedquota+?,usedquotakb=usedquotakb+?,timequotaused=timequotaused+?,timeusedquotasec=timeusedquotasec+?, speeddowngradeflag=? where custid=? and custpackageid=?";
    private static String strFindCustMacMappingQuery = "select custid,macaddress from tblcustmacmapping where is_deleted=0 and custid=? ";
    private static String strFindMacCustMappingQuery = "select custid,macaddress from tblcustmacmapping where is_deleted=0 and lower(macaddress)=?";
    private static String strFindVlanCustMappingQuery = "select custid,VLANID from tblcustomers where is_deleted=0 and custid=?";
    private static String strAddCustMacMapping = "insert into tblcustmacmapping(custid,macaddress) values(?,?)";
    private static String strAddCustVlanMapping = "update tblcustomers set VLANID=? where custid=?";
    private static String strRadiusFindCustomerCustidQuery = "select custid,username,password from tblcustomers where custid=? and is_deleted=0";
    private static String strGetCustomerActivePlanMaxSession = "select pl.planid,plan.maxconcurrentsession maxconcurrentsession From tblcustpackagerel pl INNER JOIN tblmpostpaidplan plan on plan.POSTPAIDPLANID =pl.planid where lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and plan.plangroup in (" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.BASE_PLAN_GROUP).toString() + ") and pl.custid =? and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() order by pl.expirydate asc limit 1;";
    private static String strCurrentLiveSessionCount = "select cust.custid,cust.username,case when livesessions is null then 0 else  livesessions end as livesessioncount from tblcustomers cust INNER  JOIN (select username,count(*) as livesessions From tblliveuser group by username) as lc on lc.UserName =cust.username  and cust.custid = ?";
    private static String strPoolNameFromPoolId = "select pool_name from tblippool where pool_id=?";
    private static String strStaticIPQuery = "select pooldtls.ip_address from tblcustchargedtls custcharges inner join tblcharges charges  on custcharges.chargeid = charges.CHARGEID and charges.chargecategory ='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.IP_CHARGE_CATEGORY).toString() + "' inner join tblippooldtls pooldtls on custcharges.ippooldtlsid =  pooldtls.pool_details_id and custcharges.purchase_entity_id = pooldtls.allocated_id and pooldtls.status = 'Allocated'  where custid=? and custcharges.enddate >= sysdate() and custcharges.startdate <= sysdate()";
    private static String strStaticIPQueryNew = "select pooldtls.ip_address from tblcustchargedtls custcharges inner join tblcharges charges  on custcharges.chargeid = charges.CHARGEID  and charges.chargecategory = '" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.IP_CHARGE_CATEGORY).toString() + "' inner join tblippooldtls pooldtls on custcharges.ippooldtlsid = pooldtls.pool_details_id \n inner join tblipallocationdtls allocation on allocation.id = custcharges.purchase_entity_id\n and pooldtls.status = 'Allocated' \n where custid = ? and custcharges.enddate >= sysdate() and custcharges.is_reversed = 0  and custcharges.startdate <= sysdate() and allocation.is_released = 0";
    private static String strqosPolicyQuery = "select basepolicyname,baseuploadspeed,basedownloadspeed,baseparam1,baseparam2,baseparam3,thpolicyname,thuploadspeed,thdownloadspeed,thparam1,thparam2,thparam3, totalquota,usedquota,plangroup,datacategory,plangroup from tbl_qos_policy qp,tblcustpackagerel pl, tblcustquotadtls qt, tblmpostpaidplan mpp  where mpp.postpaidplanid=pl.planid and pl.qospolicyid =qp.id and pl.custpackageid=qt.custpackageid and lower(pl.service)='" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, "dataServiceId").toString() + "' and pl.custid=?  and (pl.enddate IS NULL OR  pl.enddate >= sysdate()) and pl.expirydate >= sysdate() and pl.startdate <= sysdate() order by pl.expirydate asc limit 1;";
    private static String strCustomePackageRadiusPolicyQuery = "select radiusprofileid  from tblcustpackagerel pl, tblpostpaidplanradiusprofilerel radprof where radprof.POSTPAIDPLANID =pl.planid and pl.custpackageid=? ";
    private static String updatePasswordOnFirstLogin = "update tblcustomers set password = ? , setpasswordfromradiusflag = true where setpasswordfromradiusflag = false and is_deleted = false and username = ?";
    private static String strCustomerServiceAreaDetialsQuery = "select vlan_id from tblservicearea where service_area_id=? and is_deleted=0";
    private static String strCustIPQuery = "select custid \n from tblcustchargedtls custcharges \n inner join tblcharges charges  on custcharges.chargeid = charges.CHARGEID and charges.chargecategory = '" + CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.IP_CHARGE_CATEGORY).toString() + "' inner join tblippooldtls pooldtls on custcharges.ippooldtlsid = pooldtls.pool_details_id\n inner join tblipallocationdtls allocation on allocation.id = custcharges.purchase_entity_id and pooldtls.status = 'Allocated'\n where pooldtls.ip_address = ? and custcharges.enddate >= sysdate() and custcharges.is_reversed = 0  and custcharges.startdate <= sysdate() and allocation.is_released = 0\n";
    private static String strCustAttribUpdate = "update tblcustattribs set attribvalue=?,lastmodifieddate=sysdate() where custattrid=?";
    private static String strCustAttribInsert = "insert into tblcustattribs(custid,attribname,attribvalue) values(?,?,?)";
    static Logger log = LoggerFactory.getLogger("radiuslog");

    public DBAuthenticationDriver(String pACKET_IDENTIFIER) {
        this.PACKET_IDENTIFIER = pACKET_IDENTIFIER + "[DBAuthenticationDriver]";
    }

    public DBAuthenticationDriver() {
    }

    public boolean checkServerConfForPasswordSave() {
        try {
            CachingConfigurations.getInstance(); // makes sure the cache is initialised
            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
            Integer SAVEPASSWORDONFIRSTLOGIN = null;

            try {
                SAVEPASSWORDONFIRSTLOGIN = Integer.parseInt(cacheServerAttribe.get(AAAConstant.SAVEPASSWORDONFIRSTLOGIN).getObjectValue().toString());
            } catch (Exception e) {
                SAVEPASSWORDONFIRSTLOGIN = null;
            }

            log.debug(this.PACKET_IDENTIFIER + "SAVEPASSWORDONFIRSTLOGIN:" + SAVEPASSWORDONFIRSTLOGIN);
            if (SAVEPASSWORDONFIRSTLOGIN != null && SAVEPASSWORDONFIRSTLOGIN.equals(1)) {
                return true;
            }

            log.debug(this.PACKET_IDENTIFIER + "SAVEPASSWORDONFIRSTLOGIN Server Conf Is Off..");
        } catch (Exception ex) {
            log.error(" [checkServerConfForPasswordSave] " + ex.getMessage(), ex);
        }

        return false;
    }

    public boolean checkServerConf(String attrName, Integer checkValue) {
        try {
            CachingConfigurations.getInstance(); // makes sure the cache is initialised
            Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
            Integer attrValue = null;

            try {
                attrValue = Integer.parseInt(cacheServerAttribe.get(attrName).getObjectValue().toString());
            } catch (Exception e) {
                log.error(this.PACKET_IDENTIFIER + " Error In Found ServerConf ");
                attrValue = null;
            }

            log.debug(this.PACKET_IDENTIFIER + "AttrName : " + attrName + "AttrValue : " + attrValue);
            if (attrValue != null && attrValue.equals(checkValue)) {
                return true;
            }

            log.debug(this.PACKET_IDENTIFIER + "For AttributeName " + attrName + "  Server Conf Is Off..");
        } catch (Exception ex) {
            log.error(" [checkServerConf] " + ex.getMessage(), ex);
        }

        return false;
    }

    public boolean validateMacBinding(customerData custPassedData, String macAddress) {
        try (Connection conn = DataSource.getConnection()) {
            return validateMacBinding(conn, custPassedData, macAddress);
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "IN GET verifyMacAddress():" + e, e);
            custPassedData.setMactelflag(true);
            return true;
        }
    }

    public boolean validateMacBinding(Connection conn, customerData custPassedData, String macAddress) {
        boolean recordExists = false;

        try {
            log.debug(this.PACKET_IDENTIFIER + "IN GET verifyMacAddress CHECK FROM DB :" + custPassedData.getUsername() + " :QUERY: " + strCheckMacAddressQuery);
            try (PreparedStatement stmt1 = conn.prepareStatement(strCheckMacAddressQuery)) {
                stmt1.setInt(1, custPassedData.getCustid());
                try (ResultSet rset1 = stmt1.executeQuery()) {
                    while (rset1.next()) {
                        recordExists = true;
                        String dbMacAddress = rset1.getString("macaddress");
                        if (null != dbMacAddress && dbMacAddress.equalsIgnoreCase(macAddress)) {
                            custPassedData.setMactelflag(true);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "IN GET verifyMacAddress():" + e, e);
            recordExists = false;
        }

        if (!recordExists) {
            custPassedData.setMactelflag(true);
            return true;
        } else {
            custPassedData.setStrReplyMessage("Mac validation failed!");
            custPassedData.setMactelflag(false);
            return false;
        }
    }

    public boolean validateVlanBinding(customerData custPassedData, String vlan) {
        try (Connection conn = DataSource.getConnection()) {
            return validateVlanBinding(conn, custPassedData, vlan);
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "IN GET validateVlanBinding():" + e, e);
            custPassedData.setVlanEnable(true);
            return true;
        }
    }

    public boolean validateVlanBinding(Connection conn, customerData custPassedData, String vlan) {
        boolean recordExists = false;

        try {
            log.debug(this.PACKET_IDENTIFIER + "IN GET validateVlanBinding CHECK FROM DB :" + custPassedData.getUsername() + " :QUERY: " + strCheckVlanQuery);
            try (PreparedStatement stmt1 = conn.prepareStatement(strCheckVlanQuery)) {
                stmt1.setInt(1, custPassedData.getCustid());
                try (ResultSet rset1 = stmt1.executeQuery()) {
                    while (rset1.next()) {
                        recordExists = true;
                        String dbVlan = rset1.getString("VLANID");
                        if (null != dbVlan && dbVlan.equalsIgnoreCase(vlan)) {
                            custPassedData.setVlanEnable(true);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "IN GET validateVlanBinding():" + e, e);
            recordExists = false;
        }

        if (!recordExists) {
            custPassedData.setVlanEnable(true);
            return true;
        } else {
            custPassedData.setStrReplyMessage("Vlan validation failed!");
            custPassedData.setVlanEnable(false);
            return false;
        }
    }

    public customerData setPasswordOnFirstLogin(customerData custData, String strPassword, String username) {
        try (Connection conn = DataSource.getConnection()) {
            return setPasswordOnFirstLogin(conn, custData, strPassword, username);
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "Error in setPasswordOnFirstLogin:" + e, e);
            return custData;
        }
    }

    public customerData setPasswordOnFirstLogin(Connection conn, customerData custData, String strPassword, String username) {
        try {
            log.debug(this.PACKET_IDENTIFIER + "IN SET setPasswordOnFirstLogin For Username :" + username + ":QUERY:" + updatePasswordOnFirstLogin);
            try (PreparedStatement stmt1 = conn.prepareStatement(updatePasswordOnFirstLogin)) {
                stmt1.setString(1, strPassword);
                stmt1.setString(2, username);
                int count = stmt1.executeUpdate();
                custData.setAuthStatus(count > 0);
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in setPasswordOnFirstLogin:" + e, e);
        }

        return custData;
    }

    public customerData authenticateCustomer(customerData custPassedData) {
        try (Connection conn = DataSource.getConnection()) {
            return authenticateCustomer(conn, custPassedData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "error in authenticateCustomer():" + e, e);
            customerData custDBData = new customerData();
            custDBData.setAuthStatus(false);
            custDBData.setStrReplyMessage("Authentication Fail.Username Not Found - \"" + custPassedData.getUsername() + '"');
            return custDBData;
        }
    }

    public customerData authenticateCustomer(Connection conn, customerData custPassedData) {
        customerData custDBData = new customerData();

        try {
            custDBData.setAuthStatus(false);
            custDBData = this.getDBCustomer(conn, custPassedData.getUsername());
            if (custDBData == null) {
                custDBData = new customerData();
                custDBData.setAuthStatus(false);
                custDBData.setStrReplyMessage("Authentication Fail.Username Not Found - \"" + custPassedData.getUsername() + '"');
                return custDBData;
            } else {
                if (custPassedData.getUsername().equalsIgnoreCase(custDBData.getUsername()) && custPassedData.getPassword().equals(custDBData.getPassword())) {
                    custDBData.setValidUserExists(true);
                }

                if (this.checkServerConf(AAAConstant.ALLOW_BLANK_PWD_AUTH, AAAConstant.ATTRVALUEFORCHECK)
                        && (custPassedData.getPassword().equals("") || custPassedData.getPassword().length() == 0 || custPassedData.getPassword().equals(" ")) && custPassedData.getUsername().equalsIgnoreCase(custDBData.getUsername())) {
                    custDBData.setAuthStatus(true);
                    custDBData.setStrReplyMessage(AAAConstant.REPLYMSG);
                    return custDBData;
                } else if (custPassedData.getUsername().equalsIgnoreCase(custDBData.getUsername()) && custPassedData.getPassword().equals(custDBData.getPassword()) && custDBData.getStatus().equalsIgnoreCase(AAAConstant.CUSTSTATUS)) {
                    custDBData.setAuthStatus(true);
                    custDBData.setStrReplyMessage(AAAConstant.REPLYMSG);
                    return custDBData;
                } else if (custPassedData.getUsername().equalsIgnoreCase(custDBData.getUsername()) && !custDBData.getStatus().equalsIgnoreCase(AAAConstant.CUSTSTATUS)) {
                    custDBData.setStrReplyMessage("Authentication Fail.Customer Not Active");
                    custDBData.setAuthStatus(false);
                    return custDBData;
                } else if (custPassedData.getUsername().equalsIgnoreCase(custDBData.getUsername()) && !custPassedData.getPassword().equals(custDBData.getPassword())) {
                    custDBData.setStrReplyMessage("Authentication Fail.Password Not Match - \"" + custPassedData.getPassword() + '"');
                    custDBData.setAuthStatus(false);
                    return custDBData;
                } else if (!custPassedData.getUsername().equalsIgnoreCase(custDBData.getUsername())) {
                    custDBData.setStrReplyMessage("Authentication Fail.Username Not Found - \"" + custPassedData.getUsername() + '"');
                    custDBData.setAuthStatus(false);
                    return custDBData;
                } else {
                    custDBData.setStrReplyMessage(AAAConstant.REPLYMSG_UNKNOWN);
                    custDBData.setAuthStatus(false);
                    return custDBData;
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "error in authenticateCustomer():" + e, e);
            return custDBData;
        }
    }

    public void populateAuthorizationDetails(customerData custPassedData) {
        try (Connection conn = DataSource.getConnection()) {
            populateAuthorizationDetails(conn, custPassedData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populateAuthorizationDetails():" + e, e);
        }
    }

    public void populateAuthorizationDetails(Connection conn, customerData custPassedData) {
        try {
            log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails BASE PLAN CHECK FROM DB :" + custPassedData.getCustid() + ":QUERY:" + strCheckBasePlanExists);
            try (PreparedStatement stmt1 = conn.prepareStatement(strCheckBasePlanExists)) {
                stmt1.setInt(1, custPassedData.getCustid());
                try (ResultSet rset1 = stmt1.executeQuery()) {
                    if (rset1.next()) {
                        boolean dynamicAttrSupport = this.checkServerConf(AAAConstant.DYNAMICATTRSUPPORT, AAAConstant.ATTRVALUEFORCHECK);
                        this.populateQuotaAndQosDetails(conn, custPassedData, dynamicAttrSupport);
                    } else {
                        log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails Base Plan Not Found");
                        custPassedData.setBasePlanExist(false);
                    }
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populateAuthorizationDetails():" + e, e);
        }
    }

    /** Loads the active plan row (quota, session time, QoS) and derives the unused quota values. */
    private void populateQuotaAndQosDetails(Connection conn, customerData custPassedData, boolean dynamicAttrSupport) throws SQLException {
        String authorizationQuery;
        if (!dynamicAttrSupport) {
            authorizationQuery = strRadiusAuthorization;
            log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails AUTHORIZATION CHECK FROM DB Without DynamicAttrSupport : " + custPassedData.getCustid() + " :QUERY: " + authorizationQuery);
        } else {
            authorizationQuery = strRadiusAuthorizationWithDynamicAttribute;
            log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails AUTHORIZATION CHECK FROM DB With DynamicAttrSupport : " + custPassedData.getCustid() + " :QUERY: " + authorizationQuery);
        }

        try (PreparedStatement stmt = conn.prepareStatement(authorizationQuery)) {
            stmt.setInt(1, custPassedData.getCustid());
            try (ResultSet rset = stmt.executeQuery()) {
                if (!rset.next()) {
                    log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails QUOTA DETAILS NOT FOUND FOR CUSTOMER:" + custPassedData);
                    return;
                }

                custPassedData.setBasePlanExist(true);

                try {
                    Integer sessioinThresholdParam = Integer.parseInt(CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.SESSION_THRESHOLD).toString());
                    Integer sessionTime = Integer.parseInt(rset.getString("sessiontime"));
                    log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails sessionTime:" + sessionTime + ",sessionThresholdParam:" + sessioinThresholdParam);
                    if (sessionTime <= sessioinThresholdParam) {
                        log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails sessionTime:" + sessionTime + ",sessionThresholdParam:" + sessioinThresholdParam + ", Session Time Set");
                        custPassedData.setMaxSessionTime(sessionTime);
                    }
                } catch (Exception e) {
                    log.info(this.PACKET_IDENTIFIER + "Error parsing maxsession time");
                }

                custPassedData.setQuotatype(rset.getString("quotatype"));
                custPassedData.setQuotaunit(rset.getString("quotaunit"));
                custPassedData.setTotalquota(rset.getString("totalquota"));
                custPassedData.setUsedquota(rset.getString("usedquota"));
                custPassedData.setTotalquotakb(rset.getString("totalquotakb"));
                custPassedData.setUsedquotakb(rset.getString("usedquotakb"));
                custPassedData.setTimequotaunit(rset.getString("timequotaunit"));
                custPassedData.setTimetotalquota(rset.getString("timetotalquota"));
                custPassedData.setTimequotaused(rset.getString("timequotaused"));
                custPassedData.setTimetotalquotasec(rset.getString("timetotalquotasec"));
                custPassedData.setTimeusedquotasec(rset.getString("timeusedquotasec"));
                custPassedData.setSpeedDowngradeFUP(rset.getBoolean("speeddowngradeflag"));

                Double unusedquota = 0.0;
                Double unusedquotakb = 0.0;
                Double unusedtime = 0.0;
                Double unusedtimesec = 0.0;
                Double totalQuota = 0.0;
                Double totalQuotaKB = 0.0;
                Double timeTotalQuota = 0.0;
                Double timeTotalQuotaSec = 0.0;
                if (rset.getString("usedquota") != null) {
                    totalQuota = Double.parseDouble(rset.getString("totalquota"));
                    if (totalQuota.equals(AAAConstant.UNLIMITED_QUOTA)) {
                        unusedquota = AAAConstant.UNLIMITED_DATA_QUOTA_GB;
                    } else {
                        unusedquota = totalQuota - Double.parseDouble(rset.getString("usedquota"));
                    }
                }

                if (rset.getString("usedquotakb") != null) {
                    totalQuotaKB = Double.parseDouble(rset.getString("totalquotakb"));
                    if (totalQuotaKB.equals(AAAConstant.UNLIMITED_QUOTA)) {
                        unusedquotakb = AAAConstant.UNLIMITED_DATA_QUOTA_KB;
                    } else {
                        unusedquotakb = totalQuotaKB - Double.parseDouble(rset.getString("usedquotakb"));
                    }
                }

                if (rset.getString("timequotaused") != null) {
                    timeTotalQuota = Double.parseDouble(rset.getString("timetotalquota"));
                    if (timeTotalQuota.equals(AAAConstant.UNLIMITED_QUOTA)) {
                        unusedtime = AAAConstant.UNLIMITED_TIME_QUOTA_HR;
                    } else {
                        unusedtime = timeTotalQuota - Double.parseDouble(rset.getString("timequotaused"));
                    }
                }

                if (rset.getString("timeusedquotasec") != null) {
                    timeTotalQuotaSec = Double.parseDouble(rset.getString("timetotalquotasec"));
                    if (timeTotalQuotaSec.equals(AAAConstant.UNLIMITED_QUOTA)) {
                        unusedtimesec = AAAConstant.UNLIMITED_TIME_QUOTA_SEC;
                    } else {
                        unusedtimesec = timeTotalQuotaSec - Double.parseDouble(rset.getString("timeusedquotasec"));
                    }
                }

                String planGroup = rset.getString("plangroup");
                String dataCategory = rset.getString("datacategory");
                boolean isFUPPlan = dataCategory != null && !"".equalsIgnoreCase(dataCategory) && "FUP".equalsIgnoreCase(dataCategory) && planGroup.equalsIgnoreCase("Renew");

                log.info("IN GET populateAuthorizationDetails isFUPPlan:" + isFUPPlan);
                boolean fupQuotaExhausted = isFUPPlan && !totalQuota.equals(AAAConstant.UNLIMITED_QUOTA) && unusedquota <= 0.0;
                if (dynamicAttrSupport) {
                    int qosPolicyId = rset.getInt("id");
                    HashMap<String, String> qosPolicyParamMap = new HashMap<>();
                    log.debug(this.PACKET_IDENTIFIER + "IN QosPolicyParam GET CHECK FROM DB :" + qosPolicyId + ":QUERY:" + qosPolicyParamQuery);
                    try (PreparedStatement stmt2 = conn.prepareStatement(qosPolicyParamQuery)) {
                        stmt2.setInt(1, qosPolicyId);
                        try (ResultSet rset2 = stmt2.executeQuery()) {
                            while (rset2.next()) {
                                if (null == rset2.getString("radius_pol_param_name")) {
                                    continue;
                                }

                                String paramName = rset2.getString("radius_pol_param_name");
                                String paramValue = fupQuotaExhausted ? rset2.getString("th_param_value") : rset2.getString("base_param_value");
                                qosPolicyParamMap.put(paramName, paramValue);
                            }
                        }
                    }

                    custPassedData.setQospolicyparam(qosPolicyParamMap);
                } else if (fupQuotaExhausted) {
                    custPassedData.setPolicyname(rset.getString("thpolicyname"));
                    custPassedData.setUploadspeed(rset.getString("thuploadspeed"));
                    custPassedData.setDownloadspeed(rset.getString("thdownloadspeed"));
                    custPassedData.setParam1(rset.getString("thparam1"));
                    custPassedData.setParam2(rset.getString("thparam2"));
                    custPassedData.setParam3(rset.getString("thparam3"));
                } else {
                    custPassedData.setPolicyname(rset.getString("basepolicyname"));
                    custPassedData.setUploadspeed(rset.getString("baseuploadspeed"));
                    custPassedData.setDownloadspeed(rset.getString("basedownloadspeed"));
                    custPassedData.setParam1(rset.getString("baseparam1"));
                    custPassedData.setParam2(rset.getString("baseparam2"));
                    custPassedData.setParam3(rset.getString("baseparam3"));
                }

                if (fupQuotaExhausted) {
                    unusedquota = AAAConstant.UNLIMITED_DATA_QUOTA_GB;
                    unusedquotakb = AAAConstant.UNLIMITED_DATA_QUOTA_KB;
                }

                custPassedData.setUnusedquota(String.valueOf(unusedquota));
                custPassedData.setUnusedquotakb(String.valueOf(unusedquotakb));
                custPassedData.setTimequotaunused(String.valueOf(unusedtime));
                custPassedData.setTimeunusedquotasec(String.valueOf(unusedtimesec));
                custPassedData.setDynamicAttrSuppport(dynamicAttrSupport);
                custPassedData.setFUPPlan(isFUPPlan);
                custPassedData.setCustPlanRelId(Integer.parseInt(rset.getString("custpackageid")));
                custPassedData.setCurrentPlanId(Integer.parseInt(rset.getString("planid")));
                if (totalQuota.equals(AAAConstant.UNLIMITED_QUOTA)) {
                    custPassedData.setQuotaUnlimited(true);
                }

                log.debug(this.PACKET_IDENTIFIER + "IN GET populateAuthorizationDetails custPassedData:" + custPassedData);
            }
        }
    }

    public void populatePoolName(customerData custPassedData) {
        try (Connection conn = DataSource.getConnection()) {
            populatePoolName(conn, custPassedData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populatePoolName():" + e, e);
        }
    }

    public void populatePoolName(Connection conn, customerData custPassedData) {
        log.debug(this.PACKET_IDENTIFIER + "IN GET populatePoolName FROM DB :" + custPassedData.getCustid() + ":QUERY:" + strPoolNameFromPoolId);

        try (PreparedStatement stmt = conn.prepareStatement(strPoolNameFromPoolId)) {
            stmt.setInt(1, custPassedData.getPoolId());
            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    custPassedData.setPoolname(rset.getString("pool_name"));
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populatePoolName():" + e, e);
        }
    }

    public void populateStaticIP(customerData custPassedData) {
        try (Connection conn = DataSource.getConnection()) {
            populateStaticIP(conn, custPassedData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populateStaticIP():" + e, e);
        }
    }

    public void populateStaticIP(Connection conn, customerData custPassedData) {
        log.debug(this.PACKET_IDENTIFIER + "IN GET populateStaticIP FROM DB :" + custPassedData.getCustid() + " :QUERY: " + strStaticIPQueryNew);

        try (PreparedStatement stmt = conn.prepareStatement(strStaticIPQueryNew)) {
            stmt.setInt(1, custPassedData.getCustid());
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    custPassedData.setIpaddress(rset.getString("ip_address"));
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populateStaticIP():" + e, e);
        }
    }

    public customerData getDBCustomer(String strUsername) throws Exception {
        try (Connection conn = DataSource.getConnection()) {
            return getDBCustomer(conn, strUsername);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in GETCUSTOMER FROM DB :" + e, e);
            return null;
        }
    }

    public customerData getDBCustomer(Connection conn, String strUsername) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN GET CUSTOMER FROM DB :" + strUsername + ":QUERY:" + strRadiusCustomerQuery);
        customerData custDBData = null;

        try (PreparedStatement stmt = conn.prepareStatement(strRadiusCustomerQuery)) {
            stmt.setString(1, strUsername.trim());
            try (ResultSet rset = stmt.executeQuery()) {
                // every row builds a fresh customerData, so the last row wins
                while (rset.next()) {
                    custDBData = new customerData();
                    custDBData.setUsername(rset.getString("username"));
                    custDBData.setPassword(rset.getString("password"));
                    custDBData.setCustid(rset.getInt("custid"));
                    custDBData.setStatus(rset.getString("cstatus"));
                    custDBData.setLastlogintime(rset.getTimestamp("last_login_time"));
                    custDBData.setLastpasswordupdate(rset.getTimestamp("last_password_change"));
                    custDBData.setNetworktype(rset.getString("networktype"));
                    custDBData.setPartnerId(rset.getInt("partnerid"));
                    custDBData.setPartnerName(rset.getString("partnername"));
                    custDBData.setServicetype(rset.getString("servicetype"));
                    custDBData.setPasswordsaveflag(rset.getBoolean("setpasswordfromradiusflag"));
                    custDBData.setMactelflag(rset.getBoolean("mactelflag"));
                    custDBData.setVlanEnable(rset.getBoolean("vlanflag"));
                    if (rset.getString("servicearea_id") != null) {
                        custDBData.setServiceareaid(Integer.parseInt(rset.getString("servicearea_id")));
                    }

                    if (rset.getString("defaultpoolid") != null) {
                        custDBData.setPoolId(Integer.parseInt(rset.getString("defaultpoolid")));
                    }
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in GETCUSTOMER FROM DB :" + e, e);
        }

        return custDBData;
    }

    private void updateCustomer(customerData custDBData, int type) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Update CUSTOMER FROM DB :" + custDBData.toString() + "QUERY" + strRadiusCustomerUpdateQuery);

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strRadiusCustomerUpdateQuery)) {
            if (type == 1) {
                stmt.setInt(1, 0);
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            } else if (type == 2) {
                stmt.setInt(1, custDBData.getFailcount());
                stmt.setTimestamp(2, custDBData.getLastlogintime());
            } else {
                stmt.setInt(1, custDBData.getFailcount() + 1);
                stmt.setTimestamp(2, custDBData.getLastlogintime());
            }

            stmt.setString(3, custDBData.getUsername());
            int updated = stmt.executeUpdate();
            log.debug(this.PACKET_IDENTIFIER + "Result is " + updated);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in Update CUSTOMER FROM DB:" + e, e);
        }
    }

    public void updateQuota(Double totalActual, long totalActualKb, long totalActualTime, long sessiontime, int custid, String strQuotaUnit, String strQuotaUnitTime, long custpackageid, boolean bBWDowndgraded) throws Exception {
        try (Connection conn = DataSource.getConnection()) {
            updateQuota(conn, totalActual, totalActualKb, totalActualTime, sessiontime, custid, strQuotaUnit, strQuotaUnitTime, custpackageid, bBWDowndgraded);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in UpdateQuota():" + e, e);
        }
    }

    public void updateQuota(Connection conn, Double totalActual, long totalActualKb, long totalActualTime, long sessiontime, int custid, String strQuotaUnit, String strQuotaUnitTime, long custpackageid, boolean bBWDowndgraded) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Update updateQuota FROM DB :" + custid + ",custpackageid:" + custpackageid);

        try {
            String updateQuery = bBWDowndgraded ? strRadiusUpdateQuotaWithBWFlag : strRadiusUpdateQuota;
            log.debug(this.PACKET_IDENTIFIER + "IN Update updateQuota FROM DB :" + custid + ",custpackageid:" + custpackageid + " QUERY" + updateQuery);

            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                // usage is only added for the units the plan actually has; the other counters are written as 0
                stmt.setDouble(1, strQuotaUnit != null ? totalActual : 0.0);
                stmt.setLong(2, strQuotaUnit != null ? totalActualKb : 0L);
                stmt.setLong(3, strQuotaUnitTime != null ? totalActualTime : 0L);
                if (bBWDowndgraded) {
                    stmt.setLong(4, sessiontime);
                    stmt.setBoolean(5, bBWDowndgraded);
                    stmt.setInt(6, custid);
                    stmt.setLong(7, custpackageid);
                } else {
                    stmt.setLong(4, strQuotaUnitTime != null ? sessiontime : 0L);
                    stmt.setInt(5, custid);
                    stmt.setLong(6, custpackageid);
                }

                int intReturn = stmt.executeUpdate();
                log.debug(this.PACKET_IDENTIFIER + "Result is " + intReturn);
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in UpdateQuota():" + e, e);
        }
    }

    public boolean insertAuthResponse(HashMap<String, String> hsAuthRes) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Insert Auth Response in DB :" + hsAuthRes + "QUERY" + strRadiusAuthenticationResponse);

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strRadiusAuthenticationResponse)) {
            stmt.setString(1, hsAuthRes.get("username").toString());
            stmt.setString(2, hsAuthRes.get("replymessage"));
            stmt.setString(3, hsAuthRes.get("packettype"));
            stmt.setString(4, hsAuthRes.get(AAAConstant.CLIENTIP));
            stmt.setString(5, hsAuthRes.get("clientgroup"));
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in Insert Auth Response in DB :" + e, e);
        }

        return true;
    }

    public boolean findCustMacMapping(int custid) {
        try (Connection conn = DataSource.getConnection()) {
            return findCustMacMapping(conn, custid);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in find CUST MAC mapping in DB :" + e, e);
            return false;
        }
    }

    public boolean findCustMacMapping(Connection conn, int custid) {
        log.debug(this.PACKET_IDENTIFIER + "IN find CUST MAC mapping in DB :" + custid);

        try (PreparedStatement stmt = conn.prepareStatement(strFindCustMacMappingQuery)) {
            stmt.setInt(1, custid);
            try (ResultSet rset = stmt.executeQuery()) {
                return rset.next();
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in find CUST MAC mapping in DB :" + e, e);
            return false;
        }
    }

    public boolean findVlanMapping(int custid) {
        try (Connection conn = DataSource.getConnection()) {
            return findVlanMapping(conn, custid);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in find CUST MAC mapping in DB :" + e, e);
            return false;
        }
    }

    public boolean findVlanMapping(Connection conn, int custid) {
        log.debug(this.PACKET_IDENTIFIER + "IN find CUST Vlan mapping in DB :" + custid);

        try (PreparedStatement stmt = conn.prepareStatement(strFindVlanCustMappingQuery)) {
            stmt.setInt(1, custid);
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    return null != rset.getString("VLANID") && rset.getString("VLANID").length() > 0;
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in find CUST MAC mapping in DB :" + e, e);
        }

        return false;
    }

    public int findMacCustMapping(String macAddress) {
        try (Connection conn = DataSource.getConnection()) {
            return findMacCustMapping(conn, macAddress);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN find CUST MAC mapping in DB :" + e, e);
            return 0;
        }
    }

    public int findMacCustMapping(Connection conn, String macAddress) {
        log.debug(this.PACKET_IDENTIFIER + "IN find MAC CUST mapping in DB :" + macAddress);

        try (PreparedStatement stmt = conn.prepareStatement(strFindMacCustMappingQuery)) {
            stmt.setString(1, macAddress);
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    return Integer.parseInt(rset.getString("custid"));
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN find CUST MAC mapping in DB :" + e, e);
        }

        return 0;
    }

    public void setCustMacAddress(int custid, String macAddress) {
        try (Connection conn = DataSource.getConnection()) {
            setCustMacAddress(conn, custid, macAddress);
        } catch (SQLException e) {
            log.debug(this.PACKET_IDENTIFIER + "Error IN Insert Customer MAC Mapping in DB :" + e, e);
        }
    }

    public void setCustMacAddress(Connection conn, int custid, String macAddress) {
        log.debug(this.PACKET_IDENTIFIER + "IN Insert Customer MAC Mapping in DB :" + custid + "-" + macAddress + "QUERY" + strAddCustMacMapping);

        try (PreparedStatement stmt = conn.prepareStatement(strAddCustMacMapping)) {
            stmt.setInt(1, custid);
            stmt.setString(2, macAddress);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.debug(this.PACKET_IDENTIFIER + "Error IN Insert Customer MAC Mapping in DB :" + e, e);
        }
    }

    public void setCustVlan(int custid, String vlanId) {
        try (Connection conn = DataSource.getConnection()) {
            setCustVlan(conn, custid, vlanId);
        } catch (SQLException e) {
            log.debug(this.PACKET_IDENTIFIER + "Error IN Update Customer Vlan in DB :" + e, e);
        }
    }

    public void setCustVlan(Connection conn, int custid, String vlanId) {
        log.debug(this.PACKET_IDENTIFIER + "IN Update Customer Vlan Mapping in DB :" + custid + "-" + vlanId + "QUERY" + strAddCustVlanMapping);

        try (PreparedStatement stmt = conn.prepareStatement(strAddCustVlanMapping)) {
            stmt.setString(1, vlanId);
            stmt.setInt(2, custid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.debug(this.PACKET_IDENTIFIER + "Error IN Update Customer Vlan in DB :" + e, e);
        }
    }

    public customerData getCustomerEntryFromCustid(int custid) {
        try (Connection conn = DataSource.getConnection()) {
            return getCustomerEntryFromCustid(conn, custid);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET CUSTOMER FROM CUST ID IN DB :" + e, e);
            return null;
        }
    }

    public customerData getCustomerEntryFromCustid(Connection conn, int custid) {
        log.debug(this.PACKET_IDENTIFIER + "IN GET CUSTOMER FROM CUST ID IN DB :" + custid + ":QUERY:" + strRadiusFindCustomerCustidQuery);
        customerData custDBData = null;

        try (PreparedStatement stmt = conn.prepareStatement(strRadiusFindCustomerCustidQuery)) {
            stmt.setInt(1, custid);
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    custDBData = new customerData();
                    custDBData.setUsername(rset.getString("username"));
                    custDBData.setPassword(rset.getString("password"));
                    custDBData.setCustid(rset.getInt("custid"));
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET CUSTOMER FROM CUST ID IN DB :" + e, e);
        }

        return custDBData;
    }

    public boolean isMaxSessionsBreached(int custid) {
        log.debug(this.PACKET_IDENTIFIER + "IN isMaxSessionsBreached :" + custid + "QUERY:" + strCurrentLiveSessionCount);
        boolean maxSessionBreached = false;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strCurrentLiveSessionCount)) {
            stmt.setInt(1, custid);
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    int currentLiveUsers = rset.getInt("livesessioncount");
                    if (currentLiveUsers > 0) {
                        log.debug(this.PACKET_IDENTIFIER + "IN isMaxSessionsBreached :" + custid + "QUERY:" + strGetCustomerActivePlanMaxSession);
                        try (PreparedStatement stmt1 = conn.prepareStatement(strGetCustomerActivePlanMaxSession)) {
                            stmt1.setInt(1, custid);
                            try (ResultSet rset1 = stmt1.executeQuery()) {
                                log.debug(this.PACKET_IDENTIFIER + "IN isMaxSessionsBreached rset1:" + rset1.getFetchSize());
                                if (rset1.next()) {
                                    int planMaxSessions = Integer.parseInt(rset1.getString("maxconcurrentsession"));
                                    log.debug(this.PACKET_IDENTIFIER + "IN isMaxSessionsBreached, livesessioncount :" + currentLiveUsers + ",planMaxSessions:" + planMaxSessions);
                                    if (currentLiveUsers >= planMaxSessions) {
                                        maxSessionBreached = true;
                                        log.info(this.PACKET_IDENTIFIER + "IN isMaxSessionsBreched Max Session Exceeded, custid:" + custid + ",maxconsessions:" + planMaxSessions + ",livesessioncount:" + currentLiveUsers);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            maxSessionBreached = false;
            log.error(this.PACKET_IDENTIFIER + "Error IN isMaxSessionsBreached:" + e, e);
        }

        return maxSessionBreached;
    }

    public customerData getCustCurrentPlanDetails(String userName) {
        return this.loadActivePlanDetails(userName, "getCustCurrentPlanDetails", strGetActivePlanDetails, strGetActivePlanDetailsWithDynamicAttribute,
                "Error IN GET getCustCurrentPlanDetails():");
    }

    /**
     * Shared body of getCustCurrentPlanDetails / getCustLastActivePlanDetails: they differ only in the queries and
     * in two log texts. Returns null when the customer has no such plan or anything fails.
     */
    private customerData loadActivePlanDetails(String userName, String caller, String planQuery, String planQueryWithDynamicAttribute, String sessionTimeErrorMessage) {
        customerData custPassedData = null;

        try (Connection conn = DataSource.getConnection()) {
            boolean dynamicAttrSupport = this.checkServerConf(AAAConstant.DYNAMICATTRSUPPORT, AAAConstant.ATTRVALUEFORCHECK);
            String query;
            if (!dynamicAttrSupport) {
                query = planQuery;
                log.debug(this.PACKET_IDENTIFIER + "IN GET " + caller + " AUTHORIZATION CHECK FROM DB Without DynamicAttrSupport : " + userName + " :QUERY: " + query);
            } else {
                query = planQueryWithDynamicAttribute;
                log.debug(this.PACKET_IDENTIFIER + "IN GET " + caller + " AUTHORIZATION CHECK FROM DB With DynamicAttrSupport : " + userName + " :QUERY: " + query);
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userName);
                try (ResultSet rset = stmt.executeQuery()) {
                    if (!rset.next()) {
                        log.debug(this.PACKET_IDENTIFIER + "IN GET " + caller + " QUOTA DETAILS NOT FOUND FOR CUSTOMER:" + custPassedData);
                        return custPassedData;
                    }

                    custPassedData = new customerData();

                    try {
                        Integer sessioinThresholdParam = Integer.parseInt(CachingConfigurations.getCachedValue(AAAConstant.SERVERCACHING, AAAConstant.SESSION_THRESHOLD).toString());
                        Integer sessionTime = Integer.parseInt(rset.getString("sessiontime"));
                        log.debug(this.PACKET_IDENTIFIER + "IN GET " + caller + " sessionTime:" + sessionTime + ",sessionThresholdParam:" + sessioinThresholdParam);
                        if (sessionTime <= sessioinThresholdParam) {
                            log.debug(this.PACKET_IDENTIFIER + "IN GET " + caller + " sessionTime:" + sessionTime + ",sessionThresholdParam:" + sessioinThresholdParam + ", Session Time Set");
                            custPassedData.setMaxSessionTime(sessionTime);
                        }
                    } catch (Exception e) {
                        log.error(this.PACKET_IDENTIFIER + sessionTimeErrorMessage + e, e);
                    }

                    custPassedData.setQuotatype(rset.getString("quotatype"));
                    custPassedData.setQuotaunit(rset.getString("quotaunit"));
                    custPassedData.setTotalquota(rset.getString("totalquota"));
                    custPassedData.setUsedquota(rset.getString("usedquota"));
                    custPassedData.setTotalquotakb(rset.getString("totalquotakb"));
                    custPassedData.setUsedquotakb(rset.getString("usedquotakb"));
                    custPassedData.setTimequotaunit(rset.getString("timequotaunit"));
                    custPassedData.setTimetotalquota(rset.getString("timetotalquota"));
                    custPassedData.setTimequotaused(rset.getString("timequotaused"));
                    custPassedData.setTimetotalquotasec(rset.getString("timetotalquotasec"));
                    custPassedData.setTimeusedquotasec(rset.getString("timeusedquotasec"));

                    Double unusedquota = 0.0;
                    Double unusedquotakb = 0.0;
                    Double unusedtime = 0.0;
                    Double unusedtimesec = 0.0;
                    Double totalQuota = 0.0;
                    Double totalQuotaKB = 0.0;
                    Double timeTotalQuota = 0.0;
                    Double timeTotalQuotaSec = 0.0;
                    if (rset.getString("usedquota") != null) {
                        totalQuota = Double.parseDouble(rset.getString("totalquota"));
                        if (totalQuota.equals(AAAConstant.UNLIMITED_QUOTA)) {
                            unusedquota = AAAConstant.UNLIMITED_DATA_QUOTA_GB;
                        } else {
                            unusedquota = totalQuota - Double.parseDouble(rset.getString("usedquota"));
                        }
                    }

                    if (rset.getString("usedquotakb") != null) {
                        totalQuotaKB = Double.parseDouble(rset.getString("totalquotakb"));
                        if (totalQuotaKB.equals(AAAConstant.UNLIMITED_QUOTA)) {
                            unusedquotakb = AAAConstant.UNLIMITED_DATA_QUOTA_KB;
                        } else {
                            unusedquotakb = totalQuotaKB - Double.parseDouble(rset.getString("usedquotakb"));
                        }
                    }

                    if (rset.getString("timequotaused") != null) {
                        timeTotalQuota = Double.parseDouble(rset.getString("timetotalquota"));
                        if (timeTotalQuota.equals(AAAConstant.UNLIMITED_QUOTA)) {
                            unusedtime = AAAConstant.UNLIMITED_TIME_QUOTA_HR;
                        } else {
                            unusedtime = timeTotalQuota - Double.parseDouble(rset.getString("timequotaused"));
                        }
                    }

                    if (rset.getString("timeusedquotasec") != null) {
                        timeTotalQuotaSec = Double.parseDouble(rset.getString("timetotalquotasec"));
                        if (timeTotalQuotaSec.equals(AAAConstant.UNLIMITED_QUOTA)) {
                            unusedtimesec = AAAConstant.UNLIMITED_TIME_QUOTA_SEC;
                        } else {
                            unusedtimesec = timeTotalQuotaSec - Double.parseDouble(rset.getString("timeusedquotasec"));
                        }
                    }

                    String planGroup = rset.getString("plangroup");
                    String dataCategory = rset.getString("datacategory");
                    boolean isFUPPlan = dataCategory != null && !"".equalsIgnoreCase(dataCategory) && "FUP".equalsIgnoreCase(dataCategory) && planGroup.equalsIgnoreCase("Renew");

                    // (the last-active variant has always logged this line under the "current plan" name)
                    log.info("IN GET getCustCurrentPlanDetails isFUPPlan:" + isFUPPlan);
                    boolean fupQuotaExhausted = isFUPPlan && !totalQuota.equals(AAAConstant.UNLIMITED_QUOTA) && unusedquota <= 0.0;
                    if (dynamicAttrSupport) {
                        int qosPolicyId = rset.getInt("id");
                        HashMap<String, String> qosPolicyParamMap = new HashMap<>();
                        log.debug(this.PACKET_IDENTIFIER + "IN " + caller + ", QosPolicyParam GET CHECK FROM DB :" + qosPolicyId + ":QUERY:" + qosPolicyParamQuery);
                        try (PreparedStatement stmt2 = conn.prepareStatement(qosPolicyParamQuery)) {
                            stmt2.setInt(1, qosPolicyId);
                            try (ResultSet rset2 = stmt2.executeQuery()) {
                                while (rset2.next()) {
                                    if (null == rset2.getString("radius_pol_param_name")) {
                                        continue;
                                    }

                                    String paramName = rset2.getString("radius_pol_param_name");
                                    String paramValue = fupQuotaExhausted ? rset2.getString("th_param_value") : rset2.getString("base_param_value");
                                    qosPolicyParamMap.put(paramName, paramValue);
                                }
                            }
                        }

                        custPassedData.setQospolicyparam(qosPolicyParamMap);
                    } else if (fupQuotaExhausted) {
                        custPassedData.setPolicyname(rset.getString("thpolicyname"));
                        custPassedData.setUploadspeed(rset.getString("thuploadspeed"));
                        custPassedData.setDownloadspeed(rset.getString("thdownloadspeed"));
                        custPassedData.setParam1(rset.getString("thparam1"));
                        custPassedData.setParam2(rset.getString("thparam2"));
                        custPassedData.setParam3(rset.getString("thparam3"));
                    } else {
                        custPassedData.setPolicyname(rset.getString("basepolicyname"));
                        custPassedData.setUploadspeed(rset.getString("baseuploadspeed"));
                        custPassedData.setDownloadspeed(rset.getString("basedownloadspeed"));
                        custPassedData.setParam1(rset.getString("baseparam1"));
                        custPassedData.setParam2(rset.getString("baseparam2"));
                        custPassedData.setParam3(rset.getString("baseparam3"));
                    }

                    custPassedData.setUnusedquota(String.valueOf(unusedquota));
                    custPassedData.setUnusedquotakb(String.valueOf(unusedquotakb));
                    custPassedData.setTimequotaunused(String.valueOf(unusedtime));
                    custPassedData.setTimeunusedquotasec(String.valueOf(unusedtimesec));
                    custPassedData.setDynamicAttrSuppport(dynamicAttrSupport);
                    custPassedData.setFUPPlan(isFUPPlan);
                    custPassedData.setCustPlanRelId(Integer.parseInt(rset.getString("custpackageid")));
                    custPassedData.setCurrentPlanId(Integer.parseInt(rset.getString("planid")));
                    log.debug(this.PACKET_IDENTIFIER + "IN GET " + caller + " custPassedData:" + custPassedData);
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "IN GET " + caller + " error:" + e, e);
            custPassedData = null;
        }

        return custPassedData;
    }

    public customerData getCustLastActivePlanDetails(String userName) {
        return this.loadActivePlanDetails(userName, "getCustLastActivePlanDetails", strGetLastActivePlanDetails, strGetLastActivePlanDetailsWithDynamicAttribute,
                "IN GET getCustLastActivePlanDetails()");
    }

    public void populateRadiusProfiles(customerData custPassedData) {
        try (Connection conn = DataSource.getConnection()) {
            populateRadiusProfiles(conn, custPassedData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET populateRadiusProfiles()" + e, e);
        }
    }

    public void populateRadiusProfiles(Connection conn, customerData custPassedData) {
        log.debug(this.PACKET_IDENTIFIER + "IN GET populateRadiusProfiles FROM DB :" + custPassedData.getCustid() + ":QUERY:" + strCustomePackageRadiusPolicyQuery);

        try (PreparedStatement stmt = conn.prepareStatement(strCustomePackageRadiusPolicyQuery)) {
            stmt.setInt(1, custPassedData.getCustPlanRelId());
            try (ResultSet rset = stmt.executeQuery()) {
                List<Integer> radiusProfiles = custPassedData.getRadiusprofile() == null ? new ArrayList<>() : custPassedData.getRadiusprofile();

                while (rset.next()) {
                    radiusProfiles.add(rset.getInt("radiusprofileid"));
                }

                custPassedData.setRadiusprofile(radiusProfiles);
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN GET populateRadiusProfiles()" + e, e);
        }
    }

    public void populateServiceAreaDetails(customerData customerData) {
        try (Connection conn = DataSource.getConnection()) {
            populateServiceAreaDetails(conn, customerData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populateServiceAreaDetails FROM DB :" + e, e);
        }
    }

    public void populateServiceAreaDetails(Connection conn, customerData customerData) {
        log.debug(this.PACKET_IDENTIFIER + "IN GET populateServiceAreaDetails FROM DB :" + customerData.getCustid() + ",servicearea:" + customerData.getServiceareaid() + ",QUERY:" + strCustomerServiceAreaDetialsQuery);

        try (PreparedStatement stmt = conn.prepareStatement(strCustomerServiceAreaDetialsQuery)) {
            stmt.setInt(1, customerData.getServiceareaid());
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next() && null != rset.getString("vlan_id")) {
                    customerData.setVLANID(rset.getString("vlan_id"));
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in populateServiceAreaDetails FROM DB :" + e, e);
        }
    }

    public int findCustIPMapping(String ipAddress) {
        try (Connection conn = DataSource.getConnection()) {
            return findCustIPMapping(conn, ipAddress);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN findCustIPMapping in DB :" + e, e);
            return 0;
        }
    }

    public int findCustIPMapping(Connection conn, String ipAddress) {
        log.debug(this.PACKET_IDENTIFIER + "IN findCustIPMapping in DB :" + ipAddress);

        try (PreparedStatement stmt = conn.prepareStatement(strCustIPQuery)) {
            stmt.setString(1, ipAddress);
            try (ResultSet rset = stmt.executeQuery()) {
                if (!rset.next()) {
                    return 0;
                }

                return Integer.parseInt(rset.getString("custid"));
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN findCustIPMapping in DB :" + e, e);
            return 0;
        }
    }

    public CustomerAttribute getCustomerAttribute(int custId, String attributeName) {
        log.debug("{} IN getCustomerAttribute in DB {}:{} QUERY {} ", new Object[]{this.PACKET_IDENTIFIER, custId, attributeName, strGetCustomerAttribute});
        CustomerAttribute custAttrib = null;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strGetCustomerAttribute)) {
            stmt.setInt(1, custId);
            stmt.setString(2, attributeName);
            try (ResultSet rset = stmt.executeQuery()) {
                if (rset.next()) {
                    custAttrib = new CustomerAttribute();
                    custAttrib.setCustattrid(rset.getInt("custattrid"));
                    custAttrib.setAttributeName(rset.getString("attribname"));
                    custAttrib.setAttributeValue(rset.getString("attribvalue"));
                    custAttrib.setCustid(rset.getInt("custid"));
                }
            }
        } catch (SQLException e) {
            custAttrib = null;
            log.error("{} Error IN getCustomerAttribute in DB :{} {}", new Object[]{this.PACKET_IDENTIFIER, e.getMessage(), e});
        }

        return custAttrib;
    }

    public void updateCustomerArribute(CustomerAttribute custAttrib) {
        log.debug("{} IN updateCustomerArribute in DB {}:{} ", new Object[]{this.PACKET_IDENTIFIER, strCustAttribUpdate, custAttrib});

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strCustAttribUpdate)) {
            stmt.setString(1, custAttrib.getAttributeValue());
            stmt.setInt(2, custAttrib.getCustattrid());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("{} Error IN updateCustomerArribute in DB :{} {}", new Object[]{this.PACKET_IDENTIFIER, e.getMessage(), e});
        }
    }

    public void insertCustomerArribute(CustomerAttribute custAttrib) {
        log.debug("{} IN insertCustomerArribute in DB {}:{} ", strCustAttribInsert, custAttrib);

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strCustAttribInsert)) {
            stmt.setInt(1, custAttrib.getCustid());
            stmt.setString(2, custAttrib.getAttributeName());
            stmt.setString(3, custAttrib.getAttributeValue());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("{} Error IN insertCustomerArribute in DB :{} {}", new Object[]{this.PACKET_IDENTIFIER, e.getMessage(), e});
        }
    }
}
