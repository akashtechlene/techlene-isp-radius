/**
 * @author Akash Gaur
 * @created year 2026
 */

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
    private static String strLiveUsageFromSessionId = "select AcctInputOctets,AcctOutputOctets,AcctSessionTime,NASIPAddress,AcctSessionId,UserName,FramedIPAddress from tblliveuser where AcctSessionId=?;";
    private static String strGetUsedQuotaFromCustPackageRelID = "select qtdtl.quotatype,qtdtl.quotaunit , qtdtl.timequotaunit , totalquotakb, usedquotakb, timetotalquotasec, timeusedquotasec,datacategory,mpp.plangroup,speeddowngradeflag from tblcustquotadtls as qtdtl  INNER JOIN tblcustpackagerel as packrel on qtdtl.custpackageid=packrel.custpackageid INNER JOIN tblmpostpaidplan as mpp on mpp.postpaidplanid=packrel.planid where qtdtl.custpackageid=?";
    private static String strDuplicateStopCheck = "select 1 From $ACCT_CDR_TABLE_NAME t where UserName =? and AcctSessionId =? and lower(AcctStatusType) ='stop' and NASIPAddress =? and AcctSessionTime=?";
    private static String strDuplicateStartCheck = "select 1 From tblliveuser t where UserName =? and NASIPAddress =? and FramedIPAddress =?";
    static Logger log = LoggerFactory.getLogger("radiuslog");

    public DBAccountingDriver(String pACKET_IDENTIFIER) {
        this.PACKET_IDENTIFIER = pACKET_IDENTIFIER + "[DBAccountingDriver]";
    }

    public DBAccountingDriver() {
    }

    private int getDBCDR(String strSessionId) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN GET CDR FROM DB :" + strSessionId + "QUERY" + strRadiusAccountSelect);
        int intCount = 0;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strRadiusAccountSelect)) {
            stmt.setString(1, strSessionId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    ++intCount;
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getDBCDR():" + e, e);
        }

        return intCount;
    }

    public boolean insertCDR(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        return this.insertCDR(acctRequest, acctFieldMapping, null);
    }

    public boolean insertCDR(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, CDRData liveuserData) throws Exception {
        try (Connection conn = DataSource.getConnection()) {
            return insertCDR(conn, acctRequest, acctFieldMapping, liveuserData);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in insertCDR():" + e, e);
            return true;
        }
    }

    public boolean insertCDR(Connection conn, AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, CDRData liveuserData) throws Exception {

        log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR FROM DB QUERY:");

        try {
            String cdrTable = "tblacctcdr_" + LocalDateTime.now().getYear() + "_" + LocalDateTime.now().getMonthValue();
            String cdrInsertSQL;
            if (liveuserData == null) {
                cdrInsertSQL = strRadiusAccountInsert.replace("$ACCT_CDR_TABLE_NAME", cdrTable);
                log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR FROM DB QUERY WITHOUT CREATE_DATE:" + cdrInsertSQL);
            } else {
                cdrInsertSQL = strRadiusAccountInsertWithCreateDate.replace("$ACCT_CDR_TABLE_NAME", cdrTable);
                log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR FROM DB QUERY WITH CREATE_DATE:" + cdrInsertSQL);
            }

            try (NamedParameterStatement stmt = new NamedParameterStatement(conn, cdrInsertSQL)) {
                this.bindAttributes(stmt, acctRequest, acctFieldMapping, name -> log.debug(this.PACKET_IDENTIFIER + "IN Insert CDR Attribute not found:" + name));

                String attr = acctRequest.getAttributeValue("Acct-Status-Type");
                if (null == attr || !attr.equalsIgnoreCase("Alive") && !attr.equalsIgnoreCase("Interim-Update") && !attr.equalsIgnoreCase("Stop")) {
                    stmt.setString("jsontext", null);
                } else {
                    stmt.setString("jsontext", (new ObjectMapper()).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).writeValueAsString(acctRequest));
                }

                if (liveuserData != null) {
                    stmt.setString("createdate", liveuserData.getLastmodifiedddate().toString());
                }

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in insertCDR():" + e, e);
        }

        return true;
    }

    /** Binds every mapped request attribute (attribute name -> named parameter) to the statement; absent ones bind null. */
    @SuppressWarnings("unchecked")
    private void bindAttributes(NamedParameterStatement stmt, AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping, java.util.function.Consumer<String> onAttributeNotFound) throws SQLException {
        for (Map.Entry<String, String> entry : ((ConcurrentHashMap<String, String>) acctFieldMapping).entrySet()) {
            RadiusAttribute radAttrib = null;

            try {
                radAttrib = acctRequest.getAttribute(entry.getKey());
            } catch (Exception e) {
                onAttributeNotFound.accept(entry.getKey());
            }

            stmt.setString(entry.getValue(), radAttrib != null ? radAttrib.getAttributeValue() : null);
        }
    }

    private int getDBSession(String strSessionId) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Delete Session,Session Id:" + strSessionId + ",Query:" + strLiveSessionSelect);
        int intCount = 0;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strLiveSessionSelect)) {
            stmt.setString(1, strSessionId);

            try (ResultSet rset = stmt.executeQuery()) {
                while (rset.next()) {
                    ++intCount;
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getDBSession():" + e, e);
        }

        return intCount;
    }

    public boolean insertDBSesion(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        try (Connection conn = DataSource.getConnection()) {
            return insertDBSesion(conn, acctRequest, acctFieldMapping);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in insertDBSesion():" + e, e);
            return true;
        }
    }

    public boolean insertDBSesion(Connection conn, AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Insert Session QUERY" + strLiveSessionInsert + ", Mapping:" + acctFieldMapping.size());

        try (NamedParameterStatement stmt = new NamedParameterStatement(conn, strLiveSessionInsert)) {
            this.bindAttributes(stmt, acctRequest, acctFieldMapping, name -> log.info(this.PACKET_IDENTIFIER + "IN Insert Session QUERY Attribute not found:" + name));
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in insertDBSesion():" + e, e);
        }

        return true;
    }

    public boolean deleteDBSesion(String strSessionId, String strUsername) throws Exception {
        try (Connection conn = DataSource.getConnection()) {
            return deleteDBSesion(conn, strSessionId, strUsername);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "IN deleteDBSesion:" + e, e);
            return true;
        }
    }

    public boolean deleteDBSesion(Connection conn, String strSessionId, String strUsername) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Delete Session,Session Id:" + strSessionId + ",QUERY:" + strLiveSessionDelete);

        try (PreparedStatement stmt = conn.prepareStatement(strLiveSessionDelete)) {
            stmt.setString(1, strSessionId);
            stmt.setString(2, strUsername);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "IN deleteDBSesion:" + e, e);
        }

        return true;
    }

    public boolean updateDBSesion(AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        try (Connection conn = DataSource.getConnection()) {
            return updateDBSesion(conn, acctRequest, acctFieldMapping);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in updateDBSesion():" + e, e);
            return true;
        }
    }

    public boolean updateDBSesion(Connection conn, AccountingRequest acctRequest, ConcurrentHashMap acctFieldMapping) throws Exception {
        log.debug(this.PACKET_IDENTIFIER + "IN Update Session FROM QUERY" + strLiveSessionUpdate);
        long starttime = System.currentTimeMillis();

        try (PreparedStatement stmt = conn.prepareStatement(strLiveSessionUpdate)) {
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
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in updateDBSesion():" + e, e);
        }

        return true;
    }

    public ArrayList<CDRData> getCustomerCoAStatus(String custName, Long custPackRelId) {
        try (Connection conn = DataSource.getConnection()) {
            return getCustomerCoAStatus(conn, custName, custPackRelId);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getCustomerCoAStatus():" + e, e);
            return null;
        }
    }

    public ArrayList<CDRData> getCustomerCoAStatus(Connection conn, String custName, Long custPackRelId) {
        log.debug(this.PACKET_IDENTIFIER + "IN getCustomerCoAStatus() FROM DB :" + custName + ",QUERY " + strGetUsedQuotaFromCustPackageRelID);
        double usedQuotaBytes = 0.0;
        double totalQuotaBytes = 0.0;
        double usedQuotaSeconds = 0.0;
        double totalQuotaSeconds = 0.0;
        double totalQuotaKB = 0.0;
        boolean bSpeedDowngraded = false;
        boolean isFUPPlan = false;

        try (PreparedStatement stmt1 = conn.prepareStatement(strGetUsedQuotaFromCustPackageRelID)) {
            stmt1.setLong(1, custPackRelId);
            try (ResultSet rset1 = stmt1.executeQuery()) {
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
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getCustomerCoAStatus():" + e, e);
        }

        log.info(this.PACKET_IDENTIFIER + "In getCustomerCoAStatus() isFUPPlan:" + isFUPPlan + ",bSpeedDowngraded:" + bSpeedDowngraded);
        if (!bSpeedDowngraded) {
            double totalLiveSessionBytes = 0.0;
            double totalLiveSessionslTime = 0.0;

            ArrayList<CDRData> cdrList = this.getAllLiveSessions(conn, custName);
            if (cdrList != null) {
                for (CDRData cdrData : cdrList) {
                    totalLiveSessionBytes += cdrData.getTotalActualBytes();
                    totalLiveSessionslTime += cdrData.getTotalActualTime();
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
        try (Connection conn = DataSource.getConnection()) {
            populatePlanUsageDetails(conn, custPackRelId, custdata);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN populatePlanUsageDetails() :" + e, e);
        }
    }

    public void populatePlanUsageDetails(Connection conn, Long custPackRelId, customerData custdata) {
        log.debug(this.PACKET_IDENTIFIER + "IN populatePlanUsageDetails() FROM DB :" + custPackRelId + ",QUERY " + strGetUsedQuotaFromCustPackageRelID);

        try (PreparedStatement stmt1 = conn.prepareStatement(strGetUsedQuotaFromCustPackageRelID)) {
            stmt1.setLong(1, custPackRelId);
            try (ResultSet rset1 = stmt1.executeQuery()) {
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
                    custdata.setFUPPlan(dataCategory != null && !"".equalsIgnoreCase(dataCategory) && "FUP".equalsIgnoreCase(dataCategory) && planGroup.equalsIgnoreCase("Renew"));
                } else {
                    log.debug(this.PACKET_IDENTIFIER + "IN populatePlanUsageDetails() FROM DB, Detals not fuound for: " + custPackRelId);
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error IN populatePlanUsageDetails() :" + e, e);
        }
    }

    public CDRData getLiveSessionDetails(String sessionId) {
        log.debug(this.PACKET_IDENTIFIER + "IN getLiveSessionDetails Session Id:" + sessionId + " QUERY " + strLiveUsageFromSessionId);
        CDRData cdrData = null;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strLiveUsageFromSessionId)) {
            stmt.setString(1, sessionId);

            try (ResultSet rset = stmt.executeQuery()) {
                if (rset != null && rset.next()) {
                    cdrData = new CDRData();
                    // the octet counters and session time are only validated here (a missing or non-numeric value throws); they are not stored
                    Double.parseDouble(rset.getString("AcctInputOctets"));
                    Double.parseDouble(rset.getString("AcctOutputOctets"));
                    Double.parseDouble(rset.getString("AcctSessionTime"));
                    cdrData.setNasip(rset.getString("nasipaddress"));
                    cdrData.setSessionid(rset.getString("acctsessionid"));
                    cdrData.setUsername(rset.getString("username"));
                    cdrData.setFramedip(rset.getString("framedipaddress"));
                }
            }
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getLiveSessionDetails():" + e, e);
        }

        return cdrData;
    }

    public ArrayList<CDRData> getAllLiveSessions(String userName) {
        try (Connection conn = DataSource.getConnection()) {
            return getAllLiveSessions(conn, userName);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getAllLiveSessions():" + e, e);
            return new ArrayList<>();
        }
    }

    public ArrayList<CDRData> getAllLiveSessions(Connection conn, String userName) {
        log.debug(this.PACKET_IDENTIFIER + "IN getAllLiveSessions userName:" + userName + " QUERY " + strGetUsageFromCustName);
        double totalActualBytes = 0.0;
        double totalActualTime = 0.0;
        ArrayList<CDRData> cdrList = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(strGetUsageFromCustName)) {
            stmt.setString(1, userName);
            try (ResultSet rset = stmt.executeQuery()) {
                log.debug(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Result Set is " + rset);

                // the byte total keeps growing across rows (each CDRData carries the running total), as it always did
                while (rset.next()) {
                    CDRData cdrData = new CDRData();

                    try {
                        totalActualBytes += Double.parseDouble(rset.getString("AcctInputOctets"));
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Acct Input Octet not found");
                    }

                    try {
                        totalActualBytes += Double.parseDouble(rset.getString("AcctOutputOctets"));
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Acct Output Octet not found");
                    }

                    try {
                        if (rset.getString("AcctInputGigawords") != null) {
                            long inputGigawords = Long.parseLong(rset.getString("AcctInputGigawords"));
                            totalActualBytes += (double) (inputGigawords * 1024L * 1024L * 1024L);
                        } else {
                            log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Input Gigawords not found");
                        }
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Input Gigawords not found");
                    }

                    try {
                        if (rset.getString("AcctOutputGigawords") != null) {
                            long outputGigawords = Long.parseLong(rset.getString("AcctOutputGigawords"));
                            totalActualBytes += (double) (outputGigawords * 1024L * 1024L * 1024L);
                        } else {
                            log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Output Gigawords not found");
                        }
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getAllLiveSessions Output Gigawords not found");
                    }

                    try {
                        totalActualTime = Double.parseDouble(rset.getString("AcctSessionTime"));
                    } catch (Exception e) {
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
            }

            log.debug(this.PACKET_IDENTIFIER + "IN getAllLiveSessions userName:" + userName + ", Total Sessions:" + cdrList.size());
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getAllLiveSessions():" + e, e);
        }

        return cdrList;
    }

    public boolean isDupliacteStop(String userName, String SessionId, String NasIPAddress, String sessionTime) {
        try (Connection conn = DataSource.getConnection()) {
            return isDupliacteStop(conn, userName, SessionId, NasIPAddress, sessionTime);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "IN isDuplicateStop():" + e, e);
            return false;
        }
    }

    public boolean isDupliacteStop(Connection conn, String userName, String SessionId, String NasIPAddress, String sessionTime) {

        String cdrTable = "tblacctcdr_" + LocalDateTime.now().getYear() + "_" + LocalDateTime.now().getMonthValue();
        String duplicateStopCheckSQL = strDuplicateStopCheck.replace("$ACCT_CDR_TABLE_NAME", cdrTable);

        log.debug(this.PACKET_IDENTIFIER + "IN isDuplicateStop() userName:" + userName + ",SessionId:" + SessionId + ",NasIPAddress:" + NasIPAddress + ",sessionTime:" + sessionTime + ",QUERY " + duplicateStopCheckSQL);

        boolean recordExist = false;

        try (PreparedStatement stmt = conn.prepareStatement(duplicateStopCheckSQL)) {
            stmt.setString(1, userName);
            stmt.setString(2, SessionId);
            stmt.setString(3, NasIPAddress);
            stmt.setString(4, sessionTime);
            try (ResultSet rset = stmt.executeQuery()) {
                recordExist = rset.next();
            }

            log.info(this.PACKET_IDENTIFIER + "IN isDuplicateStop():" + recordExist);
        } catch (SQLException e) {
            log.error(this.PACKET_IDENTIFIER + "IN isDuplicateStop():" + e, e);
        }

        return recordExist;
    }

    public boolean isDupliacteStart(String userName, String NasIPAddress, String framedip) {
        try (Connection conn = DataSource.getConnection()) {
            return isDupliacteStart(conn, userName, NasIPAddress, framedip);
        } catch (SQLException e) {
            log.error("{} IN isDuplicateStart():{}", new Object[]{this.PACKET_IDENTIFIER, e, e});
            return false;
        }
    }

    public boolean isDupliacteStart(Connection conn, String userName, String NasIPAddress, String framedip) {
        log.debug("{} IN isDupliacteStart() userName:{},NasIPAddress:{},framedip:{} QUERY {}", new Object[]{this.PACKET_IDENTIFIER, userName, NasIPAddress, framedip, strDuplicateStartCheck});
        boolean recordExist = false;

        try (PreparedStatement stmt = conn.prepareStatement(strDuplicateStartCheck)) {
            stmt.setString(1, userName);
            stmt.setString(2, NasIPAddress);
            stmt.setString(3, framedip);
            try (ResultSet rset = stmt.executeQuery()) {
                recordExist = rset.next();
            }

            log.info("{} IN isDuplicateStart():{}", this.PACKET_IDENTIFIER, recordExist);
        } catch (SQLException e) {
            log.error("{} IN isDuplicateStart():{}", new Object[]{this.PACKET_IDENTIFIER, e, e});
        }

        return recordExist;
    }

    public CDRData getLiveUserSessionDetails(String acctSessionId) {
        log.debug(this.PACKET_IDENTIFIER + "IN getLiveUserDetails acctSessionId:" + acctSessionId + " QUERY " + strLiveSessionSelect);
        double totalActualBytes = 0.0;
        double totalActualTime = 0.0;
        CDRData cdrData = null;

        try (Connection conn = DataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(strLiveSessionSelect)) {
            stmt.setString(1, acctSessionId);
            try (ResultSet rset = stmt.executeQuery()) {
                log.debug(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Result Set is " + rset);
                if (rset.next()) {
                    cdrData = new CDRData();

                    try {
                        totalActualBytes += Double.parseDouble(rset.getString("AcctInputOctets"));
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Acct Input Octet not found");
                    }

                    try {
                        totalActualBytes += Double.parseDouble(rset.getString("AcctOutputOctets"));
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Acct Output Octet not found");
                    }

                    try {
                        if (rset.getString("AcctInputGigawords") != null) {
                            long inputGigawords = Long.parseLong(rset.getString("AcctInputGigawords"));
                            totalActualBytes += (double) (inputGigawords * 1024L * 1024L * 1024L);
                        } else {
                            log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Input Gigawords not found");
                        }
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Input Gigawords not found");
                    }

                    try {
                        if (rset.getString("AcctOutputGigawords") != null) {
                            long outputGigawords = Long.parseLong(rset.getString("AcctOutputGigawords"));
                            totalActualBytes += (double) (outputGigawords * 1024L * 1024L * 1024L);
                        } else {
                            log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Output Gigawords not found");
                        }
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Output Gigawords not found");
                    }

                    try {
                        totalActualTime = Double.parseDouble(rset.getString("AcctSessionTime"));
                    } catch (Exception e) {
                        log.info(this.PACKET_IDENTIFIER + "IN getLiveUserDetails Acct Session Time not found");
                    }

                    cdrData.setNasip(rset.getString("nasipaddress"));
                    cdrData.setSessionid(rset.getString("acctsessionid"));
                    cdrData.setUsername(rset.getString("username"));
                    cdrData.setFramedip(rset.getString("framedipaddress"));
                    cdrData.setTotalActualBytes(totalActualBytes);
                    cdrData.setTotalActualTime(totalActualTime);

                    try {
                        cdrData.setCreatedate(rset.getString(AAAConstant.CREATE_DATE));
                    } catch (Exception e) {
                        // the column is optional
                    }

                    try {
                        cdrData.setLastmodifiedddate(rset.getString(AAAConstant.LAST_MODIFIED_DATE));
                    } catch (Exception e) {
                        // the column is optional
                    }
                } else {
                    log.debug(this.PACKET_IDENTIFIER + "IN getLiveUserDetails() acctSessionId:" + acctSessionId + ", NOT FOUND");
                }
            }
        } catch (Exception e) {
            log.error(this.PACKET_IDENTIFIER + "Error in getLiveUserDetails():" + e, e);
        }

        return cdrData;
    }
}
