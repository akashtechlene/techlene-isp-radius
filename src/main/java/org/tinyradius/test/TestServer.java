package org.tinyradius.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

public class TestServer {
   public static void main(String[] args) throws IOException, Exception {
      RadiusServer server = new RadiusServer() {
         public String getSharedSecret(InetSocketAddress client) {
            return client.getAddress().getHostAddress().equals("127.0.0.1") ? "testing123" : null;
         }

         public String getUserPassword(String userName) {
            return userName.equals("mw") ? "test" : null;
         }

         public RadiusPacket accessRequestReceived(AccessRequest accessRequest, InetSocketAddress client) throws RadiusException {
            System.out.println("Received Access-Request:\n" + accessRequest);
            RadiusPacket packet = super.accessRequestReceived(accessRequest, client);
            if (packet.getPacketType() == 2) {
               packet.addAttribute("Reply-Message", "Welcome " + accessRequest.getUserName() + "!");
            }

            if (packet == null) {
               System.out.println("Ignore packet.");
            } else {
               System.out.println("Answer:\n" + packet);
            }

            return packet;
         }
      };
      if (args.length >= 1) {
         server.setAuthPort(Integer.parseInt(args[0]));
      }

      if (args.length >= 2) {
         server.setAcctPort(Integer.parseInt(args[1]));
      }

      server.start(true, true);
      System.out.println("Server started.");
      Thread.sleep(1800000L);
      System.out.println("Stop server");
      server.stop();
   }
}
