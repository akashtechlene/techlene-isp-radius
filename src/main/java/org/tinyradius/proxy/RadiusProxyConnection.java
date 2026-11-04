
package org.tinyradius.proxy;

import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusEndpoint;

public class RadiusProxyConnection {
   private RadiusEndpoint radiusServer;
   private RadiusEndpoint radiusClient;
   private int port;
   private RadiusPacket packet;

   public RadiusProxyConnection(RadiusEndpoint radiusServer, RadiusEndpoint radiusClient, RadiusPacket packet, int port) {
      this.radiusServer = radiusServer;
      this.radiusClient = radiusClient;
      this.packet = packet;
      this.port = port;
   }

   public RadiusEndpoint getRadiusClient() {
      return this.radiusClient;
   }

   public RadiusEndpoint getRadiusServer() {
      return this.radiusServer;
   }

   public RadiusPacket getPacket() {
      return this.packet;
   }

   public int getPort() {
      return this.port;
   }
}
