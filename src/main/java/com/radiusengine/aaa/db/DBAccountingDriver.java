//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.aaa.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.CDRData;
import com.radiusengine.aaa.data.customerData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccountingRequest;

public class DBAccountingDriver {
    private String PACKET_IDENTIFIER = "[DBAccountingDriver]";
    private static String strRadiusAccountSelect = "select CDRID,USER_NAME,NAS_IP_ADDRESS,NAS_PORT,SERVICE_TYPE,FRAMED_PROTOCOL,FRAMED_IP_ADDRESS,FRAMED_IP_NETMASK,FRAMED_ROUTING,FILTER_ID,FRAMED_MTU,FRAMED_COMPRESSION,LOGIN_IP_HOST,LOGIN_SERVICE,LOGIN_TCP_PORT,CALLBACK_NUMBER,CALLBACK_ID,FRAMED_ROUTE,FRAMED_IPX_NETWORK,CLASSRAD,VENDOR_SPECIFIC,SESSION_TIMEOUT,IDLE_TIMEOUT,TERMINATION_ACTION,CALLED_STATION_ID,CALLING_STATION_ID,NAS_IDENTIFIER,PROXY_STATE,LOGIN_LAT_SERVICE,LOGIN_LAT_NODE,LOGIN_LAT_GROUP,FRAMED_APPLETALK_LINK,FRAMED_APPLETALK_NETWORK,FRAMED_APPLETALK_ZONE,ACCT_STATUS_TYPE,ACCT_DELAY_TIME,ACCT_INPUT_OCTETS,ACCT_OUTPUT_OCTETS,ACCT_SESSION_ID,ACCT_AUTHENTIC,ACCT_SESSION_TIME,ACCT_INPUT_PACKETS,ACCT_OUTPUT_PACKETS,ACCT_TERMINATE_CAUSE,ACCT_MULTI_SESSION_ID,ACCT_LINK_COUNT,NAS_PORT_TYPE,PORT_LIMIT,LOGIN_LAT_PORT,ACCT_INPUT_GIGAWORDS,ACCT_OUTPUT_GIGAWORDS,EVENT_TIMESTAMP,NAS_PORT_ID,CONNECT_INFO,GROUPNAME,PARAM_STR1,PARAM_STR2,PARAM_STR3,PARAM_STR4,PARAM_STR5,PARAM_INT1,PARAM_INT2,PARAM_INT3,PARAM_INT4,PARAM_INT5,PARAM_DATE0,PARAM_DATE1,PARAM_DATE2,CALL_START,CALL_END,CREATE_DATE,LAST_MODIFIED_DATE from tblacctcdr where ACCT_SESSION_ID=?";
    private static String strRadiusAccountDelete = "delete from tblacctcdr where ACCTSESSIONID=?";
    private static String strRadiusAccountInsert = "insert into $ACCT_CDR_TABLE_NAME (UserName,NASIPAddress,NASPort,ServiceType,FramedProtocol,FramedIPAddress,FramedIPNetmask,FramedRouting,FilterId,FramedMTU,FramedCompression,LoginIPHost,LoginService,LoginTCPPort,ReplyMessage,CallbackNumber,CallbackId,FramedRoute,FramedIPXNetwork,State,Class,VendorSpecific,SessionTimeout,IdleTimeout,TerminationAction,CalledStationId,CallingStationId,NASIdentifier,ProxyState,LoginLATService,LoginLATNode,LoginLATGroup,FramedAppleTalkLink,FramedAppleTalkNetwork,FramedAppleTalkZone,AcctStatusType,AcctDelayTime,AcctInputOctets,AcctOutputOctets,AcctSessionId,AcctAuthentic,AcctSessionTime,AcctInputPackets,AcctOutputPackets,AcctTerminateCause,AcctMultiSessionId,AcctLinkCount,AcctInputGigawords,AcctOutputGigawords,EventTimestamp,CHAPChallenge,NASPortType,PortLimit,LoginLATPort,AcctTunnelConnection,ARAPFeatures,ARAPZoneAccess,ARAPSecurity,ARAPSecurityData,PasswordRetry,Prompt,ConnectInfo,ConfigurationToken,EAPMessage,MessageAuthenticator,ARAPChallengeResponse,AcctInterimInterval,NASPortId,FramedPool,NASIPv6Address,FramedInterfaceId,FramedIPv6Prefix,LoginIPv6Host,FramedIPv6Route,FramedIPv6Pool,DigestResponse,DigestAttributes,jsontext,DelegatedIPv6Prefix,FramedIPv6Address,DNSServerIPv6Address,RouteIPv6Information,DelegatedIPv6PrefixPool,StatefulIPv6AddressPool) values(:UserName,:NASIPAddress,:NASPort,:ServiceType,:FramedProtocol,:FramedIPAddress,:FramedIPNetmask,:FramedRouting,:FilterId,:FramedMTU,:FramedCompression,:LoginIPHost,:LoginService,:LoginTCPPort,:ReplyMessage,:CallbackNumber,:CallbackId,:FramedRoute,:FramedIPXNetwork,:State,:Class,:VendorSpecific,:SessionTimeout,:IdleTimeout,:TerminationAction,:CalledStationId,:CallingStationId,:NASIdentifier,:ProxyState,:LoginLATService,:LoginLATNode,:LoginLATGroup,:FramedAppleTalkLink,:FramedAppleTalkNetwork,:FramedAppleTalkZone,:AcctStatusType,:AcctDelayTime,:AcctInputOctets,:AcctOutputOctets,:AcctSessionId,:AcctAuthentic,:AcctSessionTime,:AcctInputPackets,:AcctOutputPackets,:AcctTerminateCause,:AcctMultiSessionId,:AcctLinkCount,:AcctInputGigawords,:AcctOutputGigawords,:EventTimestamp,:CHAPChallenge,:NASPortType,:PortLimit,:LoginLATPort,:AcctTunnelConnection,:ARAPFeatures,:ARAPZoneAccess,:ARAPSecurity,:ARAPSecurityData,:PasswordRetry,:Prompt,:ConnectInfo,:ConfigurationToken,:EAPMessage,:MessageAuthenticator,:ARAPChallengeResponse,:AcctInterimInterval,:NASPortId,:FramedPool,:NASIPv6Address,:FramedInterfaceId,:FramedIPv6Prefix,:LoginIPv6Host,:FramedIPv6Route,:FramedIPv6Pool,:DigestResponse,:DigestAttributes,:jsontext,:DelegatedIPv6Prefix,:FramedIPv6Address,:DNSServerIPv6Address,:RouteIPv6Information,:DelegatedIPv6PrefixPool,:StatefulIPv6AddressPool)";
    private static String strRadiusAccountInsertWithCreateDate = "insert into $ACCT_CDR_TABLE_NAME (UserName,NASIPAddress,NASPort,ServiceType,FramedProtocol,FramedIPAddress,FramedIPNetmask,FramedRouting,FilterId,FramedMTU,FramedCompression,LoginIPHost,LoginService,LoginTCPPort,ReplyMessage,CallbackNumber,CallbackId,FramedRoute,FramedIPXNetwork,State,Class,VendorSpecific,SessionTimeout,IdleTimeout,TerminationAction,CalledStationId,CallingStationId,NASIdentifier,ProxyState,LoginLATService,LoginLATNode,LoginLATGroup,FramedAppleTalkLink,FramedAppleTalkNetwork,FramedAppleTalkZone,AcctStatusType,AcctDelayTime,AcctInputOctets,AcctOutputOctets,AcctSessionId,AcctAuthentic,AcctSessionTime,AcctInputPackets,AcctOutputPackets,AcctTerminateCause,AcctMultiSessionId,AcctLinkCount,AcctInputGigawords,AcctOutputGigawords,EventTimestamp,CHAPChallenge,NASPortType,PortLimit,LoginLATPort,AcctTunnelConnection,ARAPFeatures,ARAPZoneAccess,ARAPSecurity,ARAPSecurityData,PasswordRetry,Prompt,ConnectInfo,ConfigurationToken,EAPMessage,MessageAuthenticator,ARAPChallengeResponse,AcctInterimInterval,NASPortId,FramedPool,NASIPv6Address,FramedInterfaceId,FramedIPv6Prefix,LoginIPv6Host,FramedIPv6Route,FramedIPv6Pool,DigestResponse,DigestAttributes,jsontext,CREATE_DATE,DelegatedIPv6Prefix,FramedIPv6Address,DNSServerIPv6Address,RouteIPv6Information,DelegatedIPv6PrefixPool,StatefulIPv6AddressPool) values(:UserName,:NASIPAddress,:NASPort,:ServiceType,:FramedProtocol,:FramedIPAddress,:FramedIPNetmask,:FramedRouting,:FilterId,:FramedMTU,:FramedCompression,:LoginIPHost,:LoginService,:LoginTCPPort,:ReplyMessage,:CallbackNumber,:CallbackId,:FramedRoute,:FramedIPXNetwork,:State,:Class,:VendorSpecific,:SessionTimeout,:IdleTimeout,:TerminationAction,:CalledStationId,:CallingStationId,:NASIdentifier,:ProxyState,:LoginLATService,:LoginLATNode,:LoginLATGroup,:FramedAppleTalkLink,:FramedAppleTalkNetwork,:FramedAppleTalkZone,:AcctStatusType,:AcctDelayTime,:AcctInputOctets,:AcctOutputOctets,:AcctSessionId,:AcctAuthentic,:AcctSessionTime,:AcctInputPackets,:AcctOutputPackets,:AcctTerminateCause,:AcctMultiSessionId,:AcctLinkCount,:AcctInputGigawords,:AcctOutputGigawords,:EventTimestamp,:CHAPChallenge,:NASPortType,:PortLimit,:LoginLATPort,:AcctTunnelConnection,:ARAPFeatures,:ARAPZoneAccess,:ARAPSecurity,:ARAPSecurityData,:PasswordRetry,:Prompt,:ConnectInfo,:ConfigurationToken,:EAPMessage,:MessageAuthenticator,:ARAPChallengeResponse,:AcctInterimInterval,:NASPortId,:FramedPool,:NASIPv6Address,:FramedInterfaceId,:FramedIPv6Prefix,:LoginIPv6Host,:FramedIPv6Route,:FramedIPv6Pool,:DigestResponse,:DigestAttributes,:jsontext,:createdate,:DelegatedIPv6Prefix,:FramedIPv6Address,:DNSServerIPv6Address,:RouteIPv6Information,:DelegatedIPv6PrefixPool,:StatefulIPv6AddressPool)";
    private static String strLiveSessionSelect = "select UserName,NASIPAddress,NASPort,ServiceType,FramedProtocol,FramedIPAddress,FramedIPNetmask,FramedRouting,FilterId,FramedMTU,FramedCompression,LoginIPHost,LoginService,LoginTCPPort,ReplyMessage,CallbackNumber,CallbackId,FramedRoute,FramedIPXNetwork,State,Class,VendorSpecific,SessionTimeout,IdleTimeout,TerminationAction,CalledStationId,CallingStationId,NASIdentifier,ProxyState,LoginLATService,LoginLATNode,LoginLATGroup,FramedAppleTalkLink,FramedAppleTalkNetwork,FramedAppleTalkZone,AcctStatusType,AcctDelayTime,AcctInputOctets,AcctOutputOctets,AcctSessionId,AcctAuthentic,AcctSessionTime,AcctInputPackets,AcctOutputPackets,AcctTerminateCause,AcctMultiSessionId,AcctLinkCount,AcctInputGigawords,AcctOutputGigawords,EventTimestamp,CHAPChallenge,NASPortType,PortLimit,LoginLATPort,AcctTunnelConnection,ARAPFeatures,ARAPZoneAccess,ARAPSecurity,ARAPSecurityData,PasswordRetry,Prompt,ConnectInfo,ConfigurationToken,EAPMessage,MessageAuthenticator,ARAPChallengeResponse,AcctInterimInterval,NASPortId,FramedPool,NASIPv6Address,FramedInterfaceId,FramedIPv6Prefix,LoginIPv6Host,FramedIPv6Route,FramedIPv6Pool,DigestResponse,DigestAttributes,CREATE_DATE,LAST_MODIFIED_DATE,DelegatedIPv6Prefix,FramedIPv6Address,DNSServerIPv6Address,RouteIPv6Information,DelegatedIPv6PrefixPool,StatefulIPv6AddressPool from tblliveuser where AcctSessionId=?";
    private static String strLiveSessionDelete = "delete from tblliveuser where ACCTSESSIONID=? and USERNAME=?";
    private static String strLiveSessionInsert = "insert into tblliveuser(UserName,NASIPAddress,NASPort,ServiceType,FramedProtocol,FramedIPAddress,FramedIPNetmask,FramedRouting,FilterId,FramedMTU,FramedCompression,LoginIPHost,LoginService,LoginTCPPort,ReplyMessage,CallbackNumber,CallbackId,FramedRoute,FramedIPXNetwork,State,Class,VendorSpecific,SessionTimeout,IdleTimeout,TerminationAction,CalledStationId,CallingStationId,NASIdentifier,ProxyState,LoginLATService,LoginLATNode,LoginLATGroup,FramedAppleTalkLink,FramedAppleTalkNetwork,FramedAppleTalkZone,AcctStatusType,AcctDelayTime,AcctInputOctets,AcctOutputOctets,AcctSessionId,AcctAuthentic,AcctSessionTime,AcctInputPackets,AcctOutputPackets,AcctTerminateCause,AcctMultiSessionId,AcctLinkCount,AcctInputGigawords,AcctOutputGigawords,EventTimestamp,CHAPChallenge,NASPortType,PortLimit,LoginLATPort,AcctTunnelConnection,ARAPFeatures,ARAPZoneAccess,ARAPSecurity,ARAPSecurityData,PasswordRetry,Prompt,ConnectInfo,ConfigurationToken,EAPMessage,MessageAuthenticator,ARAPChallengeResponse,AcctInterimInterval,NASPortId,FramedPool,NASIPv6Address,FramedInterfaceId,FramedIPv6Prefix,LoginIPv6Host,FramedIPv6Route,FramedIPv6Pool,DigestResponse,DigestAttributes,DelegatedIPv6Prefix,FramedIPv6Address,DNSServerIPv6Address,RouteIPv6Information,DelegatedIPv6PrefixPool,StatefulIPv6AddressPool) values(:UserName,:NASIPAddress,:NASPort,:ServiceType,:FramedProtocol,:FramedIPAddress,:FramedIPNetmask,:FramedRouting,:FilterId,:FramedMTU,:FramedCompression,:LoginIPHost,:LoginService,:LoginTCPPort,:ReplyMessage,:CallbackNumber,:CallbackId,:FramedRoute,:FramedIPXNetwork,:State,:Class,:VendorSpecific,:SessionTimeout,:IdleTimeout,:TerminationAction,:CalledStationId,:CallingStationId,:NASIdentifier,:ProxyState,:LoginLATService,:LoginLATNode,:LoginLATGroup,:FramedAppleTalkLink,:FramedAppleTalkNetwork,:FramedAppleTalkZone,:AcctStatusType,:AcctDelayTime,:AcctInputOctets,:AcctOutputOctets,:AcctSessionId,:AcctAuthentic,:AcctSessionTime,:AcctInputPackets,:AcctOutputPackets,:AcctTerminateCause,:AcctMultiSessionId,:AcctLinkCount,:AcctInputGigawords,:AcctOutputGigawords,:EventTimestamp,:CHAPChallenge,:NASPortType,:PortLimit,:LoginLATPort,:AcctTunnelConnection,:ARAPFeatures,:ARAPZoneAccess,:ARAPSecurity,:ARAPSecurityData,:PasswordRetry,:Prompt,:ConnectInfo,:ConfigurationToken,:EAPMessage,:MessageAuthenticator,:ARAPChallengeResponse,:AcctInterimInterval,:NASPortId,:FramedPool,:NASIPv6Address,:FramedInterfaceId,:FramedIPv6Prefix,:LoginIPv6Host,:FramedIPv6Route,:FramedIPv6Pool,:DigestResponse,:DigestAttributes,:DelegatedIPv6Prefix,:FramedIPv6Address,:DNSServerIPv6Address,:RouteIPv6Information,:DelegatedIPv6PrefixPool,:StatefulIPv6AddressPool)";
    private static String strLiveSessionUpdate = "update tblliveuser set AcctInputOctets=?,AcctOutputOctets=?,AcctSessionTime=?,AcctInputPackets=?,AcctOutputPackets=?,AcctInputGigawords=?\n,AcctOutputGigawords=?,AcctStatusType=?,LAST_MODIFIED_DATE=?,FramedIPAddress=?,FramedIPv6Prefix=?,FramedInterfaceId=? where AcctSessionId=?";
    private static String strGetUsageFromCustName = "select AcctInputOctets,AcctOutputOctets,AcctInputGigawords,AcctOutputGigawords,AcctSessionTime,NASIPAddress,AcctSessionId,UserName,FramedIPAddress from tblliveuser where UserName=?;";
    private static String strLiveUsageFromSessionId = "select * from tblliveuser where AcctSessionId=?;";
    private static String strGetUsedQuotaFromCustPackageRelID = "select qtdtl.quotatype,qtdtl.quotaunit , qtdtl.timequotaunit , totalquotakb, usedquotakb, timetotalquotasec, timeusedquotasec,datacategory,mpp.plangroup,speeddowngradeflag from tblcustquotadtls as qtdtl  INNER JOIN tblcustpackagerel as packrel on qtdtl.custpackageid=packrel.custpackageid INNER JOIN tblmpostpaidplan as mpp on mpp.postpaidplanid=packrel.planid where qtdtl.custpackageid=?";
    private static String strDuplicateStopCheck = "select * From $ACCT_CDR_TABLE_NAME t where UserName =? and AcctSessionId =? and lower(AcctStatusType) ='stop' and NASIPAddress =? and AcctSessionTime=?";
    private static String strDuplicateStartCheck = "select * From tblliveuser t where UserName =? and NASIPAddress =? and FramedIPAddress =?";
    static Logger log = LoggerFactory.getLogger("radiuslog");

