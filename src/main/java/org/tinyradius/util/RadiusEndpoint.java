//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.util;

import java.net.InetSocketAddress;

public class RadiusEndpoint {
   private InetSocketAddress endpointAddress;
   private String sharedSecret;

   public RadiusEndpoint(InetSocketAddress remoteAddress, String sharedSecret) {
      this.endpointAddress = remoteAddress;
      this.sharedSecret = sharedSecret;
   }

   public InetSocketAddress getEndpointAddress() {
      return this.endpointAddress;
   }

   public String getSharedSecret() {
      return this.sharedSecret;
   }
}
