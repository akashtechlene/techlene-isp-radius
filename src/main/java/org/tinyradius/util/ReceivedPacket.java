//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.util;

import java.net.InetSocketAddress;

class ReceivedPacket {
   public int packetIdentifier;
   public long receiveTime;
   public InetSocketAddress address;
   public byte[] authenticator;

   ReceivedPacket() {
   }
}
