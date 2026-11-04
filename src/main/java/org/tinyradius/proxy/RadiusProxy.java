//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusEndpoint;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

public abstract class RadiusProxy extends RadiusServer {
   private int proxyIndex = 1;
   private Map proxyConnections = new HashMap();
   private int proxyPort = 1814;
   private DatagramSocket proxySocket = null;
   private static Logger logger = LoggerFactory.getLogger("radiuslog");

   public RadiusProxy() {
   }

   public void start(boolean listenAuth, boolean listenAcct, boolean listenProxy) {
      super.start(listenAuth, listenAcct);
      if (listenProxy) {
         (new Thread() {
            public void run() {
               this.setName("Radius Proxy Listener");

               try {
                  RadiusProxy.logger.info("starting RadiusProxyListener on port " + RadiusProxy.this.getProxyPort());
                  RadiusProxy.this.listen(RadiusProxy.this.getProxySocket());
               } catch (Exception var2) {
                  Exception e = var2;
                  e.printStackTrace();
               }

            }
         }).start();
      }

   }

   public void stop() {
      logger.info("stopping Radius proxy");
      if (this.proxySocket != null) {
         this.proxySocket.close();
      }

      super.stop();
   }

   public abstract RadiusEndpoint getProxyServer(RadiusPacket var1, RadiusEndpoint var2);

   public int getProxyPort() {
      return this.proxyPort;
   }

   public void setProxyPort(int proxyPort) {
      this.proxyPort = proxyPort;
      this.proxySocket = null;
   }

   public void setSocketTimeout(int socketTimeout) throws SocketException {
      super.setSocketTimeout(socketTimeout);
      if (this.proxySocket != null) {
         this.proxySocket.setSoTimeout(socketTimeout);
      }

   }

   protected DatagramSocket getProxySocket() throws SocketException {
      if (this.proxySocket == null) {
         if (this.getListenAddress() == null) {
            this.proxySocket = new DatagramSocket(this.getProxyPort());
         } else {
            this.proxySocket = new DatagramSocket(this.getProxyPort(), this.getListenAddress());
         }

         this.proxySocket.setSoTimeout(this.getSocketTimeout());
      }

      return this.proxySocket;
   }

   protected RadiusPacket handlePacket(InetSocketAddress localAddress, InetSocketAddress remoteAddress, RadiusPacket request, String sharedSecret) throws RadiusException, IOException {
      if (localAddress.getPort() == this.getProxyPort()) {
         this.proxyPacketReceived(request, remoteAddress);
         return null;
      } else {
         RadiusEndpoint radiusClient = new RadiusEndpoint(remoteAddress, sharedSecret);
         RadiusEndpoint radiusServer = this.getProxyServer(request, radiusClient);
         if (radiusServer != null) {
            RadiusProxyConnection proxyConnection = new RadiusProxyConnection(radiusServer, radiusClient, request, localAddress.getPort());
            logger.info("proxy packet to " + proxyConnection);
            this.proxyPacket(request, proxyConnection);
            return null;
         } else {
            return super.handlePacket(localAddress, remoteAddress, request, sharedSecret);
         }
      }
   }

   protected void proxyPacketReceived(RadiusPacket packet, InetSocketAddress remote) throws IOException, RadiusException {
      List proxyStates = packet.getAttributes(33);
      if (proxyStates != null && proxyStates.size() != 0) {
         RadiusAttribute proxyState = (RadiusAttribute)proxyStates.get(proxyStates.size() - 1);
         String state = new String(proxyState.getAttributeData());
         RadiusProxyConnection proxyConnection = (RadiusProxyConnection)this.proxyConnections.remove(state);
         if (proxyConnection == null) {
            logger.warn("received packet on proxy port without saved proxy connection - duplicate?");
         } else {
            RadiusEndpoint client = proxyConnection.getRadiusClient();
            if (logger.isInfoEnabled()) {
               logger.info("received proxy packet: " + packet);
               logger.info("forward packet to " + client.getEndpointAddress().toString() + " with secret " + client.getSharedSecret());
            }

            packet.removeLastAttribute(33);
            RadiusPacket answer = new RadiusPacket(packet.getPacketType(), packet.getPacketIdentifier(), packet.getAttributes());
            DatagramPacket datagram = this.makeDatagramPacket(answer, client.getSharedSecret(), client.getEndpointAddress().getAddress(), client.getEndpointAddress().getPort(), proxyConnection.getPacket());
            DatagramSocket socket;
            if (proxyConnection.getPort() == this.getAuthPort()) {
               socket = this.getAuthSocket();
            } else {
               socket = this.getAcctSocket();
            }

            socket.send(datagram);
         }
      } else {
         throw new RadiusException("proxy packet without Proxy-State attribute");
      }
   }

   protected void proxyPacket(RadiusPacket packet, RadiusProxyConnection proxyConnection) throws IOException {
      Class var3 = RadiusProxy.class;
      synchronized(RadiusProxy.class) {
         ++this.proxyIndex;
         String proxyIndexStr = Integer.toString(this.proxyIndex);
         packet.addAttribute(new RadiusAttribute(33, proxyIndexStr.getBytes()));
         this.proxyConnections.put(proxyIndexStr, proxyConnection);
      }

      InetAddress serverAddress = proxyConnection.getRadiusServer().getEndpointAddress().getAddress();
      int serverPort = proxyConnection.getRadiusServer().getEndpointAddress().getPort();
      String serverSecret = proxyConnection.getRadiusServer().getSharedSecret();
      byte[] auth = packet.getAuthenticator();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      packet.encodeRequestPacket(bos, serverSecret);
      byte[] data = bos.toByteArray();
      DatagramPacket datagram = new DatagramPacket(data, data.length, serverAddress, serverPort);
      packet.setAuthenticator(auth);
      DatagramSocket proxySocket = this.getProxySocket();
      proxySocket.send(datagram);
   }
}