    public DBAccountingDriver(String pACKET_IDENTIFIER) {
        this.PACKET_IDENTIFIER = pACKET_IDENTIFIER + "[DBAccountingDriver]";
    }

    public DBAccountingDriver() {
    }

    private int getDBCDR(String strSessionId) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN GET CDR FROM DB :" + strSessionId + "QUERY" + strRadiusAccountSelect);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        int intCount = 0;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strRadiusAccountSelect);
            stmt.setString(1, strSessionId);

            for(rset = stmt.executeQuery(); rset.next(); ++intCount) {
            }
        } catch (SQLException var23) {
            SQLException e = var23;
            log.error(this.PACKET_IDENTIFIER + "Error in getDBCDR():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var22) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var21) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var20) {
            }

        }

        return intCount;
    }

    public boolean insertCDR(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        return this.insertCDR(acctRequest, acctFieldMapping, null);
    }

    public boolean insertCDR(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, CDRData liveuserData) throws Exception {
        
        log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR FROM DB QUERY:");

        NamedParameterStatement stmt = null;
        ResultSet rset = null;
        customerData custDBData = null;
        try (Connection conn = DataSource.getConnection()) {
            String cdrTable = "tblacctcdr_" + LocalDateTime.now().getYear() + "_" + LocalDateTime.now().getMonthValue();
            String cdrInsertSQL = null;
            if (liveuserData == null) {
                cdrInsertSQL = strRadiusAccountInsert.replace("$ACCT_CDR_TABLE_NAME", cdrTable);
                log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR FROM DB QUERY WITHOUT CREATE_DATE:" + cdrInsertSQL);
                stmt = new NamedParameterStatement(conn, cdrInsertSQL);
            } else {
                cdrInsertSQL = strRadiusAccountInsertWithCreateDate.replace("$ACCT_CDR_TABLE_NAME", cdrTable);
                log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR FROM DB QUERY WITH CREATE_DATE:" + cdrInsertSQL);
                stmt = new NamedParameterStatement(conn, cdrInsertSQL);
            }

            Iterator<Map.Entry<String, String>> itr = acctFieldMapping.entrySet().iterator();
            RadiusAttribute radAttrib = null;

            while (itr.hasNext()) {
                radAttrib = null;
                Map.Entry<String, String> entry =  itr.next();

                try {
                    radAttrib = acctRequest.getAttribute( entry.getKey());
                } catch (Exception var35) {
                    log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR Attribute not found:" +  entry.getKey());
                }

                if (radAttrib != null) {
                    stmt.setString( entry.getValue(), radAttrib.getAttributeValue());
                } else {
                    stmt.setString( entry.getValue(),  null);
                }
            }

            String attr = null != acctRequest.getAttributeValue("Acct-Status-Type") ? acctRequest.getAttributeValue("Acct-Status-Type") : null;
            if (null == attr || !attr.equalsIgnoreCase("Alive") && !attr.equalsIgnoreCase("Interim-Update") && !attr.equalsIgnoreCase("Stop")) {
                stmt.setString("jsontext",  null);
            } else {
                stmt.setString("jsontext", (new ObjectMapper()).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).writeValueAsString(acctRequest));
            }

            if (liveuserData != null) {
                stmt.setString("createdate", liveuserData.getLastmodifiedddate().toString());
            }

            synchronized (this) {
                int var14 = stmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in insertCDR():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    ((ResultSet) rset).close();
                }
            } catch (Exception var33) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var32) {
            }

        }

        return true;
    }

    private int getDBSession(String strSessionId) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Delete Session,Session Id:" + strSessionId + ",Query:" + strLiveSessionSelect);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        int intCount = 0;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strLiveSessionSelect);
            stmt.setString(1, strSessionId);

            for(rset = stmt.executeQuery(); rset.next(); ++intCount) {
            }
        } catch (SQLException var23) {
            SQLException e = var23;
            log.error(this.PACKET_IDENTIFIER + "Error in getDBSession():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var22) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var21) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var20) {
            }

        }

        return intCount;
    }

    public boolean insertDBSesion(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Insert Session QUERY" + strLiveSessionInsert + ", Mapping:" + acctFieldMapping.size());

        NamedParameterStatement stmt = null;
        ResultSet rset = null;

        try (Connection conn = DataSource.getConnection()) {
            stmt = new NamedParameterStatement(conn, strLiveSessionInsert);
            Iterator<Map.Entry<String, String>> itr = acctFieldMapping.entrySet().iterator();
            RadiusAttribute radAttrib = null;

            while (itr.hasNext()) {
                radAttrib = null;
                Map.Entry<String, String> entry = (Map.Entry) itr.next();

                try {
                    radAttrib = acctRequest.getAttribute(entry.getKey());
                } catch (Exception var27) {
                    log.info(this.PACKET_IDENTIFIER + "IN Insert Session QUERY Attribute not found:" + entry.getKey());
                }

                if (radAttrib != null) {
                    stmt.setString(entry.getValue(), radAttrib.getAttributeValue());
                } else {
                    stmt.setString(entry.getValue(), null);
                }
            }

            int var31 = stmt.executeUpdate();
        } catch (SQLException var28) {
            SQLException e = var28;
            log.error(this.PACKET_IDENTIFIER + "Error in insertDBSesion():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    ((ResultSet) rset).close();
                }
            } catch (Exception var26) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var25) {
            }

        }

        return true;
    }

    public boolean deleteDBSesion(String strSessionId, String strUsername) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Delete Session,Session Id:" + strSessionId + ",QUERY:" + strLiveSessionDelete);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strLiveSessionDelete);
            stmt.setString(1, strSessionId);
            stmt.setString(2, strUsername);
            int var25 = stmt.executeUpdate();
        } catch (SQLException var23) {
            SQLException e = var23;
            log.error(this.PACKET_IDENTIFIER + "IN deleteDBSesion:" + e, e);
        } finally {
            try {
                if (rset != null) {
                    ((ResultSet)rset).close();
                }
            } catch (Exception var22) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var21) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var20) {
            }

        }

        return true;
    }

    public boolean updateDBSesion(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Update Session FROM QUERY" + strLiveSessionUpdate);
        long starttime = System.currentTimeMillis();
        ResultSet rset = null;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strLiveSessionUpdate)) {
            RadiusAttribute acctInputOctets = acctRequest.getAttribute("Acct-Input-Octets");
            RadiusAttribute acctOutputOctets = acctRequest.getAttribute("Acct-Output-Octets");
            RadiusAttribute acctSessionTime = acctRequest.getAttribute("Acct-Session-Time");
            RadiusAttribute acctInputPackets = acctRequest.getAttribute("Acct-Input-Packets");
            RadiusAttribute acctOutputPackets = acctRequest.getAttribute("Acct-Output-Packets");
            RadiusAttribute acctInputGigawords = acctRequest.getAttribute("Acct-Input-Gigawords");
            RadiusAttribute acctOutputGigawords = acctRequest.getAttribute("Acct-Output-Gigawords");
            RadiusAttribute acctSessionId = acctRequest.getAttribute("Acct-Session-Id");
            RadiusAttribute acctStatusType = acctRequest.getAttribute("Acct-Status-Type");
            RadiusAttribute acctFramedIpAddress = acctRequest.getAttribute("Framed-IP-Address");
            RadiusAttribute acctFramedIPV6Prefix = acctRequest.getAttribute("Framed-IPv6-Prefix");
            RadiusAttribute acctFramedInterfaceId = acctRequest.getAttribute("Framed-Interface-Id");
            if (acctInputOctets != null) {
                stmt.setString(1, acctInputOctets.getAttributeValue());
            } else {
                stmt.setString(1, null);
            }

            if (acctOutputOctets != null) {
                stmt.setString(2, acctOutputOctets.getAttributeValue());
            } else {
                stmt.setString(2, null);
            }

            if (acctSessionTime != null) {
                stmt.setString(3, acctSessionTime.getAttributeValue());
            } else {
                stmt.setString(3, null);
            }

            if (acctInputPackets != null) {
                stmt.setString(4, acctInputPackets.getAttributeValue());
            } else {
                stmt.setString(4, null);
            }

            if (acctOutputPackets != null) {
                stmt.setString(5, acctOutputPackets.getAttributeValue());
            } else {
                stmt.setString(5, null);
            }

            if (acctInputGigawords != null) {
                stmt.setString(6, acctInputGigawords.getAttributeValue());
            } else {
                stmt.setString(6, null);
            }

            if (acctOutputGigawords != null) {
                stmt.setString(7, acctOutputGigawords.getAttributeValue());
            } else {
                stmt.setString(7, null);
            }

            if (acctStatusType != null) {
                stmt.setString(8, acctStatusType.getAttributeValue());
            } else {
                stmt.setString(8, null);
            }

            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            if (acctFramedIpAddress != null) {
                stmt.setString(10, acctFramedIpAddress.getAttributeValue());
            } else {
                stmt.setString(10, null);
            }

            if (acctFramedIPV6Prefix != null) {
                stmt.setString(11, acctFramedIPV6Prefix.getAttributeValue());
            } else {
                stmt.setString(11, null);
            }

            if (acctFramedInterfaceId != null) {
                stmt.setString(12, acctFramedInterfaceId.getAttributeValue());
            } else {
                stmt.setString(12, null);
            }

            stmt.setString(13, acctSessionId.getAttributeValue());
            int count = stmt.executeUpdate();
            long queryTime = System.currentTimeMillis() - starttime;
            log.debug(this.PACKET_IDENTIFIER + "IN Update Session FROM QUERY Time:" + queryTime);
        } catch (SQLException var39) {
            SQLException e = var39;
            log.error(this.PACKET_IDENTIFIER + "Error in updateDBSesion():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    ((ResultSet) rset).close();
                }
            } catch (Exception var38) {
            }

        }

        return true;
    }

    public ArrayList<CDRData> getCustomerCoAStatus(String custName, Long custPackRelId) {
        log.debug(this.PACKET_IDENTIFIER + "IN getCustomerCoAStatus() FROM DB :" + custName + ",QUERY " + strGetUsedQuotaFromCustPackageRelID);
        Connection conn1 = null;
        PreparedStatement stmt1 = null;
        ResultSet rset1 = null;
        double usedQuotaBytes = 0.0;
        double totalQuotaBytes = 0.0;
        double usedQuotaSeconds = 0.0;
        double totalQuotaSeconds = 0.0;
        double totalQuotaKB = 0.0;
        boolean bSpeedDowngraded = false;
        boolean isFUPPlan = false;

        try {
            conn1 = DataSource.getConnection();
            stmt1 = conn1.prepareStatement(strGetUsedQuotaFromCustPackageRelID);
            stmt1.setLong(1, custPackRelId);
            rset1 = stmt1.executeQuery();
            if (rset1.next()) {
                log.debug(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() Result Found");
                totalQuotaKB = Double.parseDouble(rset1.getString("totalquotakb"));
                if (totalQuotaKB != AAAConstant.UNLIMITED_QUOTA) {
                    totalQuotaBytes = totalQuotaKB * 1024.0;
                } else {
                    totalQuotaBytes = AAAConstant.UNLIMITED_QUOTA;
                }

                usedQuotaBytes = Double.parseDouble(rset1.getString("usedquotakb")) * 1024.0;
                totalQuotaSeconds = Double.parseDouble(rset1.getString("timetotalquotasec"));
                usedQuotaSeconds = Double.parseDouble(rset1.getString("timeusedquotasec"));
                bSpeedDowngraded = rset1.getBoolean("speeddowngradeflag");
                if (rset1.getString("datacategory") != null && "FUP".equalsIgnoreCase(rset1.getString("datacategory"))) {
                    isFUPPlan = true;
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getCustomerCoAStatus():" + e, e);
        } finally {
            try {
                if (rset1 != null) {
                    rset1.close();
                }
            } catch (Exception var38) {
            }

            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception var37) {
            }

            try {
                if (conn1 != null) {
                    conn1.close();
                }
            } catch (Exception var36) {
            }

        }

        log.info(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() isFUPPlan:" + isFUPPlan + ",bSpeedDowngraded:" + bSpeedDowngraded);
        if (!bSpeedDowngraded) {

            double totalLiveSessionBytes = 0.0;
            double totalLiveSessionslTime = 0.0;

            ArrayList<CDRData> cdrList = this.getAllLiveSessions(custName);
            CDRData cdrData;
            if (cdrList != null && cdrList.size() > 0) {
                for(Iterator<CDRData> var23 = cdrList.iterator(); var23.hasNext(); totalLiveSessionslTime += cdrData.getTotalActualTime()) {
                    cdrData = var23.next();
                    totalLiveSessionBytes += cdrData.getTotalActualBytes();
                }
            }

            log.debug(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() totalLiveSessionBytes :" + totalLiveSessionBytes + " totalLiveSessionsTime: " + totalLiveSessionslTime);
            log.debug(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() Quota :: TotalQuotaKB:" + totalQuotaKB + ",TotalQuotaBytes:" + totalQuotaBytes + ",TotalUsedBytes:" + usedQuotaBytes);
            log.debug(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() Time :: totalQuotaSeconds:" + totalQuotaSeconds + ",usedQuotaSeconds:" + usedQuotaSeconds);
            if (totalQuotaBytes != AAAConstant.UNLIMITED_QUOTA && totalQuotaBytes < usedQuotaBytes + totalLiveSessionBytes) {
                log.info(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() Quota COA Triggered Total:" + totalQuotaBytes + ", Used:" + (usedQuotaBytes + totalLiveSessionBytes));
                return cdrList;
            }

            if (totalQuotaSeconds != AAAConstant.UNLIMITED_QUOTA && totalQuotaSeconds < usedQuotaSeconds + totalLiveSessionslTime) {
                log.info(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() Time COA Triggered Total:" + totalQuotaSeconds + ", Used:" + (usedQuotaSeconds + totalLiveSessionslTime));
                return cdrList;
            }
        }

        return null;
    }

    public void populatePlanUsageDetails(Long custPackRelId, customerData custdata) {
        log.debug(this.PACKET_IDENTIFIER + "IN populatePlanUsageDetails() FROM DB :" + custPackRelId + ",QUERY " + strGetUsedQuotaFromCustPackageRelID);
        Connection conn1 = null;
        PreparedStatement stmt1 = null;
        ResultSet rset1 = null;

        try {
            conn1 = DataSource.getConnection();
            stmt1 = conn1.prepareStatement(strGetUsedQuotaFromCustPackageRelID);
            stmt1.setLong(1, custPackRelId);
            rset1 = stmt1.executeQuery();
            if (rset1.next()) {
                custdata.setQuotaunit(rset1.getString("quotaunit"));
                custdata.setQuotatype(rset1.getString("quotatype"));
                custdata.setTotalquotakb(rset1.getString("totalquotakb"));
                custdata.setUsedquotakb(rset1.getString("usedquotakb"));
                custdata.setTimequotaunit(rset1.getString("timequotaunit"));
                custdata.setTimetotalquotasec(rset1.getString("timetotalquotasec"));
                custdata.setTimeusedquotasec(rset1.getString("timeusedquotasec"));
                String planGroup = rset1.getString("plangroup");
                String dataCategory = rset1.getString("datacategory");
                if (dataCategory != null && !"".equalsIgnoreCase(dataCategory) && "FUP".equalsIgnoreCase(dataCategory) && planGroup.equalsIgnoreCase("Renew")) {
                    custdata.setFUPPlan(true);
                } else {
                    custdata.setFUPPlan(false);
                }
            } else {
                log.debug(this.PACKET_IDENTIFIER + "IN populatePlanUsageDetails() FROM DB, Detals not fuound for: " + custPackRelId);
            }
        } catch (SQLException var24) {
            SQLException e = var24;
            log.error(this.PACKET_IDENTIFIER + "Error IN populatePlanUsageDetails() :" + e, e);
        } finally {
            try {
                if (rset1 != null) {
                    rset1.close();
                }
            } catch (Exception var23) {
            }

            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception var22) {
            }

            try {
                if (conn1 != null) {
                    conn1.close();
                }
            } catch (Exception var21) {
            }

        }

    }

    public CDRData getLiveSessionDetails(String sessionId) {
        log.debug(this.PACKET_IDENTIFIER + "IN getLiveSessionDetails Session Id:" + sessionId + " QUERY " + strLiveUsageFromSessionId);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        double totalActualBytes = 0.0;
        double totalActualTime = 0.0;
        CDRData cdrData = null;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strLiveUsageFromSessionId);
            stmt.setString(1, sessionId);
            rset = stmt.executeQuery();
            if (rset != null && rset.next()) {
                cdrData = new CDRData();
                totalActualBytes = totalActualBytes + Double.parseDouble(rset.getString("AcctInputOctets")) + Double.parseDouble(rset.getString("AcctOutputOctets"));
                double var10000 = totalActualTime + Double.parseDouble(rset.getString("AcctSessionTime"));
                cdrData.setNasip(rset.getString("nasipaddress"));
                cdrData.setSessionid(rset.getString("acctsessionid"));
                cdrData.setUsername(rset.getString("username"));
                cdrData.setFramedip(rset.getString("framedipaddress"));
            }
        } catch (SQLException var27) {
            SQLException e = var27;
            log.error(this.PACKET_IDENTIFIER + "Error in getLiveSessionDetails():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var26) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var25) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var24) {
            }

        }

        return cdrData;
    }

    public ArrayList<CDRData> getAllLiveSessions(String userName) {
        log.debug(this.PACKET_IDENTIFIER + "IN getAllLiveSessions userName:" + userName + " QUERY " + strGetUsageFromCustName);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        double totalActualBytes = 0.0;
        double totalActualTime = 0.0;
        CDRData cdrData = null;
        ArrayList<CDRData> cdrList = new ArrayList();

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strGetUsageFromCustName);
            stmt.setString(1, userName);
            rset = stmt.executeQuery();
            log.debug(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Result Set is " + rset);

            while(rset.next()) {
                cdrData = new CDRData();

                try {
                    totalActualBytes += Double.parseDouble(rset.getString("AcctInputOctets"));
                } catch (Exception var38) {
                    log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Acct Input Octet not found");
                }

                try {
                    totalActualBytes += Double.parseDouble(rset.getString("AcctOutputOctets"));
                } catch (Exception var37) {
                    log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Acct Output Octet not found");
                }

                long uploadGB;
                try {
                    if (rset.getString("AcctInputGigawords") != null) {
                        uploadGB = Long.parseLong(rset.getString("AcctInputGigawords"));
                        totalActualBytes += (double)(uploadGB * 1024L * 1024L * 1024L);
                    } else {
                        log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Input Gigawords not found");
                    }
                } catch (Exception var36) {
                    log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Input Gigawords not found");
                }

                try {
                    if (rset.getString("AcctOutputGigawords") != null) {
                        uploadGB = Long.parseLong(rset.getString("AcctOutputGigawords"));
                        totalActualBytes += (double)(uploadGB * 1024L * 1024L * 1024L);
                    } else {
                        log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Output Gigawords not found");
                    }
                } catch (Exception var35) {
                    log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Output Gigawords not found");
                }

                try {
                    totalActualTime = Double.parseDouble(rset.getString("AcctSessionTime"));
                } catch (Exception var34) {
                    log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Acct Session Time not found");
                }

                cdrData.setNasip(rset.getString("nasipaddress"));
                cdrData.setSessionid(rset.getString("acctsessionid"));
                cdrData.setUsername(rset.getString("username"));
                cdrData.setFramedip(rset.getString("framedipaddress"));
                cdrData.setTotalActualBytes(totalActualBytes);
                cdrData.setTotalActualTime(totalActualTime);
                cdrList.add(cdrData);
            }

            log.debug(this.PACKET_IDENTIFIER + "IN getAllLiveSessions userName:" + userName + ", Total Sessions:" + cdrList.size());
        } catch (SQLException var39) {
            SQLException e = var39;
            log.error(this.PACKET_IDENTIFIER + "Error in getAllLiveSessions():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var33) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var32) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var31) {
            }

        }

        return cdrList;
    }

    public boolean isDupliacteStop(String userName, String SessionId, String NasIPAddress, String sessionTime) {

        String cdrTable = "tblacctcdr_" + LocalDateTime.now().getYear() + "_" + LocalDateTime.now().getMonthValue();
        String duplicateStopCheckSQL = strDuplicateStopCheck.replace("$ACCT_CDR_TABLE_NAME", cdrTable);

        log.debug(this.PACKET_IDENTIFIER + "IN isDuplicateStop() userName:" + userName + ",SessionId:" + SessionId + ",NasIPAddress:" + NasIPAddress + ",sessionTime:" + sessionTime + ",QUERY " + duplicateStopCheckSQL);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        boolean recordExist = false;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(duplicateStopCheckSQL);
            stmt.setString(1, userName);
            stmt.setString(2, SessionId);
            stmt.setString(3, NasIPAddress);
            stmt.setString(4, sessionTime);
            rset = stmt.executeQuery();
            if (rset.next()) {
                recordExist = true;
            }

            log.info(this.PACKET_IDENTIFIER + "IN isDuplicateStop():" + recordExist);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "IN isDuplicateStop():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var27) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var26) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var25) {
            }

        }

        return recordExist;
    }

    public boolean isDupliacteStart(String userName, String NasIPAddress, String framedip) {
        log.debug(this.PACKET_IDENTIFIER + "IN isDupliacteStart() userName:{},NasIPAddress:{},framedip:{}", new Object[]{userName, NasIPAddress, framedip, strDuplicateStartCheck});
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        boolean recordExist = false;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strDuplicateStartCheck);
            stmt.setString(1, userName);
            stmt.setString(2, NasIPAddress);
            stmt.setString(3, framedip);
            rset = stmt.executeQuery();
            if (rset.next()) {
                recordExist = true;
            }

            log.info("{} IN isDuplicateStart():{}", this.PACKET_IDENTIFIER, recordExist);
        } catch (SQLException var25) {
            SQLException e = var25;
            log.error("{} IN isDuplicateStart():{}", new Object[]{this.PACKET_IDENTIFIER, e, e});
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var24) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var23) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var22) {
            }

        }

        return recordExist;
    }

    public CDRData getLiveUserSessionDetails(String acctSessionId) {
        log.debug(this.PACKET_IDENTIFIER + "IN getLiveUserDetails acctSessionId:" + acctSessionId + " QUERY " + strLiveSessionSelect);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        double totalActualBytes = 0.0;
        double totalActualTime = 0.0;
        CDRData cdrData = null;

        try {
            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strLiveSessionSelect);
            stmt.setString(1, acctSessionId);
            rset = stmt.executeQuery();
            log.debug(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Result Set is " + rset);
            if (rset.next()) {
                cdrData = new CDRData();

                try {
                    totalActualBytes += Double.parseDouble(rset.getString("AcctInputOctets"));
                } catch (Exception var41) {
                    log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Acct Input Octet not found");
                }

                try {
                    totalActualBytes += Double.parseDouble(rset.getString("AcctOutputOctets"));
                } catch (Exception var40) {
                    log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Acct Output Octet not found");
                }

                long uploadGB;
                try {
                    if (rset.getString("AcctInputGigawords") != null) {
                        uploadGB = Long.parseLong(rset.getString("AcctInputGigawords"));
                        totalActualBytes += (double)(uploadGB * 1024L * 1024L * 1024L);
                    } else {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Input Gigawords not found");
                    }
                } catch (Exception var39) {
                    log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Input Gigawords not found");
                }

                try {
                    if (rset.getString("AcctOutputGigawords") != null) {
                        uploadGB = Long.parseLong(rset.getString("AcctOutputGigawords"));
                        totalActualBytes += (double)(uploadGB * 1024L * 1024L * 1024L);
                    } else {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Output Gigawords not found");
                    }
                } catch (Exception var38) {
                    log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Output Gigawords not found");
                }

                try {
                    totalActualTime = Double.parseDouble(rset.getString("AcctSessionTime"));
                } catch (Exception var37) {
                    log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Acct Session Time not found");
                }

                cdrData.setNasip(rset.getString("nasipaddress"));
                cdrData.setSessionid(rset.getString("acctsessionid"));
                cdrData.setUsername(rset.getString("username"));
                cdrData.setFramedip(rset.getString("framedipaddress"));
                cdrData.setTotalActualBytes(totalActualBytes);
                cdrData.setTotalActualTime(totalActualTime);

                try {
                    cdrData.setCreatedate(rset.getString("CREATE_DATE"));
                } catch (Exception var36) {
                }

                try {
                    cdrData.setLastmodifiedddate(rset.getString("LAST_MODIFIED_DATE"));
                } catch (Exception var35) {
                }
            } else {
                log.debug(this.PACKET_IDENTIFIER + "IN getLiveUserDetails() acctSessionId:" + acctSessionId + ", NOT FOUND");
            }
        } catch (Exception var42) {
            Exception e = var42;
            log.error(this.PACKET_IDENTIFIER + "Error in getLiveUserDetails():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var34) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var33) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var32) {
            }

        }

        return cdrData;
    }
}
