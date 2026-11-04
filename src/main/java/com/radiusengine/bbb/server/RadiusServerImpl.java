//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.bbb.server;

import com.radiusengine.bbb.data.clientData;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Iterator;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.util.RadiusServer;

public class RadiusServerImpl extends RadiusServer {
    Logger log = LoggerFactory.getLogger("radiuslog");
    Logger reqreslog = LoggerFactory.getLogger("reqresplog");
    String secret;
    DatagramPacket packetIn;
    DatagramSocket s;

    public RadiusServerImpl() {
    }

    public String getSharedSecret(InetSocketAddress client) {
        Exception e;
        try {
            e = null;
            clientData cltData = this.identifyClient(client.getAddress().toString());
            if (cltData != null) {
                this.log.debug("Received Request From " + client.getAddress().toString() + ", Share Secret Key:" + cltData != null ? cltData.getSharedkey() : "not found");
            } else {
                this.log.debug("Received Request From " + client.getAddress().toString() + ", Share Secret Key not found");
            }

            if (cltData != null) {
                return cltData.getSharedkey();
            }
        } catch (Exception var3) {
            e = var3;
            this.log.error("Error in getSharedSecret():" + e, e);
        }

        return null;
    }

    public String getUserPassword(String userName) {
        this.log.debug("UserPassword " + userName);
        return "TEST";
    }

    public clientData identifyClient(String strClient) {
        if (strClient != null) {
            strClient = strClient.replaceAll("/", "");
        }

        new clientData();
        this.log.debug("Received Request From " + strClient);
        Cache cache = CachingConfigurations.getCacheManagerData().getCache("clientConfig");
        clientData cltData;
        if (cache.get(strClient) != null) {
            cltData = (clientData)cache.get(strClient).getObjectValue();
            this.log.debug("For Client:" + strClient + ":Shared Key is " + cltData.getSharedkey());
            return cltData;
        } else {
            Iterator var4 = cache.getKeys().iterator();

            while(var4.hasNext()) {
                Object key = var4.next();
                Element element = cache.get(key);
                clientData cltDataDat = (clientData)element.getObjectValue();
                if (cltDataDat.getIptype().equalsIgnoreCase("subnet")) {
                    cltDataDat.getClientip();
                    String addrStr = strClient.substring(1);
                    this.log.debug("Address is :" + addrStr + ":Client Ip:" + cltDataDat.getClientip());
                    IPAddress subnetAddress = (new IPAddressString(cltDataDat.getClientip())).getAddress();
                    IPAddress subnet = subnetAddress.toPrefixBlock();
                    IPAddress testAddress = (new IPAddressString(addrStr)).getAddress();
                    boolean result = subnet.contains(testAddress);
                    if (result) {
                        this.log.debug("Matched");
                        return cltDataDat;
                    }
                }
            }

            if (cache.get("0.0.0.0") != null) {
                cltData = (clientData)cache.get("0.0.0.0").getObjectValue();
                this.log.debug("For Client:0.0.0.0:Shared Key is " + cltData.getSharedkey());
                return cltData;
            } else {
                this.log.debug("For Client Return Null");
                return null;
            }
        }
    }
}
