package org.tinyradius.workers;

import com.radiusengine.aaa.data.CDRData;
import com.radiusengine.aaa.data.clientData;
import com.radiusengine.aaa.server.CachingConfigurations;
import com.radiusengine.aaa.server.RadiusServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.CoaRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

public class RadiusCoAProcessor implements Runnable {
   Logger log = LoggerFactory.getLogger("coalog");
   CDRData cdrData;
   String PACKET_IDENTIFIER = "";
   String requestType = "DM";
   long lngStartTime = 0L;

   public RadiusCoAProcessor(String PACKET_IDENTIFIER, CDRData cdrData, String reqType) {
      this.cdrData = cdrData;
      this.PACKET_IDENTIFIER = PACKET_IDENTIFIER;
      this.lngStartTime = System.currentTimeMillis();
      this.requestType = reqType;
   }

   public void run() {
      try {
         if (this.requestType.equals("DM")) {
            this.sendDMRequest(this.cdrData);
         } else if (this.requestType.equals("COA")) {
         }
      } catch (Exception e) {
         this.log.error(this.PACKET_IDENTIFIER + "ERROR:" + e, e);
      }

      long endTime = System.currentTimeMillis();
      this.log.info(this.PACKET_IDENTIFIER + "COA Ended. Time to Final Respond:" + (endTime - this.lngStartTime));
   }

   public void sendDMRequest(CDRData cdrData) {
      this.log.debug(this.PACKET_IDENTIFIER + "Into sendDMRequest() Start");

      try {
         Integer coaPort = Integer.parseInt(CachingConfigurations.getCachedValue("serverConfig", "coaPort").toString());
         String coaOperation = CachingConfigurations.getCachedValue("serverConfig", "coaOperation").toString();
         String sendCOA = CachingConfigurations.getCachedValue("serverConfig", "SEND_COA").toString();
         String coaTimeOut = CachingConfigurations.getCachedValue("serverConfig", "coaTimeout").toString();
         String coaRetry = CachingConfigurations.getCachedValue("serverConfig", "coaRetry").toString();
         String coaSleepTime = CachingConfigurations.getCachedValue("serverConfig", "coaSleepTime").toString();
         String strInActivePoolIPStartsWith = CachingConfigurations.getCachedValue("serverAttribe", "INACTIVE_POOL_IP").toString();
         if (strInActivePoolIPStartsWith != null) {
            strInActivePoolIPStartsWith.trim();
         }

         try {
            Thread.sleep((long)Integer.parseInt(coaSleepTime));
         } catch (Exception var18) {
         }

         if (sendCOA != null && sendCOA.equalsIgnoreCase("true")) {
            int coaPacketType = 40;
            String requestType = "Disconnect";
            if (coaOperation.equals("COA")) {
               coaPacketType = 43;
               requestType = "COA";
            }

            RadiusServerImpl impl = new RadiusServerImpl();
            clientData client = impl.identifyClient(cdrData.getNasip());
            if (client != null) {
               if (strInActivePoolIPStartsWith != null && !"".equalsIgnoreCase(strInActivePoolIPStartsWith) && cdrData.getFramedip() != null && !cdrData.getFramedip().startsWith(strInActivePoolIPStartsWith)) {
                  RadiusClient rc = new RadiusClient(cdrData.getNasip(), client.getSharedkey());
                  if (coaTimeOut != null && !"".equalsIgnoreCase(coaTimeOut)) {
                     try {
                        rc.setSocketTimeout(Integer.parseInt(coaTimeOut));
                     } catch (Exception var17) {
                     }
                  }

                  if (coaRetry != null && !"".equalsIgnoreCase(coaRetry)) {
                     try {
                        rc.setRetryCount(Integer.parseInt(coaRetry));
                     } catch (Exception var16) {
                        rc.setRetryCount(0);
                     }
                  }

                  RadiusPacket dmPacket = new CoaRequest(coaPacketType);
                  dmPacket.addAttribute("Acct-Session-Id", cdrData.getSessionid());
                  dmPacket.addAttribute("User-Name", cdrData.getUsername());
                  if (cdrData.getFramedip() != null) {
                     dmPacket.addAttribute("Framed-IP-Address", cdrData.getFramedip());
                  }

                  this.log.debug(this.PACKET_IDENTIFIER + "Into sendDMRequest(), Sending " + requestType + " NAS IP:" + cdrData.getNasip() + " Packet:" + dmPacket);
                  RadiusPacket dmResponse = rc.communicate(dmPacket, coaPort);
                  this.log.debug(this.PACKET_IDENTIFIER + "Into sendDMRequest(),Response: " + dmResponse);
               } else {
                  this.log.debug(this.PACKET_IDENTIFIER + "sendDMRequest Skipped for Framed IP :" + cdrData.getFramedip());
               }
            } else {
               this.log.debug(this.PACKET_IDENTIFIER + "sendDMRequest Radius Client:" + cdrData.getNasip() + " Not a Allowed Client");
            }
         } else {
            this.log.info(this.PACKET_IDENTIFIER + "sendDMRequest() COA SKIPPED, WITH FLAG");
         }
      } catch (Exception e) {
         this.log.error(this.PACKET_IDENTIFIER + "Error in sendDMRequest():" + e, e);
      }

   }
}
