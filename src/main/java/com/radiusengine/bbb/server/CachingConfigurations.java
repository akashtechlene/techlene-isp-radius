/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.bbb.server;

import com.radiusengine.bbb.data.AcctProfileData;
import com.radiusengine.bbb.data.ReplyItemData;
import com.radiusengine.bbb.data.ServerConfData;
import com.radiusengine.bbb.data.clientData;
import com.radiusengine.bbb.db.DataSource;
import com.radiusengine.bbb.constant.AAAConstant;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class CachingConfigurations {
    Logger log = LoggerFactory.getLogger("radiuslog");
    private static com.radiusengine.bbb.server.CachingConfigurations SINGLE_INSTANCE = null;
    private static CacheManager cacheManagerData = CacheManager.getInstance();
    private static String strClientQuery = "select clientip,clientid,sharedkey,timeout,cl.clientgroupid,name,iptype from tblclients cl ,tblclientgroup clg where cl.clientgroupid=clg.clientgroupid and cl.is_delete=0";
    private static String strAcctProfile = "select name,status,checkitem,accountcdrstatus,sessionstatus,mappingmasterid,priority from tblacctprofile where is_delete=0 order by priority";
    private static String strDBCDRMapping = "select mappingmasterid,radiusname,dbcolumnname from tbldbmapping where mappingmasterid=?";
    private static String strClientReplyQuery = "select attribute,attributevalue from tblradiusclientreply where clientid=?";
    private static String strRadiusProfileQuery = "select radiusprofileid,name from tblradiusprofile where is_delete=0";
    private static String strRadiusProfileCheckQuery = "select radiuscheckitmid,checkitem from tblradiusprofilecheckitm where radiusprofileid=? and is_deleted=0";
    private static String strRadiusProfileReplyQuery = "select  attribute,attributevalue from tblradiusprofilereplyitm where radiuscheckitmid=?";
    private static String strRadiusServerAttributeQuery = "select  * from tblserverconf";

    private CachingConfigurations() {
        try {
            CacheConfiguration cacheConfigurationSer = (new CacheConfiguration()).name("serverConfig").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheConfigurationSer));
            CacheConfiguration cacheConfigurationClient = (new CacheConfiguration()).name("clientConfig").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheConfigurationClient));
            CacheConfiguration cacheConfigurationClientReply = (new CacheConfiguration()).name("clientReply").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheConfigurationClientReply));
            CacheConfiguration cacheAcctProrfile = (new CacheConfiguration()).name("acctprofile").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheAcctProrfile));
            CacheConfiguration cacheConfigurationGroupPolicy = (new CacheConfiguration()).name("grouppolicy").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheConfigurationGroupPolicy));
            CacheConfiguration cacheConfAttributeServer = (new CacheConfiguration()).name("serverAttribe").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheConfAttributeServer));
            CacheConfiguration cacheConfCustAttributeServer = (new CacheConfiguration()).name("serverCustomerAttribe").eternal(true).overflowToDisk(true).maxEntriesLocalHeap(15000);
            cacheManagerData.addCache(new Cache(cacheConfCustAttributeServer));
            InputStream input = com.radiusengine.bbb.server.CachingConfigurations.class.getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
            if (input == null) {
                this.log.warn("Sorry, unable to find config.properties");
                return;
            }

            prop.load(input);
            this.log.debug(prop.getProperty("db.url"));
            this.log.debug(prop.getProperty("db.user"));
            Cache cacheServer = getCacheManagerData().getCache("serverConfig");
            cacheServer.put(new Element("authPort", prop.getProperty("authPort")));
            cacheServer.put(new Element("acctPort", prop.getProperty("acctPort")));
            cacheServer.put(new Element("serverip", prop.getProperty("serverip")));
            cacheServer.put(new Element("webserviceIP", prop.getProperty("webserviceIP")));
            cacheServer.put(new Element("webservicePort", prop.getProperty("webservicePort")));
            cacheServer.put(new Element("coaPort", prop.getProperty("coaPort")));
            cacheServer.put(new Element("coaOperation", prop.getProperty("coaOperation")));
            cacheServer.put(new Element("dataServiceId", prop.getProperty("dataServiceId")));
            cacheServer.put(new Element("ipchargecategory", prop.getProperty("ipchargecategory")));
            cacheServer.put(new Element("baseplangroup", prop.getProperty("baseplangroup")));
            cacheServer.put(new Element("sessionthresold", prop.getProperty("sessionthresold")));
            cacheServer.put(new Element("authMinThreads", prop.getProperty("authMinThreads")));
            cacheServer.put(new Element("authMaxThreads", prop.getProperty("authMaxThreads")));
            cacheServer.put(new Element("acctMinThreads", prop.getProperty("acctMinThreads")));
            cacheServer.put(new Element("acctMaxThreads", prop.getProperty("acctMaxThreads")));
            cacheServer.put(new Element("threadBenchTime", prop.getProperty("threadBenchTime")));
            cacheServer.put(new Element("accept_all", prop.getProperty("accept_all")));
            cacheServer.put(new Element("convert_stop_to_x", prop.getProperty("convert_stop_to_x")));
            cacheServer.put(new Element("stop_to_x_method", prop.getProperty("stop_to_x_method")));
            cacheServer.put(new Element("SEND_COA", prop.getProperty("SEND_COA")));
            cacheServer.put(new Element("enable_resp_thread_pool", prop.getProperty("enable_resp_thread_pool")));
            cacheServer.put(new Element("authRespMinThreads", prop.getProperty("authRespMinThreads")));
            cacheServer.put(new Element("authRespMaxThreads", prop.getProperty("authRespMaxThreads")));
            cacheServer.put(new Element("acctRespMinThreads", prop.getProperty("acctRespMinThreads")));
            cacheServer.put(new Element("acctRespMaxThreads", prop.getProperty("acctRespMaxThreads")));
            cacheServer.put(new Element("coaMaxThreads", prop.getProperty("coaMaxThreads")));
            cacheServer.put(new Element("coaMinThreads", prop.getProperty("coaMinThreads")));
            cacheServer.put(new Element("coaTimeout", prop.getProperty("coaTimeout")));
            cacheServer.put(new Element("coaRetry", prop.getProperty("coaRetry")));
            cacheServer.put(new Element("coaSleepTime", prop.getProperty("coaSleepTime")));
            cacheServer.put(new Element("authAuditMinThreads", prop.getProperty("authAuditMinThreads")));
            cacheServer.put(new Element("authAuditMaxThreads", prop.getProperty("authAuditMaxThreads")));
            cacheServer.put(new Element("recordCDRMinThreads", prop.getProperty("recordCDRMinThreads")));
            cacheServer.put(new Element("recordCDRMaxThreads", prop.getProperty("recordCDRMaxThreads")));
            cacheServer.put(new Element("recordAcctStartMinThreads", prop.getProperty("recordAcctStartMinThreads")));
            cacheServer.put(new Element("recordAcctStartMaxThreads", prop.getProperty("recordAcctStartMaxThreads")));
            cacheServer.put(new Element("enable_duplicate_start_check", prop.getProperty("enable_duplicate_start_check")));
            cacheServer.put(new Element("commonPoolMinThreads", prop.getProperty("commonPoolMinThreads")));
            cacheServer.put(new Element("commonPoolMaxThreads", prop.getProperty("commonPoolMaxThreads")));
            this.log.debug("AAA SERVER IS RUNNING ON" + prop.getProperty("serverip") + ":AUTH:" + prop.getProperty("authPort") + ":ACCT:" + prop.getProperty("acctPort"));
            com.radiusengine.bbb.db.DataSource.setupDataSource(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"), prop.getProperty("hikari_cachePrepStmts"), prop.getProperty("hikari_prepStmtCacheSize"), prop.getProperty("hikari_prepStmtCacheSqlLimit"), Integer.parseInt(prop.getProperty("hikari_mini_idle_conn")), Integer.parseInt(prop.getProperty("hikari_max_conn_poolsize")));
            Cache cacheClient = getCacheManagerData().getCache("clientConfig");
            Cache cacheClientReply = getCacheManagerData().getCache("clientReply");
            this.cacheClient(cacheClient, cacheClientReply);
            Cache cacheAcctProfile = getCacheManagerData().getCache("acctprofile");
            this.cacheAcctProfile(cacheAcctProfile);
            Cache cacheGroup = getCacheManagerData().getCache("grouppolicy");
            this.cacheRadiusProfile(cacheGroup);
            Cache cacheAttributeServer = getCacheManagerData().getCache("serverAttribe");
            Cache cacheCustAttributeServer = getCacheManagerData().getCache("serverCustomerAttribe");
            this.cacheServerAttribe(cacheAttributeServer, cacheCustAttributeServer);
        } catch (Exception var17) {
            Exception e = var17;
            this.log.error("Error in CacheCOnfiguration Constructor:" + e, e);
        }

    }

    public void reloadConfiguration() {
        try {
            Cache cacheClient = getCacheManagerData().getCache("clientConfig");
            Cache cacheClientReply = getCacheManagerData().getCache("clientReply");
            this.cacheClient(cacheClient, cacheClientReply);
            Cache cacheAcctProfile = getCacheManagerData().getCache("acctprofile");
            this.cacheAcctProfile(cacheAcctProfile);
            Cache cacheGroup = getCacheManagerData().getCache("grouppolicy");
            this.cacheRadiusProfile(cacheGroup);
            Cache cacheAttributeServer = getCacheManagerData().getCache("serverAttribe");
            Cache cacheCustomerAttributeServer = getCacheManagerData().getCache("serverCustomerAttribe");
            this.cacheServerAttribe(cacheAttributeServer, cacheCustomerAttributeServer);
        } catch (Exception var7) {
            Exception e = var7;
            this.log.error("Error in reloadConfiguration():" + e, e);
        }

    }

    private void cacheClient(Cache cacheClient, Cache cacheClientReply) {
        Connection conn = null;
        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        ResultSet rset = null;
        ResultSet rset1 = null;
        ResultSet rset2 = null;

        try {
            if (rset != null) {
                rset.close();
            }

            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }

            conn = com.radiusengine.bbb.db.DataSource.getConnection();
            stmt = conn.prepareStatement(strClientQuery);
            rset = stmt.executeQuery();
            this.log.debug("Executing : " + strClientQuery);

            while(rset.next()) {
                if (rset1 != null) {
                    rset1.close();
                }

                if (stmt1 != null) {
                    stmt1.close();
                }

                if (conn1 != null) {
                    conn1.close();
                }

                com.radiusengine.bbb.data.clientData clientData = new clientData();
                clientData.setClientip(rset.getString("clientip"));
                clientData.setSharedkey(rset.getString("sharedkey"));
                clientData.setGroupid(rset.getInt("clientgroupid"));
                clientData.setTimeout(rset.getString("timeout"));
                clientData.setGroupname(rset.getString("name"));
                clientData.setIptype(rset.getString("iptype"));
                conn1 = com.radiusengine.bbb.db.DataSource.getConnection();
                stmt1 = conn1.prepareStatement(strClientReplyQuery);
                stmt1.setInt(1, rset.getInt("clientid"));
                rset1 = stmt1.executeQuery();
                ConcurrentHashMap<String, String> clientReply = new ConcurrentHashMap();

                while(rset1.next()) {
                    clientReply.put(rset1.getString("attribute"), rset1.getString("attributevalue"));
                }

                cacheClientReply.put(new Element(rset.getString("clientip"), clientReply));
                cacheClient.put(new Element(rset.getString("clientip"), clientData));
                this.log.debug("AAA CLIENT ARE" + rset.getString("clientip") + ":DATA:" + clientReply + ":");
            }
        } catch (Exception var54) {
            Exception e = var54;
            this.log.error("Error in Cacheclient():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var53) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var52) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var51) {
            }

            try {
                if (rset1 != null) {
                    rset1.close();
                }
            } catch (Exception var50) {
            }

            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception var49) {
            }

            try {
                if (conn1 != null) {
                    conn1.close();
                }
            } catch (Exception var48) {
            }

            try {
                if (rset2 != null) {
                    ((ResultSet)rset2).close();
                }
            } catch (Exception var47) {
            }

            try {
                if (stmt2 != null) {
                    ((PreparedStatement)stmt2).close();
                }
            } catch (Exception var46) {
            }

            try {
                if (conn2 != null) {
                    ((Connection)conn2).close();
                }
            } catch (Exception var45) {
            }

        }

    }

    private void cacheAcctProfile(Cache cacheAcctProfile) {
        Connection conn = null;
        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        ResultSet rset = null;
        ResultSet rset1 = null;
        ResultSet rset2 = null;

        try {
            if (rset != null) {
                rset.close();
            }

            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }

            conn = com.radiusengine.bbb.db.DataSource.getConnection();
            stmt = conn.prepareStatement(strAcctProfile);
            rset = stmt.executeQuery();
            this.log.debug("Executing : " + strAcctProfile);
            ConcurrentHashMap acctProfileMap = new ConcurrentHashMap();

            while(rset.next()) {
                ConcurrentHashMap dbFieldMapping = new ConcurrentHashMap();
                com.radiusengine.bbb.data.AcctProfileData acctProfileData = new AcctProfileData();
                acctProfileData.setName(rset.getString("name"));
                acctProfileData.setCheckitem(rset.getString("checkitem"));
                acctProfileData.setAccountcdrstatus(rset.getString("accountcdrstatus"));
                acctProfileData.setMappingmasterid(rset.getInt("mappingmasterid"));
                acctProfileData.setPriority(rset.getInt("priority"));
                acctProfileData.setSessionstatus(rset.getString("sessionstatus"));
                if (rset1 != null) {
                    rset1.close();
                }

                if (stmt1 != null) {
                    stmt1.close();
                }

                if (conn1 != null) {
                    conn1.close();
                }

                conn1 = com.radiusengine.bbb.db.DataSource.getConnection();
                stmt1 = conn1.prepareStatement(strDBCDRMapping);
                this.log.debug("Executing : " + strDBCDRMapping + "For Id" + rset.getInt("mappingmasterid"));
                stmt1.setInt(1, rset.getInt("mappingmasterid"));
                rset1 = stmt1.executeQuery();

                while(rset1.next()) {
                    dbFieldMapping.put(rset1.getString("radiusname"), rset1.getString("dbcolumnname"));
                }

                acctProfileData.setDbFieldMapping(dbFieldMapping);
                acctProfileMap.put(rset.getInt("priority"), acctProfileData);
            }

            cacheAcctProfile.put(new Element("acctprofilecache", acctProfileMap));
        } catch (Exception var54) {
            Exception e = var54;
            this.log.error("Error in cacheAcctProfile():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var53) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var52) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var51) {
            }

            try {
                if (rset1 != null) {
                    rset1.close();
                }
            } catch (Exception var50) {
            }

            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception var49) {
            }

            try {
                if (conn1 != null) {
                    conn1.close();
                }
            } catch (Exception var48) {
            }

            try {
                if (rset2 != null) {
                    ((ResultSet)rset2).close();
                }
            } catch (Exception var47) {
            }

            try {
                if (stmt2 != null) {
                    ((PreparedStatement)stmt2).close();
                }
            } catch (Exception var46) {
            }

            try {
                if (conn2 != null) {
                    ((Connection)conn2).close();
                }
            } catch (Exception var45) {
            }

        }

    }

    private void cacheRadiusProfile(Cache cacheGroup) {
        Connection conn = null;
        Connection conn1 = null;
        Connection conn2 = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        ResultSet rset = null;
        ResultSet rset1 = null;
        ResultSet rset2 = null;

        try {
            if (rset != null) {
                rset.close();
            }

            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }

            conn = com.radiusengine.bbb.db.DataSource.getConnection();
            stmt = conn.prepareStatement(strRadiusProfileQuery);
            rset = stmt.executeQuery();
            this.log.debug("Executing : " + strRadiusProfileQuery);

            while(rset.next()) {
                ConcurrentHashMap profileCheck = new ConcurrentHashMap();
                this.log.debug("Executed radiusprofileid:" + rset.getInt("radiusprofileid"));
                if (rset1 != null) {
                    rset1.close();
                }

                if (stmt1 != null) {
                    stmt1.close();
                }

                if (conn1 != null) {
                    conn1.close();
                }

                conn1 = com.radiusengine.bbb.db.DataSource.getConnection();
                stmt1 = conn1.prepareStatement(strRadiusProfileCheckQuery);
                this.log.debug("Executing : " + strRadiusProfileCheckQuery);
                stmt1.setInt(1, rset.getInt("radiusprofileid"));
                rset1 = stmt1.executeQuery();

                while(rset1.next()) {
                    if (rset2 != null) {
                        rset2.close();
                    }

                    if (stmt2 != null) {
                        stmt2.close();
                    }

                    if (conn2 != null) {
                        conn2.close();
                    }

                    ConcurrentHashMap profileReply = new ConcurrentHashMap();
                    this.log.debug("Executed radiuscheckitmid:" + rset1.getInt("radiuscheckitmid"));
                    this.log.debug("Executing : " + strRadiusProfileReplyQuery);
                    conn2 = com.radiusengine.bbb.db.DataSource.getConnection();
                    this.log.debug("Got Connection");
                    stmt2 = conn2.prepareStatement(strRadiusProfileReplyQuery);
                    stmt2.setInt(1, rset1.getInt("radiuscheckitmid"));
                    rset2 = stmt2.executeQuery();

                    for(int i = 0; rset2.next(); ++i) {
                        this.log.debug("Executed attribute:" + rset2.getString("attribute"));
                        com.radiusengine.bbb.data.ReplyItemData replyItem = new ReplyItemData();
                        replyItem.setAttribute(rset2.getString("attribute"));
                        replyItem.setAttributeValue(rset2.getString("attributevalue"));
                        profileReply.put(i, replyItem);
                    }

                    profileCheck.put(rset1.getString("checkitem"), profileReply);
                    this.log.debug("SET Map for :" + rset1.getString("checkitem") + ":DATA:" + profileReply);
                }

                this.log.debug("SET Map for :" + rset.getString("radiusprofileid") + ":DATA:" + profileCheck);
                cacheGroup.put(new Element(rset.getInt("radiusprofileid"), profileCheck));
                this.log.debug("RADIUS PROFILE IS " + rset.getInt("radiusprofileid") + ":DATA:" + profileCheck + ":");
            }
        } catch (Exception var55) {
            Exception e = var55;
            this.log.error("Error in cacheRadiusProfile():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var54) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var53) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var52) {
            }

            try {
                if (rset1 != null) {
                    rset1.close();
                }
            } catch (Exception var51) {
            }

            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception var50) {
            }

            try {
                if (conn1 != null) {
                    conn1.close();
                }
            } catch (Exception var49) {
            }

            try {
                if (rset2 != null) {
                    rset2.close();
                }
            } catch (Exception var48) {
            }

            try {
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (Exception var47) {
            }

            try {
                if (conn2 != null) {
                    conn2.close();
                }
            } catch (Exception var46) {
            }

        }

    }

    private void cacheServerAttribe(Cache cacheServerArrib, Cache cacheCustServerArrib) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            if (rset != null) {
                rset.close();
            }

            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }

            conn = DataSource.getConnection();
            stmt = conn.prepareStatement(strRadiusServerAttributeQuery);
            rset = stmt.executeQuery();
            this.log.debug("Executing : " + strRadiusServerAttributeQuery);
            String strCustSpecifiAttribute = "N";
            com.radiusengine.bbb.data.ServerConfData confData = null;

            while(rset.next()) {
                strCustSpecifiAttribute = "N";
                cacheServerArrib.put(new Element(rset.getString("attributename"), rset.getString("attributevalue")));
                strCustSpecifiAttribute = rset.getString("prsv_with_cust");
                if (strCustSpecifiAttribute != null && !"".equalsIgnoreCase(strCustSpecifiAttribute) && "Y".equalsIgnoreCase(strCustSpecifiAttribute)) {
                    confData = new ServerConfData();
                    confData.setAttributeName(rset.getString("attributename"));
                    confData.setAttributeValue(rset.getString("attributevalue"));
                    confData.setPreservWithCustomer(rset.getString("prsv_with_cust"));
                    confData.setEnsureUnique(rset.getString("ensure_unique"));
                    cacheCustServerArrib.put(new Element(rset.getString("attributename"), confData));
                }
            }
        } catch (Exception var24) {
            Exception e = var24;
            this.log.error("Error in cacheServerAttribe():" + e, e);
        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (Exception var23) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception var22) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var21) {
            }

        }

    }

    public static com.radiusengine.bbb.server.CachingConfigurations getInstance() {
        if (SINGLE_INSTANCE == null) {
            Class var0 = com.radiusengine.bbb.server.CachingConfigurations.class;
            synchronized(com.radiusengine.bbb.server.CachingConfigurations.class) {
                if (SINGLE_INSTANCE == null) {
                    SINGLE_INSTANCE = new com.radiusengine.bbb.server.CachingConfigurations();
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
