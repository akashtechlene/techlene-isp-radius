package org.tinyradius.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.workers.RadiusAcctProcessor;
import org.tinyradius.workers.RadiusAuthProcessor;

public abstract class RadiusServer {
   private ExecutorService authExecutor;
   private ExecutorService acctExecutor;
   private InetAddress listenAddress = null;
   private int authPort = 1812;
   private int acctPort = 1813;
   private DatagramSocket authSocket = null;
   private DatagramSocket acctSocket = null;
   private int socketTimeout = 3000;
   private int socketRecvBufferSize = 51200;
   private List receivedPackets = new LinkedList();
   private long duplicateInterval = 30000L;
   private boolean closing = false;
   protected static Logger logger = LoggerFactory.getLogger("radiuslog");
   private int authMinThreads = 10;
   private int authMaxThreads = 100;
   private int acctMinThreads = 10;
   private int acctMaxThreads = 100;
   private int threadBenchTime = 200;

   public abstract String getSharedSecret(InetSocketAddress var1);

   public abstract String getUserPassword(String var1);

   public RadiusPacket accessRequestReceived(AccessRequest accessRequest, InetSocketAddress client) throws RadiusException {
      String plaintext = this.getUserPassword(accessRequest.getUserName());
      int type = 3;
      if (plaintext != null && accessRequest.verifyPassword(plaintext)) {
         type = 2;
      }

      RadiusPacket answer = new RadiusPacket(type, accessRequest.getPacketIdentifier());
      this.copyProxyState(accessRequest, answer);
      return answer;
   }

   public RadiusPacket accountingRequestReceived(AccountingRequest accountingRequest, InetSocketAddress client) throws RadiusException {
      RadiusPacket answer = new RadiusPacket(5, accountingRequest.getPacketIdentifier());
      this.copyProxyState(accountingRequest, answer);
      return answer;
   }

   public void start(boolean listenAuth, boolean listenAcct) {
      if (listenAuth) {
         (new Thread() {
            public void run() {
               this.setName("Radius Auth Listener");

               try {
                  RadiusServer.logger.info("starting RadiusAuthListener on port " + RadiusServer.this.getAuthPort());
                  RadiusServer.this.listenAuth();
                  RadiusServer.logger.info("RadiusAuthListener is being terminated");
               } catch (Exception e) {
                  RadiusServer.logger.trace("auth thread stopped by exception", e);
               } finally {
                  RadiusServer.this.authSocket.close();
                  RadiusServer.logger.debug("auth socket closed");
               }

            }
         }).start();
      }

      if (listenAcct) {
         (new Thread() {
            public void run() {
               this.setName("Radius Acct Listener");

               try {
                  RadiusServer.logger.info("starting RadiusAcctListener on port " + RadiusServer.this.getAcctPort());
                  RadiusServer.this.listenAcct();
                  RadiusServer.logger.info("RadiusAcctListener is being terminated");
               } catch (Exception e) {
                  RadiusServer.logger.trace("acct thread stopped by exception" + e, e);
               } finally {
                  RadiusServer.this.acctSocket.close();
                  RadiusServer.logger.debug("acct socket closed");
               }

            }
         }).start();
      }

   }

   public void stop() {
      logger.info("stopping Radius server");
      this.closing = true;
      if (this.authSocket != null) {
         this.authSocket.close();
      }

      if (this.acctSocket != null) {
         this.acctSocket.close();
      }

   }

   public int getAuthPort() {
      return this.authPort;
   }

   public ExecutorService getAuthPool() {
      if (this.authExecutor == null) {
         logger.debug("AUTH POOL Initialized: min:" + this.authMinThreads + ",Max:" + this.authMaxThreads);
         this.authExecutor = new ThreadPoolExecutor(this.authMinThreads, this.authMaxThreads, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("AUTHPOOL-%d").build());
      }

      return this.authExecutor;
   }

