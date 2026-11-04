//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.workers;

import com.radiusengine.aaa.data.AcctProfileData;
import com.radiusengine.aaa.data.clientData;
import com.radiusengine.aaa.server.CachingConfigurations;
import com.radiusengine.aaa.server.RadiusAcctImpl;
import com.radiusengine.aaa.server.RadiusServerImpl;
import com.radiusengine.aaa.util.ValidateExpression;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;

public class RadiusAcctProcessor implements Runnable {
   Logger logger = LoggerFactory.getLogger("radiuslog");
   Logger reqreslog = LoggerFactory.getLogger("reqresplog");
   DatagramSocket socket;
   String secret;
   RadiusPacket request;
   InetSocketAddress remoteAddress = null;
   String strUsername = null;
   Long lngPacketId = null;
   String sessionId = null;
   long lngStartTime = System.currentTimeMillis();
   String PACKET_IDENTIFIER = "";

   public RadiusAcctProcessor(DatagramSocket socket, InetSocketAddress remoteAddress, String secret, RadiusPacket request) {
      this.socket = socket;
      this.secret = secret;
      this.request = request;
      this.remoteAddress = remoteAddress;

      try {
         this.strUsername = request.getAttribute("User-Name").getAttributeValue();
         if (this.strUsername != null) {
            request.removeAttributes(1);
            request.addAttribute("User-Name", this.strUsername.trim());
         }
      } catch (Exception var8) {
         this.logger.error("User-Name Not Found");
      }

      try {
         this.lngPacketId = (long)request.getPacketIdentifier();
      } catch (Exception var7) {
         this.lngPacketId = System.currentTimeMillis();
      }

      try {
         this.sessionId = request.getAttribute("Acct-Session-Id").getAttributeValue();
      } catch (Exception var6) {
         this.sessionId = String.valueOf(System.currentTimeMillis());
      }

      this.PACKET_IDENTIFIER = "[" + this.strUsername + "][" + this.lngPacketId + "][" + this.sessionId + "][ACCT THREAD]";
   }

   public void run() {
      try {
         this.logger.info(this.PACKET_IDENTIFIER + "Started...");
         InetSocketAddress localAddress = (InetSocketAddress)this.socket.getLocalSocketAddress();
         if (this.secret == null && this.logger.isInfoEnabled()) {
            this.logger.info(this.PACKET_IDENTIFIER + "ignoring packet from unknown client " + this.remoteAddress + " received on local address " + localAddress);
         }

         if (this.logger.isInfoEnabled()) {
            this.logger.info(this.PACKET_IDENTIFIER + "received packet from " + this.remoteAddress + " on local address " + localAddress + ": " + this.request);
         }

         this.logger.trace(this.PACKET_IDENTIFIER + "about to call RadiusServer.handlePacket()");
         RadiusPacket response = null;
         if (this.request instanceof AccountingRequest) {
            response = this.accountingRequestReceived((AccountingRequest)this.request, this.remoteAddress);
         }

         if (response != null) {
            if (this.logger.isInfoEnabled()) {
               this.logger.info(this.PACKET_IDENTIFIER + "send response: " + response);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            response.encodeResponsePacket(bos, this.secret, this.request);
            byte[] data = bos.toByteArray();
            DatagramPacket packetOut = new DatagramPacket(data, data.length, this.remoteAddress.getAddress(), this.remoteAddress.getPort());
            this.socket.send(packetOut);
         } else {
            this.logger.info(this.PACKET_IDENTIFIER + "no response sent");
         }
      } catch (SocketTimeoutException var6) {
         this.logger.trace(this.PACKET_IDENTIFIER + "normal socket timeout");
      } catch (IOException var7) {
         IOException ioe = var7;
         this.logger.error(this.PACKET_IDENTIFIER + "communication error", ioe);
      } catch (Exception var8) {
         Exception e = var8;
         this.logger.error(this.PACKET_IDENTIFIER + "ERROR:", e);
         e.printStackTrace();
      }

      long endTime = System.currentTimeMillis();
      this.logger.info(this.PACKET_IDENTIFIER + "Ended. Time to Final Respond:" + (endTime - this.lngStartTime));
   }

   public RadiusPacket accountingRequestReceived(AccountingRequest request, InetSocketAddress client) {
      RadiusPacket accoutningResponse = new RadiusPacket(5, request.getPacketIdentifier());

      try {
         RadiusServerImpl impl = new RadiusServerImpl();

         try {
            clientData cltData = impl.identifyClient(client.getAddress().toString());
            request.removeAttributes(77);
            request.addAttribute("Connect-Info", cltData.getGroupname());
         } catch (Exception var17) {
            Exception e = var17;
            this.logger.error(this.PACKET_IDENTIFIER + "Error in accountingRequestReceived() : " + e, e);
         }

         this.logger.debug(this.PACKET_IDENTIFIER + "Radius Accounting Request Packet is : " + request);
         this.reqreslog.info(this.PACKET_IDENTIFIER + "Received Accounting-" + request.getAttributeValue("Acct-Status-Type") + " Request from:[" + client.getAddress() + "-" + client.getPort() + "]\n" + request);
         CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
         Cache cache = CachingConfigurations.getCacheManagerData().getCache("acctprofile");
         Iterator var7 = cache.getKeys().iterator();

         label37:
         while(true) {
            while(true) {
               if (!var7.hasNext()) {
                  break label37;
               }

               Object key = var7.next();
               Element element = cache.get(key);
               ConcurrentHashMap<String, String> acctProfile = (ConcurrentHashMap)element.getObjectValue();
               Iterator var11 = acctProfile.entrySet().iterator();

               while(var11.hasNext()) {
                  Map.Entry entry = (Map.Entry)var11.next();
                  AcctProfileData acctProfileData = (AcctProfileData)entry.getValue();
                  ValidateExpression validate = new ValidateExpression(this.PACKET_IDENTIFIER);
                  boolean blnResponse = validate.checkExpression(acctProfileData.getCheckitem(), request);
                  if (blnResponse) {
                     RadiusAcctImpl acctImpl = new RadiusAcctImpl(request, accoutningResponse, acctProfileData.getAccountcdrstatus(), acctProfileData.getSessionstatus(), acctProfileData.getDbFieldMapping());
                     acctImpl.processAcctPacket();
                     break;
                  }
               }
            }
         }
      } catch (Exception var18) {
         Exception e = var18;
         e.printStackTrace();
      }

      this.reqreslog.info(this.PACKET_IDENTIFIER + "Sending Accounting Response:[" + client.getAddress() + "-" + client.getPort() + "]\n" + accoutningResponse);
      return accoutningResponse;
   }
}
