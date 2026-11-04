//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.packet;

import java.security.MessageDigest;
import org.tinyradius.util.RadiusUtil;

public class CoaRequest extends RadiusPacket {
   public CoaRequest() {
      super(43, getNextPacketIdentifier());
   }

   public CoaRequest(int RequestType) {
      super(RequestType, getNextPacketIdentifier());
   }

   protected byte[] updateRequestAuthenticator(String sharedSecret, int packetLength, byte[] attributes) {
      byte[] authenticator = new byte[16];

      for(int i = 0; i < 16; ++i) {
         authenticator[i] = 0;
      }

      MessageDigest md5 = this.getMd5Digest();
      md5.reset();
      md5.update((byte)this.getPacketType());
      md5.update((byte)this.getPacketIdentifier());
      md5.update((byte)(packetLength >> 8));
      md5.update((byte)(packetLength & 255));
      md5.update(authenticator, 0, authenticator.length);
      md5.update(attributes, 0, attributes.length);
      md5.update(RadiusUtil.getUtf8Bytes(sharedSecret));
      return md5.digest();
   }
}