   public ExecutorService getAcctPool() {
      if (this.acctExecutor == null) {
         logger.debug("ACCT POOL Initialized: min:" + this.acctMinThreads + ",Max:" + this.acctMaxThreads);
         this.acctExecutor = new ThreadPoolExecutor(this.acctMinThreads, this.acctMaxThreads, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("ACCTPOOL-%d").build());
      }

      return this.acctExecutor;
   }

   public void setAuthPort(int authPort) {
      if (authPort >= 1 && authPort <= 65535) {
         this.authPort = authPort;
         this.authSocket = null;
      } else {
         throw new IllegalArgumentException("bad port number");
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
         if (this.authSocket != null) {
            this.authSocket.setSoTimeout(socketTimeout);
         }

         if (this.acctSocket != null) {
            this.acctSocket.setSoTimeout(socketTimeout);
         }

      }
   }

   public void setAcctPort(int acctPort) {
      if (acctPort >= 1 && acctPort <= 65535) {
         this.acctPort = acctPort;
         this.acctSocket = null;
      } else {
         throw new IllegalArgumentException("bad port number");
      }
   }

   public int getAcctPort() {
      return this.acctPort;
   }

   public long getDuplicateInterval() {
      return this.duplicateInterval;
   }

   public void setDuplicateInterval(long duplicateInterval) {
      if (duplicateInterval <= 0L) {
         throw new IllegalArgumentException("duplicate interval must be positive");
      } else {
         this.duplicateInterval = duplicateInterval;
      }
   }

   public InetAddress getListenAddress() {
      return this.listenAddress;
   }

   public void setListenAddress(InetAddress listenAddress) {
      this.listenAddress = listenAddress;
   }

   protected void copyProxyState(RadiusPacket request, RadiusPacket answer) {
      for(RadiusAttribute proxyStateAttr : request.getAttributes(33)) {
         answer.addAttribute(proxyStateAttr);
      }

   }

   protected void listenAuth() throws SocketException {
      this.listen(this.getAuthSocket());
   }

   protected void listenAcct() throws SocketException {
      this.listen(this.getAcctSocket());
   }

   protected void listen(DatagramSocket s) {
      DatagramPacket packetIn = new DatagramPacket(new byte[4096], 4096);

      while(true) {
         try {
            try {
               logger.trace("about to call socket.receive()");
               s.receive(packetIn);
               Thread.yield();
               if (logger.isDebugEnabled()) {
                  logger.debug("receive buffer size = " + s.getReceiveBufferSize());
               }
            } catch (SocketException se) {
               if (!this.closing) {
                  logger.error("SocketException during s.receive() -> retry", se);
                  continue;
               }

               logger.info("got closing signal - end listen thread");
               return;
            }

            try {
               InetSocketAddress localAddress = (InetSocketAddress)s.getLocalSocketAddress();
               InetSocketAddress remoteAddress = new InetSocketAddress(packetIn.getAddress(), packetIn.getPort());
               String secret = this.getSharedSecret(remoteAddress);
               RadiusPacket request = this.makeRadiusPacket(packetIn, secret);
               packetIn.setData(new byte[4096], 0, 4096);
               this.handlePacket(localAddress, remoteAddress, request, secret);
            } catch (SocketTimeoutException var7) {
               logger.trace("normal socket timeout");
            } catch (IOException ioe) {
               logger.error("communication error", ioe);
            } catch (RadiusException re) {
               logger.error("malformed Radius packet:" + re, re);
            } catch (Exception e) {
               logger.error("ERROR:" + e, e);
            }
         } catch (SocketTimeoutException var12) {
            logger.trace("normal socket timeout");
         } catch (IOException ioe) {
            logger.error("communication error", ioe);
         }
      }
   }

