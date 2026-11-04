/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.server;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.AcctProfileData;
import com.radiusengine.aaa.data.ReplyItemData;
import com.radiusengine.aaa.data.ServerConfData;
import com.radiusengine.aaa.data.clientData;
import com.radiusengine.aaa.db.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingConfigurations {
    Logger log = LoggerFactory.getLogger("radiuslog");
    private static volatile CachingConfigurations SINGLE_INSTANCE = null;
    private static CacheManager cacheManagerData = CacheManager.getInstance();
    private static String strClientQuery = "select clientip,clientid,sharedkey,timeout,cl.clientgroupid,name,iptype from tblclients cl ,tblclientgroup clg where cl.clientgroupid=clg.clientgroupid and cl.is_delete=0";
    private static String strAcctProfile = "select name,status,checkitem,accountcdrstatus,sessionstatus,mappingmasterid,priority from tblacctprofile where is_delete=0 order by priority";
    private static String strDBCDRMapping = "select mappingmasterid,radiusname,dbcolumnname from tbldbmapping where mappingmasterid=?";
    private static String strClientReplyQuery = "select attribute,attributevalue from tblradiusclientreply where clientid=?";
    private static String strRadiusProfileQuery = "select radiusprofileid,name from tblradiusprofile where is_delete=0";
    private static String strRadiusProfileCheckQuery = "select radiuscheckitmid,checkitem from tblradiusprofilecheckitm where radiusprofileid=? and is_deleted=0";
    private static String strRadiusProfileReplyQuery = "select  attribute,attributevalue from tblradiusprofilereplyitm where radiuscheckitmid=?";
    private static String strRadiusServerAttributeQuery = "select  * from tblserverconf";

    private static final String[] CACHE_NAMES = {
        AAAConstant.SERVERCACHING,
        AAAConstant.CLIENTCACHING,
        AAAConstant.CLIENTREPLY,
        AAAConstant.ACCTPROFILECONFIG,
        AAAConstant.GROUPPOLICY,
        AAAConstant.SERVERATTRIBUTE,
        AAAConstant.SERVERCUSTATTRIBUTE
    };

    /** config.properties keys that are copied as they are into the SERVERCACHING cache. */
    private static final String[] SERVER_CONFIG_KEYS = {
        AAAConstant.AUTHPORT,
        AAAConstant.ACCTPORT,
        AAAConstant.SERVERIP,
        AAAConstant.WEBSERVICEIP,
        AAAConstant.WEBSERVICEPORT,
        AAAConstant.COAPORT,
        "coaOperation",
        "dataServiceId",
        AAAConstant.IP_CHARGE_CATEGORY,
        AAAConstant.BASE_PLAN_GROUP,
        AAAConstant.SESSION_THRESHOLD,
        AAAConstant.AUTH_MIN_THREAD,
        AAAConstant.AUTH_MAX_THREAD,
        AAAConstant.ACCT_MIN_THREAD,
        AAAConstant.ACCT_MAX_THREAD,
        AAAConstant.THREAD_BENCH_TIME,
        AAAConstant.ACCEPT_ALL,
        AAAConstant.CONVERT_STOP_TO_X,
        AAAConstant.STOP_TO_X_METHOD,
        AAAConstant.SEND_COA,
        AAAConstant.ENABLE_RESP_THREAD_POOL,
        AAAConstant.AUTH_RESP_MIN_THREAD,
        AAAConstant.AUTH_RESP_MAX_THREAD,
        AAAConstant.ACCT_RESP_MIN_THREAD,
        AAAConstant.ACCT_RESP_MAX_THREAD,
        AAAConstant.COA_MAX_THREAD,
        AAAConstant.COA_MIN_THREAD,
        AAAConstant.COA_TIMEOUT,
        AAAConstant.COA_RETRY,
        AAAConstant.COA_SLEEPTIME,
        AAAConstant.RADIUS_AUTH_AUDIT_MIN_THREAD,
        AAAConstant.RADIUS_AUTH_AUDIT_MAX_THREAD,
        AAAConstant.RECORD_CDR_MIN_THREAD,
        AAAConstant.RECORD_CDR_MAX_THREAD,
        AAAConstant.RECORD_ACCTSTART_MIN_THREAD,
        AAAConstant.RECORD_ACCTSTART_MAX_THREAD,
        AAAConstant.ENABLE_DUPLICATE_START_CHECK,
        AAAConstant.COMMONPOOL_MIN_THREAD,
        AAAConstant.COMMONPOOL_MAX_THREAD
    };

    private CachingConfigurations() {
        try {
            for (String cacheName : CACHE_NAMES) {
                CacheConfiguration cacheConfiguration = (new CacheConfiguration()).name(cacheName).eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
                cacheManagerData.addCache(new Cache(cacheConfiguration));
            }

            Properties prop = this.loadConfigProperties();
            if (prop == null) {
                this.log.warn("Sorry, unable to find config.properties");
                return;
            }

            this.log.debug(prop.getProperty(getKey()));
            this.log.debug(prop.getProperty("db.user"));
            Cache cacheServer = getCacheManagerData().getCache(AAAConstant.SERVERCACHING);
            for (String key : SERVER_CONFIG_KEYS) {
                cacheServer.put(new Element(key, prop.getProperty(key)));
            }

            this.log.debug("AAA SERVER IS RUNNING ON" + prop.getProperty(AAAConstant.SERVERIP) + ":AUTH:" + prop.getProperty(AAAConstant.AUTHPORT) + ":ACCT:" + prop.getProperty(AAAConstant.ACCTPORT));
            DataSource.setupDataSource(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"),
                    prop.getProperty(AAAConstant.HIKARI_THPOOL_CACHE_PREPSTMT), prop.getProperty(AAAConstant.HIKARI_THPOOL_PRESTMT_CACHE_SIZE), prop.getProperty(AAAConstant.HIKARI_THPOOL_PRESTMT_CACHE_SQL_LIMIT),
                    Integer.parseInt(prop.getProperty(AAAConstant.HIKARI_THPOOL_MIN_IDLE_CONN)), Integer.parseInt(prop.getProperty(AAAConstant.HIKARI_THPOOL_MAX_CONN_POOLSIZE)));
            this.loadDatabaseCaches();
        } catch (Exception e) {
            this.log.error("Error in CacheCOnfiguration Constructor:" + e, e);
        }

    }

    /** Loads config.properties from the class path; returns null when the file is not there. */
    private Properties loadConfigProperties() throws IOException {
        try (InputStream input = CachingConfigurations.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                return null;
            }

            Properties prop = new Properties();
            prop.load(input);
            return prop;
        }
    }

    private static String getKey() {
        return "db.url";
    }

    public void reloadConfiguration() {
        try {
            this.loadDatabaseCaches();
        } catch (Exception e) {
            this.log.error("Error in reloadConfiguration():" + e, e);
        }

    }

    /** Fills the client, accounting profile, radius profile and server attribute caches from the database. */
    private void loadDatabaseCaches() {
        Cache cacheClient = getCacheManagerData().getCache(AAAConstant.CLIENTCACHING);
        Cache cacheClientReply = getCacheManagerData().getCache(AAAConstant.CLIENTREPLY);
        this.cacheClient(cacheClient, cacheClientReply);
        Cache cacheAcctProfile = getCacheManagerData().getCache(AAAConstant.ACCTPROFILECONFIG);
        this.cacheAcctProfile(cacheAcctProfile);
        Cache cacheGroup = getCacheManagerData().getCache(AAAConstant.GROUPPOLICY);
        this.cacheRadiusProfile(cacheGroup);
        Cache cacheAttributeServer = getCacheManagerData().getCache(AAAConstant.SERVERATTRIBUTE);
        Cache cacheCustAttributeServer = getCacheManagerData().getCache(AAAConstant.SERVERCUSTATTRIBUTE);
        this.cacheServerAttribe(cacheAttributeServer, cacheCustAttributeServer);
    }

    private void cacheClient(Cache cacheClient, Cache cacheClientReply) {
        try (Connection conn = DataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(strClientQuery);
             ResultSet rset = stmt.executeQuery()) {
            this.log.debug("Executing : " + strClientQuery);

            while (rset.next()) {
                clientData clientData = new clientData();
                clientData.setClientip(rset.getString(AAAConstant.CLIENTIP));
                clientData.setSharedkey(rset.getString(AAAConstant.SHAREDKEY));
                clientData.setGroupid(rset.getInt("clientgroupid"));
                clientData.setTimeout(rset.getString(AAAConstant.CLIENTTIMEOUT));
                clientData.setGroupname(rset.getString("name"));
                clientData.setIptype(rset.getString("iptype"));
                ConcurrentHashMap<String, String> clientReply = new ConcurrentHashMap<>();

                try (PreparedStatement stmt1 = conn.prepareStatement(strClientReplyQuery)) {
                    stmt1.setInt(1, rset.getInt(AAAConstant.CLIENTID));

                    try (ResultSet rset1 = stmt1.executeQuery()) {
                        while (rset1.next()) {
                            clientReply.put(rset1.getString("attribute"), rset1.getString("attributevalue"));
                        }
                    }
                }

                cacheClientReply.put(new Element(rset.getString(AAAConstant.CLIENTIP), clientReply));
                cacheClient.put(new Element(rset.getString(AAAConstant.CLIENTIP), clientData));
                this.log.debug("AAA CLIENT ARE" + rset.getString(AAAConstant.CLIENTIP) + ":DATA:" + clientReply + ":");
            }
        } catch (Exception e) {
            this.log.error("Error in Cacheclient():" + e, e);
        }

    }

    private void cacheAcctProfile(Cache cacheAcctProfile) {
        try (Connection conn = DataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(strAcctProfile);
             ResultSet rset = stmt.executeQuery()) {
            this.log.debug("Executing : " + strAcctProfile);
            ConcurrentHashMap<Integer, AcctProfileData> acctProfileMap = new ConcurrentHashMap<>();

            while (rset.next()) {
                ConcurrentHashMap<String, String> dbFieldMapping = new ConcurrentHashMap<>();
                AcctProfileData acctProfileData = new AcctProfileData();
                acctProfileData.setName(rset.getString("name"));
                acctProfileData.setCheckitem(rset.getString("checkitem"));
                acctProfileData.setAccountcdrstatus(rset.getString("accountcdrstatus"));
                acctProfileData.setMappingmasterid(rset.getInt("mappingmasterid"));
                acctProfileData.setPriority(rset.getInt("priority"));
                acctProfileData.setSessionstatus(rset.getString("sessionstatus"));

                try (PreparedStatement stmt1 = conn.prepareStatement(strDBCDRMapping)) {
                    this.log.debug("Executing : " + strDBCDRMapping + "For Id" + rset.getInt("mappingmasterid"));
                    stmt1.setInt(1, rset.getInt("mappingmasterid"));

                    try (ResultSet rset1 = stmt1.executeQuery()) {
                        while (rset1.next()) {
                            dbFieldMapping.put(rset1.getString(AAAConstant.RADIUSNAME), rset1.getString(AAAConstant.DBNAMECOL));
                        }
                    }
                }

                acctProfileData.setDbFieldMapping(dbFieldMapping);
                acctProfileMap.put(rset.getInt("priority"), acctProfileData);
            }

            cacheAcctProfile.put(new Element(AAAConstant.ACCTPROFILECACHE, acctProfileMap));
        } catch (Exception e) {
            this.log.error("Error in cacheAcctProfile():" + e, e);
        }

    }

    private void cacheRadiusProfile(Cache cacheGroup) {
        try (Connection conn = DataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(strRadiusProfileQuery);
             ResultSet rset = stmt.executeQuery()) {
            this.log.debug("Executing : " + strRadiusProfileQuery);

            while (rset.next()) {
                ConcurrentHashMap<String, ConcurrentHashMap<Integer, ReplyItemData>> profileCheck = new ConcurrentHashMap<>();
                this.log.debug("Executed radiusprofileid:" + rset.getInt("radiusprofileid"));

                try (PreparedStatement stmt1 = conn.prepareStatement(strRadiusProfileCheckQuery)) {
                    this.log.debug("Executing : " + strRadiusProfileCheckQuery);
                    stmt1.setInt(1, rset.getInt("radiusprofileid"));

                    try (ResultSet rset1 = stmt1.executeQuery()) {
                        while (rset1.next()) {
                            ConcurrentHashMap<Integer, ReplyItemData> profileReply = new ConcurrentHashMap<>();
                            this.log.debug("Executed radiuscheckitmid:" + rset1.getInt("radiuscheckitmid"));
                            this.log.debug("Executing : " + strRadiusProfileReplyQuery);

                            try (PreparedStatement stmt2 = conn.prepareStatement(strRadiusProfileReplyQuery)) {
                                stmt2.setInt(1, rset1.getInt("radiuscheckitmid"));

                                try (ResultSet rset2 = stmt2.executeQuery()) {
                                    for (int i = 0; rset2.next(); ++i) {
                                        this.log.debug("Executed attribute:" + rset2.getString("attribute"));
                                        ReplyItemData replyItem = new ReplyItemData();
                                        replyItem.setAttribute(rset2.getString("attribute"));
                                        replyItem.setAttributeValue(rset2.getString("attributevalue"));
                                        profileReply.put(i, replyItem);
                                    }
                                }
                            }

                            profileCheck.put(rset1.getString("checkitem"), profileReply);
                            this.log.debug("SET Map for :" + rset1.getString("checkitem") + ":DATA:" + profileReply);
                        }
                    }
                }

                this.log.debug("SET Map for :" + rset.getString("radiusprofileid") + ":DATA:" + profileCheck);
                cacheGroup.put(new Element(rset.getInt("radiusprofileid"), profileCheck));
                this.log.debug("RADIUS PROFILE IS " + rset.getInt("radiusprofileid") + ":DATA:" + profileCheck + ":");
            }
        } catch (Exception e) {
            this.log.error("Error in cacheRadiusProfile():" + e, e);
        }

    }

    private void cacheServerAttribe(Cache cacheServerArrib, Cache cacheCustServerArrib) {
        try (Connection conn = DataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(strRadiusServerAttributeQuery);
             ResultSet rset = stmt.executeQuery()) {
            this.log.debug("Executing : " + strRadiusServerAttributeQuery);

            while (rset.next()) {
                cacheServerArrib.put(new Element(rset.getString("attributename"), rset.getString("attributevalue")));
                String strCustSpecifiAttribute = rset.getString("prsv_with_cust");
                if (strCustSpecifiAttribute != null && !"".equalsIgnoreCase(strCustSpecifiAttribute) && "Y".equalsIgnoreCase(strCustSpecifiAttribute)) {
                    ServerConfData confData = new ServerConfData();
                    confData.setAttributeName(rset.getString("attributename"));
                    confData.setAttributeValue(rset.getString("attributevalue"));
                    confData.setPreservWithCustomer(rset.getString("prsv_with_cust"));
                    confData.setEnsureUnique(rset.getString("ensure_unique"));
                    cacheCustServerArrib.put(new Element(rset.getString("attributename"), confData));
                }
            }
        } catch (Exception e) {
            this.log.error("Error in cacheServerAttribe():" + e, e);
        }

    }

    public static CachingConfigurations getInstance() {
        if (SINGLE_INSTANCE == null) {
            synchronized (CachingConfigurations.class) {
                if (SINGLE_INSTANCE == null) {
                    SINGLE_INSTANCE = new CachingConfigurations();
                }
            }
        }

        return SINGLE_INSTANCE;
    }

    public static CacheManager getCacheManagerData() {
        return cacheManagerData;
    }

    public static Object getCachedValue(String cacheName, String paramName) {
        return getCacheManagerData().getCache(cacheName).get(paramName).getObjectValue();
    }
}
