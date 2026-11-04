/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.server;

import com.radiusengine.aaa.constant.AAAConstant;
import com.radiusengine.aaa.data.clientData;
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
        try {
            clientData cltData = this.identifyClient(client.getAddress().toString());
            if (cltData == null) {
                this.log.debug("Received Request From " + client.getAddress().toString() + ", Share Secret Key not found");
                return null;
            }

            this.log.debug("Received Request From " + client.getAddress().toString() + ", Share Secret Key found");
            return cltData.getSharedkey();
        } catch (Exception e) {
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

        this.log.debug("Received Request From " + strClient);
        Cache cache = CachingConfigurations.getCacheManagerData().getCache(AAAConstant.CLIENTCACHING);
        clientData cltData;
        Element exactMatch = cache.get(strClient);
        if (exactMatch != null) {
            cltData = (clientData) exactMatch.getObjectValue();
            this.log.debug("For Client:" + strClient + ":Shared Key is " + cltData.getSharedkey());
            return cltData;
        } else {

            for (Object key : cache.getKeys()) {
                Element element = cache.get(key);
                clientData cltDataDat = (clientData) element.getObjectValue();
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

            if (cache.get(AAAConstant.ALLOWALL) != null) {
                cltData = (clientData)cache.get(AAAConstant.ALLOWALL).getObjectValue();
                this.log.debug("For Client:0.0.0.0:Shared Key is " + cltData.getSharedkey());
                return cltData;
            } else {
                this.log.debug("For Client Return Null");
                return null;
            }
        }
    }
}