   protected RadiusPacket handlePacket(InetSocketAddress localAddress, InetSocketAddress remoteAddress, RadiusPacket request, String sharedSecret) throws RadiusException, IOException {
      RadiusPacket response = null;
      if (!this.isPacketDuplicate(request, remoteAddress)) {
         if (localAddress.getPort() == this.getAuthPort()) {
            RadiusAuthProcessor processor = new RadiusAuthProcessor(this.getAuthSocket(), remoteAddress, sharedSecret, request);
            this.getAuthPool().execute(processor);
         } else if (localAddress.getPort() == this.getAcctPort()) {
            RadiusAcctProcessor processor = new RadiusAcctProcessor(this.getAcctSocket(), remoteAddress, sharedSecret, request);
            this.getAcctPool().execute(processor);
         }
      } else {
         logger.info("ignore duplicate packet");
      }

      return response;
   }

   protected DatagramSocket getAuthSocket() throws SocketException {
      if (this.authSocket == null) {
         if (this.getListenAddress() == null) {
            this.authSocket = new DatagramSocket(this.getAuthPort());
         } else {
            this.authSocket = new DatagramSocket(this.getAuthPort(), this.getListenAddress());
         }

         this.authSocket.setSoTimeout(this.getSocketTimeout());
      }

      return this.authSocket;
   }

   protected DatagramSocket getAcctSocket() throws SocketException {
      if (this.acctSocket == null) {
         if (this.getListenAddress() == null) {
            this.acctSocket = new DatagramSocket(this.getAcctPort());
         } else {
            this.acctSocket = new DatagramSocket(this.getAcctPort(), this.getListenAddress());
         }

         this.acctSocket.setSoTimeout(this.getSocketTimeout());
      }

      return this.acctSocket;
   }

   protected DatagramPacket makeDatagramPacket(RadiusPacket packet, String secret, InetAddress address, int port, RadiusPacket request) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      packet.encodeResponsePacket(bos, secret, request);
      byte[] data = bos.toByteArray();
      DatagramPacket datagram = new DatagramPacket(data, data.length, address, port);
      return datagram;
   }

   protected RadiusPacket makeRadiusPacket(DatagramPacket packet, String sharedSecret) throws IOException, RadiusException {
      ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
      return RadiusPacket.decodeRequestPacket(in, sharedSecret);
   }

   protected boolean isPacketDuplicate(RadiusPacket packet, InetSocketAddress address) {
      long now = System.currentTimeMillis();
      long intervalStart = now - this.getDuplicateInterval();
      byte[] authenticator = packet.getAuthenticator();
      synchronized(this.receivedPackets) {
         Iterator i = this.receivedPackets.iterator();

         while(i.hasNext()) {
            ReceivedPacket p = (ReceivedPacket)i.next();
            if (p.receiveTime < intervalStart) {
               i.remove();
            } else if (p.address.equals(address) && p.packetIdentifier == packet.getPacketIdentifier()) {
               if (authenticator != null && p.authenticator != null) {
                  return Arrays.equals(p.authenticator, authenticator);
               }

               return true;
            }
         }

         ReceivedPacket rp = new ReceivedPacket();
         rp.address = address;
         rp.packetIdentifier = packet.getPacketIdentifier();
         rp.receiveTime = now;
         rp.authenticator = authenticator;
         this.receivedPackets.add(rp);
         return false;
      }
   }

   public int getAuthMinThreads() {
      return this.authMinThreads;
   }

   public void setAuthMinThreads(int authMinThreads) {
      this.authMinThreads = authMinThreads;
   }

   public int getAuthMaxThreads() {
      return this.authMaxThreads;
   }

   public void setAuthMaxThreads(int authMaxThreads) {
      this.authMaxThreads = authMaxThreads;
   }

   public int getAcctMinThreads() {
      return this.acctMinThreads;
   }

   public void setAcctMinThreads(int acctMinThreads) {
      this.acctMinThreads = acctMinThreads;
   }

   public int getAcctMaxThreads() {
      return this.acctMaxThreads;
   }

   public void setAcctMaxThreads(int acctMaxThreads) {
      this.acctMaxThreads = acctMaxThreads;
   }

   public int getThreadBenchTime() {
      return this.threadBenchTime;
   }

   public void setThreadBenchTime(int threadBenchTime) {
      this.threadBenchTime = threadBenchTime;
   }
}
