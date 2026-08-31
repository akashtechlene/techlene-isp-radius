package org.tinyradius.util;

import java.net.InetSocketAddress;

class ReceivedPacket {
   public int packetIdentifier;
   public long receiveTime;
   public InetSocketAddress address;
   public byte[] authenticator;
}
