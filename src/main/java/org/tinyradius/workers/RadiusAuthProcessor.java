//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.workers;

import com.radiusengine.aaa.server.CachingConfigurations;
import com.radiusengine.aaa.server.RadiusAuthImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.threadpool.RadiusAuthRespPool;

public class RadiusAuthProcessor implements Runnable {
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

   public RadiusAuthProcessor(DatagramSocket socket, InetSocketAddress remoteAddress, String secret, RadiusPacket request) {
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
         this.logger.info("User-Name Not Found");
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

      this.PACKET_IDENTIFIER = "[" + this.strUsername + "][" + this.lngPacketId + "][" + this.sessionId + "][AUTH THREAD]";
   }

   public void run() {
      try {
         CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
         Cache cacheServerAttribe = CachingConfigurations.getCacheManagerData().getCache("serverConfig");
         boolean enableRespThreadPool = false;

         try {
            enableRespThreadPool = Boolean.parseBoolean(cacheServerAttribe.get("enable_resp_thread_pool").getObjectValue().toString());
         } catch (Exception var10) {
            enableRespThreadPool = false;
         }

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
         if (this.request instanceof AccessRequest) {
            response = this.accessRequestReceived((AccessRequest)this.request, this.remoteAddress);
         }

         if (response != null) {
            if (this.logger.isInfoEnabled()) {
               this.logger.info(this.PACKET_IDENTIFIER + "send response: " + response);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            response.encodeResponsePacket(bos, this.secret, this.request);
            byte[] data = bos.toByteArray();
            DatagramPacket packetOut = new DatagramPacket(data, data.length, this.remoteAddress.getAddress(), this.remoteAddress.getPort());
            if (enableRespThreadPool) {
               RadiusAuthRespSender sender = new RadiusAuthRespSender(this.socket, packetOut, this.strUsername, this.lngPacketId, this.sessionId);
               RadiusAuthRespPool.getRadiusAuthRespPool().getAuthRespExecutor().execute(sender);
            } else {
               this.socket.send(packetOut);
            }
         } else {
            this.logger.info(this.PACKET_IDENTIFIER + "no response sent");
         }
      } catch (SocketTimeoutException var11) {
         this.logger.trace(this.PACKET_IDENTIFIER + "normal socket timeout");
      } catch (IOException var12) {
         IOException ioe = var12;
         this.logger.error(this.PACKET_IDENTIFIER + "communication error", ioe);
      } catch (Exception var13) {
         Exception e = var13;
         this.logger.error(this.PACKET_IDENTIFIER + "ERROR:", e);
      }

      long endTime = System.currentTimeMillis();
      this.logger.info(this.PACKET_IDENTIFIER + "Ended. Time to Final Respond:" + (endTime - this.lngStartTime));
   }

   public RadiusPacket accessRequestReceived(AccessRequest request, InetSocketAddress client) {
      this.reqreslog.info(this.PACKET_IDENTIFIER + "Received Access Request from:[" + client.getAddress() + "-" + client.getPort() + "]\n" + request);
      RadiusPacket accessResponse = new RadiusPacket(3, request.getPacketIdentifier());

      try {
         RadiusAuthImpl authImpl = new RadiusAuthImpl(request, client);
         accessResponse = authImpl.processAccessRquest();
      } catch (Exception var5) {
         Exception e = var5;
         accessResponse.addAttribute("Reply-Message", "Internal Error Authentication Fail");
         accessResponse.setPacketType(3);
         this.logger.info(this.PACKET_IDENTIFIER + "Error in accessRequestReceived():" + e, e);
      }

      this.reqreslog.info(this.PACKET_IDENTIFIER + "Sending Access Response:[" + client.getAddress() + "-" + client.getPort() + "]\n" + accessResponse);
      return accessResponse;
   }
}
