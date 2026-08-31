package org.tinyradius.workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusAuthRespSender implements Runnable {
   Logger logger = LoggerFactory.getLogger("radiuslog");
   Logger reqreslog = LoggerFactory.getLogger("reqresplog");
   DatagramSocket socket;
   DatagramPacket packetOut;
   String strUsername = null;
   Long lngPacketId = null;
   String sessionId = null;
   long lngStartTime = System.currentTimeMillis();
   String PACKET_IDENTIFIER = "";

   public RadiusAuthRespSender(DatagramSocket socket, DatagramPacket packetOut, String userName, Long packetId, String sessionId) {
      this.socket = socket;
      this.packetOut = packetOut;
      this.strUsername = userName;
      this.lngPacketId = packetId;
      this.sessionId = sessionId;
      this.PACKET_IDENTIFIER = "[" + this.strUsername + "][" + this.lngPacketId + "][" + sessionId + "][RadiusAuthRespSender]";
   }

   public void run() {
      try {
         this.logger.info(this.PACKET_IDENTIFIER + "Started...");
         this.socket.send(this.packetOut);
      } catch (SocketTimeoutException ste) {
         this.logger.trace(this.PACKET_IDENTIFIER + "normal socket timeout:" + ste, ste);
      } catch (IOException ioe) {
         this.logger.error(this.PACKET_IDENTIFIER + "communication error:" + ioe, ioe);
      } catch (Exception e) {
         this.logger.error(this.PACKET_IDENTIFIER + "ERROR:" + e, e);
      }

      long endTime = System.currentTimeMillis();
   }
}
