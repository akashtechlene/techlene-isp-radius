package org.tinyradius.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;

public class RadiusClient {
   private int authPort;
   private int acctPort;
   private String hostName;
   private String sharedSecret;
   private DatagramSocket socket;
   private int retryCount;
   private int socketTimeout;
   private static Logger logger = LoggerFactory.getLogger("radiuslog");

   public RadiusClient(String hostName, String sharedSecret) {
      this.authPort = 1812;
      this.acctPort = 1813;
      this.hostName = null;
      this.sharedSecret = null;
      this.socket = null;
      this.retryCount = 3;
      this.socketTimeout = 3000;
      this.setHostName(hostName);
      this.setSharedSecret(sharedSecret);
   }

   public RadiusClient(RadiusEndpoint client) {
      this(client.getEndpointAddress().getAddress().getHostAddress(), client.getSharedSecret());
   }

   public synchronized boolean authenticate(String userName, String password) throws IOException, RadiusException {
      AccessRequest request = new AccessRequest(userName, password);
      RadiusPacket response = this.authenticate(request);
      return response.getPacketType() == 2;
   }

   public synchronized RadiusPacket authenticate(AccessRequest request) throws IOException, RadiusException {
      if (logger.isInfoEnabled()) {
         logger.info("send Access-Request packet: " + request);
      }

      RadiusPacket response = this.communicate(request, this.getAuthPort());
      if (logger.isInfoEnabled()) {
         logger.info("received packet: " + response);
      }

      return response;
   }

   public synchronized RadiusPacket account(AccountingRequest request) throws IOException, RadiusException {
      if (logger.isInfoEnabled()) {
         logger.info("send Accounting-Request packet: " + request);
      }

      RadiusPacket response = this.communicate(request, this.getAcctPort());
      if (logger.isInfoEnabled()) {
         logger.info("received packet: " + response);
      }

      return response;
   }

   public void close() {
      if (this.socket != null) {
         this.socket.close();
      }

   }

   public int getAuthPort() {
      return this.authPort;
   }

   public void setAuthPort(int authPort) {
      if (authPort >= 1 && authPort <= 65535) {
         this.authPort = authPort;
      } else {
         throw new IllegalArgumentException("bad port number");
      }
   }

   public String getHostName() {
      return this.hostName;
   }

   public void setHostName(String hostName) {
      if (hostName != null && hostName.length() != 0) {
         this.hostName = hostName;
      } else {
         throw new IllegalArgumentException("host name must not be empty");
      }
   }

   public int getRetryCount() {
      return this.retryCount;
   }

   public void setRetryCount(int retryCount) {
      if (retryCount < 1) {
         throw new IllegalArgumentException("retry count must be positive");
      } else {
         this.retryCount = retryCount;
      }
   }

   public String getSharedSecret() {
      return this.sharedSecret;
   }

   public void setSharedSecret(String sharedSecret) {
      if (sharedSecret != null && sharedSecret.length() != 0) {
         this.sharedSecret = sharedSecret;
      } else {
         throw new IllegalArgumentException("shared secret must not be empty");
      }
   }

   public int getSocketTimeout() {
      return this.socketTimeout;
   }

   public void setSocketTimeout(int socketTimeout) throws SocketException {
      if (socketTimeout < 1) {
         throw new IllegalArgumentException("socket tiemout must be positive");
      } else {
         this.socketTimeout = socketTimeout;
         if (this.socket != null) {
            this.socket.setSoTimeout(socketTimeout);
         }

      }
   }

   public void setAcctPort(int acctPort) {
      if (acctPort >= 1 && acctPort <= 65535) {
         this.acctPort = acctPort;
      } else {
         throw new IllegalArgumentException("bad port number");
      }
   }

   public int getAcctPort() {
      return this.acctPort;
   }

   public RadiusPacket communicate(RadiusPacket request, int port) throws IOException, RadiusException {
      DatagramPacket packetIn = new DatagramPacket(new byte[4096], 4096);
      DatagramPacket packetOut = this.makeDatagramPacket(request, port);
      DatagramSocket socket = this.getSocket();

      for(int i = 1; i <= this.getRetryCount(); ++i) {
         try {
            socket.send(packetOut);
            socket.receive(packetIn);
            return this.makeRadiusPacket(packetIn, request);
         } catch (IOException var8) {
            if (i == this.getRetryCount()) {
               if (logger.isTraceEnabled()) {
                  if (var8 instanceof SocketTimeoutException) {
                     logger.error("communication failure (timeout), no more retries");
                  } else {
                     logger.error("communication failure, no more retries", var8);
                  }
               }

               throw var8;
            }

            if (logger.isInfoEnabled()) {
               logger.info("communication failure, retry " + i);
            }
         }
      }

      return null;
   }

   public static RadiusPacket communicate(RadiusEndpoint remoteServer, RadiusPacket request) throws RadiusException, IOException {
      RadiusClient rc = new RadiusClient(remoteServer);
      return rc.communicate(request, remoteServer.getEndpointAddress().getPort());
   }

   protected DatagramSocket getSocket() throws SocketException {
      if (this.socket == null) {
         this.socket = new DatagramSocket();
         this.socket.setSoTimeout(this.getSocketTimeout());
      }

      return this.socket;
   }

   protected DatagramPacket makeDatagramPacket(RadiusPacket packet, int port) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      packet.encodeRequestPacket(bos, this.getSharedSecret());
      byte[] data = bos.toByteArray();
      InetAddress address = InetAddress.getByName(this.getHostName());
      DatagramPacket datagram = new DatagramPacket(data, data.length, address, port);
      return datagram;
   }

   protected RadiusPacket makeRadiusPacket(DatagramPacket packet, RadiusPacket request) throws IOException, RadiusException {
      ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
      return RadiusPacket.decodeResponsePacket(in, this.getSharedSecret(), request);
   }
}
