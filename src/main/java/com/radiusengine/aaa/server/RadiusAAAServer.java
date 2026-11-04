/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.server;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.snmp.SNMPTrapGenerator;
import com.radiusengine.aaa.webservices.RadiusServices;
import com.radiusengine.aaa.webservices.ReloadConfiguration;
import com.radiusengine.aaa.webservices.ValidateServer;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;
import net.sf.ehcache.Cache;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.threadpool.CommonThreadPool;
import org.tinyradius.threadpool.RadiusAcctStartPool;
import org.tinyradius.threadpool.RadiusAuthAuditPool;
import org.tinyradius.threadpool.RadiusAuthRespPool;
import org.tinyradius.threadpool.RadiusCOAPool;
import org.tinyradius.threadpool.RadiusRecordCDRPool;
import org.tinyradius.util.RadiusServer;

public class RadiusAAAServer {
    RadiusServer server = new RadiusServerImpl();

    public RadiusAAAServer() {
    }

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger("radiuslog");

        try {
            RadiusAAAServer radiusAAAServer = new RadiusAAAServer();
            if (args.length > 0 && args[0].equals("stop")) {
                radiusAAAServer.stopServer();
            } else {
                radiusAAAServer.startServer();
            }
        } catch (Exception e) {
            log.error("Error Staring Radius Server:" + e, e);
            e.printStackTrace();
        }

    }

    /** Reads a server config value from the cache and parses it as an int (NPE / NumberFormatException when missing or invalid). */
    private static int intValue(Cache cache, String key) {
        return Integer.parseInt(cache.get(key).getObjectValue().toString());
    }

    private void startServer() {
        Logger log = LoggerFactory.getLogger("radiuslog");

        try {
            log.info("Initializing Radius Server");
            CachingConfigurations.getInstance();
            Cache cache = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCACHING);
            this.server.setAcctPort(intValue(cache, AAAConstant.ACCTPORT));
            this.server.setAuthPort(intValue(cache, AAAConstant.AUTHPORT));
            this.server.setAuthMaxThreads(intValue(cache, AAAConstant.AUTH_MAX_THREAD));
            this.server.setAuthMinThreads(intValue(cache, AAAConstant.AUTH_MIN_THREAD));
            this.server.setAcctMaxThreads(intValue(cache, AAAConstant.ACCT_MAX_THREAD));
            this.server.setAcctMinThreads(intValue(cache, AAAConstant.ACCT_MIN_THREAD));
            this.server.setThreadBenchTime(intValue(cache, AAAConstant.THREAD_BENCH_TIME));
            RadiusAuthRespPool.getRadiusAuthRespPool().setAuthMinThreads(intValue(cache, AAAConstant.AUTH_RESP_MIN_THREAD));
            RadiusAuthRespPool.getRadiusAuthRespPool().setAuthMaxThreads(intValue(cache, AAAConstant.AUTH_RESP_MAX_THREAD));
            RadiusAuthRespPool.getRadiusAuthRespPool().setThreadBenchTime(this.server.getThreadBenchTime());
            RadiusCOAPool.getCoaPool().setCoaMaxThread(intValue(cache, AAAConstant.COA_MAX_THREAD));
            RadiusCOAPool.getCoaPool().setCoaMinThread(intValue(cache, AAAConstant.COA_MIN_THREAD));
            RadiusCOAPool.getCoaPool().setCoaSleepTime(intValue(cache, AAAConstant.COA_SLEEPTIME));
            RadiusCOAPool.getCoaPool().setThreadBenchTime(intValue(cache, AAAConstant.THREAD_BENCH_TIME));
            RadiusAuthAuditPool.getInstance().setRadiusAuthAuditMinThread(intValue(cache, AAAConstant.RADIUS_AUTH_AUDIT_MIN_THREAD));
            RadiusAuthAuditPool.getInstance().setRadiusAuthAuditMaxThread(intValue(cache, AAAConstant.RADIUS_AUTH_AUDIT_MAX_THREAD));
            RadiusAuthAuditPool.getInstance().setThreadBenchTime(intValue(cache, AAAConstant.THREAD_BENCH_TIME));
            RadiusRecordCDRPool.getInstance().setMinThread(intValue(cache, AAAConstant.RECORD_CDR_MIN_THREAD));
            RadiusRecordCDRPool.getInstance().setMaxThread(intValue(cache, AAAConstant.RECORD_CDR_MAX_THREAD));
            RadiusRecordCDRPool.getInstance().setThreadBenchTime(intValue(cache, AAAConstant.THREAD_BENCH_TIME));
            RadiusAcctStartPool.getInstance().setMinThread(intValue(cache, AAAConstant.RECORD_ACCTSTART_MIN_THREAD));
            RadiusAcctStartPool.getInstance().setMaxThread(intValue(cache, AAAConstant.RECORD_ACCTSTART_MAX_THREAD));
            RadiusAcctStartPool.getInstance().setThreadBenchTime(intValue(cache, AAAConstant.THREAD_BENCH_TIME));
            CommonThreadPool.getInstance().setMinThread(intValue(cache, AAAConstant.COMMONPOOL_MIN_THREAD));
            CommonThreadPool.getInstance().setMaxThread(intValue(cache, AAAConstant.COMMONPOOL_MAX_THREAD));
            CommonThreadPool.getInstance().setThreadBenchTime(intValue(cache, AAAConstant.THREAD_BENCH_TIME));
            InetAddress address = InetAddress.getByName(cache.get(AAAConstant.SERVERIP).getObjectValue().toString());
            this.server.setListenAddress(address);
            this.server.start(true, true);
            URI baseUri = UriBuilder.fromUri(cache.get(AAAConstant.WEBSERVICEIP).getObjectValue().toString()).port(intValue(cache, AAAConstant.WEBSERVICEPORT)).build();
            Set<Class<?>> resourceSet = new HashSet<>();
            resourceSet.add(ValidateServer.class);
            resourceSet.add(ReloadConfiguration.class);
            resourceSet.add(RadiusServices.class);
            ResourceConfig config = new ResourceConfig(resourceSet);
            JdkHttpServerFactory.createHttpServer(baseUri, config);
            log.info("*** Radius Server Started Successfully ***");
            SNMPTrapGenerator trapV2 = new SNMPTrapGenerator();
            trapV2.clearTrap_Version2("public", "127.0.0.1", 162, "Radius Server Up", ".1.3.6.1.2.1.1.9");
        } catch (Exception e) {
            log.error("Error in startServer():" + e, e);
        }

    }

    private void stopServer() {
        Logger log = LoggerFactory.getLogger("radiuslog");

        try {
            log.info("Stopping Radius Server");
            CachingConfigurations.getInstance();
            Cache cache = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.SERVERCACHING);
            this.server.setAcctPort(intValue(cache, AAAConstant.ACCTPORT));
            this.server.setAuthPort(intValue(cache, AAAConstant.AUTHPORT));
            InetAddress address = InetAddress.getByName(cache.get(AAAConstant.SERVERIP).getObjectValue().toString());
            this.server.setListenAddress(address);
            this.server.stop();
            SNMPTrapGenerator trapV2 = new SNMPTrapGenerator();
            trapV2.sendTrap_Version2("public", "127.0.0.1", 162, "Radius Server Down", ".1.3.6.1.2.1.1.8");
        } catch (Exception e) {
            log.error("Error in stopServer():" + e, e);
        }

    }
}
