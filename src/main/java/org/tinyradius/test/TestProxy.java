package org.tinyradius.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.proxy.RadiusProxy;
import org.tinyradius.util.RadiusEndpoint;

public class TestProxy extends RadiusProxy {
   public RadiusEndpoint getProxyServer(RadiusPacket packet, RadiusEndpoint client) {
      try {
         InetAddress address = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
         int port = 10000;
         if (packet instanceof AccountingRequest) {
            port = 10001;
         }

         return new RadiusEndpoint(new InetSocketAddress(address, port), "testing123");
      } catch (UnknownHostException uhe) {
         uhe.printStackTrace();
         return null;
      }
   }

   public String getSharedSecret(InetSocketAddress client) {
      if (client.getPort() != 10000 && client.getPort() != 10001) {
         return client.getAddress().getHostAddress().equals("127.0.0.1") ? "proxytest" : null;
      } else {
         return "testing123";
      }
   }

   public String getUserPassword(String userName) {
      return null;
   }

   public static void main(String[] args) {
      (new TestProxy()).start(true, true, true);
   }
}
