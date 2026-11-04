///**
// * @author Akash Gaur
// * @created year 2026
// */
//
//package com.radiusengine.bbb.server;
//
//import com.radiusengine.bbb.server.CachingConfigurations;
//import com.radiusengine.bbb.server.RadiusServerImpl;
//import com.radiusengine.bbb.snmp.SNMPTrapGenerator;
//import com.radiusengine.bbb.webservices.RadiusServices;
//import com.radiusengine.bbb.webservices.ReloadConfiguration;
//import com.radiusengine.bbb.webservices.ValidateServer;
//import com.radiusengine.bbb.constant.AAAConstant;
//import net.sf.ehcache.Cache;
//import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.tinyradius.threadpool.*;
//import org.tinyradius.util.RadiusServer;
//
//import javax.ws.rs.core.UriBuilder;
//import java.net.InetAddress;
//import java.net.URI;
//import java.util.HashSet;
//import java.util.Set;
//
//public class RadiusAAAServer {
//    RadiusServer server = new com.radiusengine.bbb.server.RadiusServerImpl();
//
//    public RadiusAAAServer() {
//    }
//
//    public static void main(String[] args) {
//        new RadiusServerImpl();
//        Logger log = LoggerFactory.getLogger("radiuslog");
//
//        try {
//            com.radiusengine.bbb.server.RadiusAAAServer radiusAAAServer = new com.radiusengine.bbb.server.RadiusAAAServer();
//            if (args.length == 0) {
//                radiusAAAServer.startServer();
//            } else if (args.length != 0 && args[0].equals("stop")) {
//                radiusAAAServer.stopServer();
//            } else {
//                radiusAAAServer.startServer();
//            }
//        } catch (Exception var4) {
//            Exception e = var4;
//            log.error("Error Staring Radius Server:" + e, e);
//            e.printStackTrace();
//        }
//
//    }
//
//    private void startServer() {
//        Logger log = LoggerFactory.getLogger("radiuslog");
//
//        try {
//            log.info("Initializing Radius Server");
//            com.radiusengine.bbb.server.CachingConfigurations cacheConfig = com.radiusengine.bbb.server.CachingConfigurations.getInstance();
//            Cache cache = com.radiusengine.bbb.server.CachingConfigurations.getCacheManagerData().getCache("serverConfig");
//            this.server.setAcctPort(Integer.parseInt(cache.get("acctPort").getObjectValue().toString()));
//            this.server.setAuthPort(Integer.parseInt(cache.get("authPort").getObjectValue().toString()));
//            this.server.setAuthMaxThreads(Integer.parseInt(cache.get("authMaxThreads").getObjectValue().toString()));
//            this.server.setAuthMinThreads(Integer.parseInt(cache.get("authMinThreads").getObjectValue().toString()));
//            this.server.setAcctMaxThreads(Integer.parseInt(cache.get("acctMaxThreads").getObjectValue().toString()));
//            this.server.setAcctMinThreads(Integer.parseInt(cache.get("acctMinThreads").getObjectValue().toString()));
//            this.server.setThreadBenchTime(Integer.parseInt(cache.get("threadBenchTime").getObjectValue().toString()));
//            RadiusAuthRespPool.getRadiusAuthRespPool().setAuthMinThreads(Integer.parseInt(cache.get("authRespMinThreads").getObjectValue().toString()));
//            RadiusAuthRespPool.getRadiusAuthRespPool().setAuthMaxThreads(Integer.parseInt(cache.get("authRespMaxThreads").getObjectValue().toString()));
//            RadiusAuthRespPool.getRadiusAuthRespPool().setThreadBenchTime(this.server.getThreadBenchTime());
//            RadiusCOAPool.getCoaPool().setCoaMaxThread(Integer.parseInt(cache.get("coaMaxThreads").getObjectValue().toString()));
//            RadiusCOAPool.getCoaPool().setCoaMinThread(Integer.parseInt(cache.get("coaMinThreads").getObjectValue().toString()));
//            RadiusCOAPool.getCoaPool().setCoaSleepTime(Integer.parseInt(cache.get("coaSleepTime").getObjectValue().toString()));
//            RadiusCOAPool.getCoaPool().setThreadBenchTime(Integer.parseInt(cache.get("threadBenchTime").getObjectValue().toString()));
//            RadiusAuthAuditPool.getInstance().setRadiusAuthAuditMinThread(Integer.parseInt(cache.get("authAuditMinThreads").getObjectValue().toString()));
//            RadiusAuthAuditPool.getInstance().setRadiusAuthAuditMaxThread(Integer.parseInt(cache.get("authAuditMaxThreads").getObjectValue().toString()));
//            RadiusAuthAuditPool.getInstance().setThreadBenchTime(Integer.parseInt(cache.get("threadBenchTime").getObjectValue().toString()));
//            RadiusRecordCDRPool.getInstance().setMinThread(Integer.parseInt(cache.get("recordCDRMinThreads").getObjectValue().toString()));
//            RadiusRecordCDRPool.getInstance().setMaxThread(Integer.parseInt(cache.get("recordCDRMaxThreads").getObjectValue().toString()));
//            RadiusRecordCDRPool.getInstance().setThreadBenchTime(Integer.parseInt(cache.get("threadBenchTime").getObjectValue().toString()));
//            RadiusAcctStartPool.getInstance().setMinThread(Integer.parseInt(cache.get("recordAcctStartMinThreads").getObjectValue().toString()));
//            RadiusAcctStartPool.getInstance().setMaxThread(Integer.parseInt(cache.get("recordAcctStartMaxThreads").getObjectValue().toString()));
//            RadiusAcctStartPool.getInstance().setThreadBenchTime(Integer.parseInt(cache.get("threadBenchTime").getObjectValue().toString()));
//            CommonThreadPool.getInstance().setMinThread(Integer.parseInt(cache.get("commonPoolMinThreads").getObjectValue().toString()));
//            CommonThreadPool.getInstance().setMaxThread(Integer.parseInt(cache.get("commonPoolMaxThreads").getObjectValue().toString()));
//            CommonThreadPool.getInstance().setThreadBenchTime(Integer.parseInt(cache.get("threadBenchTime").getObjectValue().toString()));
//            InetAddress address = InetAddress.getByName(cache.get("serverip").getObjectValue().toString());
//            this.server.setListenAddress(address);
//            this.server.start(true, true);
//            URI baseUri = UriBuilder.fromUri(cache.get("webserviceIP").getObjectValue().toString()).port(Integer.parseInt(cache.get("webservicePort").getObjectValue().toString())).build(new Object[0]);
//            Set resourceSet = new HashSet();
//            resourceSet.add(ValidateServer.class);
//            resourceSet.add(ReloadConfiguration.class);
//            resourceSet.add(RadiusServices.class);
//            ResourceConfig config = new ResourceConfig(resourceSet);
//            JdkHttpServerFactory.createHttpServer(baseUri, config);
//            log.info("*** Radius Server Started Successfully ***");
//            com.radiusengine.bbb.snmp.SNMPTrapGenerator trapV2 = new com.radiusengine.bbb.snmp.SNMPTrapGenerator();
//            trapV2.clearTrap_Version2("public", "127.0.0.1", 162, "Radius Server Up", ".1.3.6.1.2.1.1.9");
//        } catch (Exception var10) {
//            Exception e = var10;
//            log.error("Error in startServer():" + e, e);
//        }
//
//    }
//
//    private void stopServer() {
//        Logger log = LoggerFactory.getLogger("radiuslog");
//
//        try {
//            log.info("Stopping Radius Server");
//            com.radiusengine.bbb.server.CachingConfigurations cacheConfig = com.radiusengine.bbb.server.CachingConfigurations.getInstance();
//            Cache cache = CachingConfigurations.getCacheManagerData().getCache("serverConfig");
//            this.server.setAcctPort(Integer.parseInt(cache.get("acctPort").getObjectValue().toString()));
//            this.server.setAuthPort(Integer.parseInt(cache.get("authPort").getObjectValue().toString()));
//            InetAddress address = InetAddress.getByName(cache.get("serverip").getObjectValue().toString());
//            this.server.setListenAddress(address);
//            this.server.stop();
//            com.radiusengine.bbb.snmp.SNMPTrapGenerator trapV2 = new SNMPTrapGenerator();
//            trapV2.sendTrap_Version2("public", "127.0.0.1", 162, "Radius Server Down", ".1.3.6.1.2.1.1.8");
//        } catch (Exception var6) {
//            Exception e = var6;
//            log.error("Error in stopServer():" + e, e);
//        }
//
//    }
//}
