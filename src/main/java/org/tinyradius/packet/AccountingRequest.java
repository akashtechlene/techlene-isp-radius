//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.packet;

import java.security.MessageDigest;
import java.util.List;
import org.tinyradius.attribute.IntegerAttribute;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.attribute.StringAttribute;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusUtil;

public class AccountingRequest extends RadiusPacket {
   public static final int ACCT_STATUS_TYPE_START = 1;
   public static final int ACCT_STATUS_TYPE_STOP = 2;
   public static final int ACCT_STATUS_TYPE_INTERIM_UPDATE = 3;
   public static final int ACCT_STATUS_TYPE_ACCOUNTING_ON = 7;
   public static final int ACCT_STATUS_TYPE_ACCOUNTING_OFF = 8;
   private static final int USER_NAME = 1;
   private static final int ACCT_STATUS_TYPE = 40;

   public AccountingRequest(String userName, int acctStatusType) {
      super(4, getNextPacketIdentifier());
      this.setUserName(userName);
      this.setAcctStatusType(acctStatusType);
   }

   public AccountingRequest() {
   }

   public void setUserName(String userName) {
      if (userName == null) {
         throw new NullPointerException("user name not set");
      } else if (userName.length() == 0) {
         throw new IllegalArgumentException("empty user name not allowed");
      } else {
         this.removeAttributes(1);
         this.addAttribute(new StringAttribute(1, userName));
      }
   }

   public String getUserName() throws RadiusException {
      List attrs = this.getAttributes(1);
      if (attrs.size() >= 1 && attrs.size() <= 1) {
         RadiusAttribute ra = (RadiusAttribute)attrs.get(0);
         return ((StringAttribute)ra).getAttributeValue();
      } else {
         throw new RuntimeException("exactly one User-Name attribute required");
      }
   }

   public void setAcctStatusType(int acctStatusType) {
      if (acctStatusType >= 1 && acctStatusType <= 15) {
         this.removeAttributes(40);
         this.addAttribute(new IntegerAttribute(40, acctStatusType));
      } else {
         throw new IllegalArgumentException("bad Acct-Status-Type");
      }
   }

   public int getAcctStatusType() throws RadiusException {
      RadiusAttribute ra = this.getAttribute(40);
      return ra == null ? -1 : ((IntegerAttribute)ra).getAttributeValueInt();
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

   protected void checkRequestAuthenticator(String sharedSecret, int packetLength, byte[] attributes) throws RadiusException {
      byte[] expectedAuthenticator = this.updateRequestAuthenticator(sharedSecret, packetLength, attributes);
      byte[] receivedAuth = this.getAuthenticator();

      for(int i = 0; i < 16; ++i) {
         if (expectedAuthenticator[i] != receivedAuth[i]) {
            throw new RadiusException("request authenticator invalid");
         }
      }

   }
}
