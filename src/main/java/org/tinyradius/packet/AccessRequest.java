package org.tinyradius.packet;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.attribute.StringAttribute;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusUtil;

public class AccessRequest extends RadiusPacket {
   public static final String AUTH_PAP = "pap";
   public static final String AUTH_CHAP = "chap";
   private String password;
   private String authProtocol = "pap";
   private byte[] chapPassword;
   private byte[] chapChallenge;
   private static SecureRandom random = new SecureRandom();
   private static final int USER_NAME = 1;
   private static final int USER_PASSWORD = 2;
   private static final int CHAP_PASSWORD = 3;
   private static final int CHAP_CHALLENGE = 60;
   private static Logger logger = LoggerFactory.getLogger("radiuslog");

   public AccessRequest() {
   }

   public AccessRequest(String userName, String userPassword) {
      super(1, getNextPacketIdentifier());
      this.setUserName(userName);
      this.setUserPassword(userPassword);
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

   public void setUserPassword(String userPassword) {
      if (userPassword != null && userPassword.length() != 0) {
         this.password = userPassword;
      } else {
         throw new IllegalArgumentException("password is empty");
      }
   }

   public String getUserPassword() {
      return this.password;
   }

   public String getUserName() {
      List attrs = this.getAttributes(1);
      if (attrs.size() >= 1 && attrs.size() <= 1) {
         RadiusAttribute ra = (RadiusAttribute)attrs.get(0);
         return ((StringAttribute)ra).getAttributeValue();
      } else {
         throw new RuntimeException("exactly one User-Name attribute required");
      }
   }

   public String getAuthProtocol() {
      return this.authProtocol;
   }

   public void setAuthProtocol(String authProtocol) {
      if (authProtocol == null || !authProtocol.equals("pap") && !authProtocol.equals("chap")) {
         throw new IllegalArgumentException("protocol must be pap or chap");
      } else {
         this.authProtocol = authProtocol;
      }
   }

   public boolean verifyPassword(String plaintext) throws RadiusException {
      if (plaintext != null && plaintext.length() != 0) {
         return this.getAuthProtocol().equals("chap") ? this.verifyChapPassword(plaintext) : this.getUserPassword().equals(plaintext);
      } else {
         throw new IllegalArgumentException("password is empty");
      }
   }

   protected void decodeRequestAttributes(String sharedSecret) throws RadiusException {
      RadiusAttribute userPassword = this.getAttribute(2);
      RadiusAttribute chapPassword = this.getAttribute(3);
      RadiusAttribute chapChallenge = this.getAttribute(60);
      if (userPassword != null) {
         this.setAuthProtocol("pap");
         this.password = this.decodePapPassword(userPassword.getAttributeData(), RadiusUtil.getUtf8Bytes(sharedSecret));
         userPassword.setAttributeData(RadiusUtil.getUtf8Bytes(this.password));
      } else {
         if (chapPassword == null || chapChallenge == null) {
            throw new RadiusException("Access-Request: User-Password or CHAP-Password/CHAP-Challenge missing");
         }

         this.setAuthProtocol("chap");
         this.chapPassword = chapPassword.getAttributeData();
         this.chapChallenge = chapChallenge.getAttributeData();
      }

   }

   protected void encodeRequestAttributes(String sharedSecret) {
      if (this.password != null && this.password.length() != 0) {
         if (this.getAuthProtocol().equals("pap")) {
            byte[] pass = this.encodePapPassword(RadiusUtil.getUtf8Bytes(this.password), RadiusUtil.getUtf8Bytes(sharedSecret));
            this.removeAttributes(2);
            this.addAttribute(new RadiusAttribute(2, pass));
         } else if (this.getAuthProtocol().equals("chap")) {
            byte[] challenge = this.createChapChallenge();
            byte[] pass = this.encodeChapPassword(this.password, challenge);
            this.removeAttributes(3);
            this.removeAttributes(60);
            this.addAttribute(new RadiusAttribute(3, pass));
            this.addAttribute(new RadiusAttribute(60, challenge));
         }

      }
   }

   private byte[] encodePapPassword(byte[] userPass, byte[] sharedSecret) {
      byte[] userPassBytes = null;
      if (userPass.length > 128) {
         userPassBytes = new byte[128];
         System.arraycopy(userPass, 0, userPassBytes, 0, 128);
      } else {
         userPassBytes = userPass;
      }

      byte[] encryptedPass = null;
      if (userPassBytes.length < 128) {
         if (userPassBytes.length % 16 == 0) {
            encryptedPass = new byte[userPassBytes.length];
         } else {
            encryptedPass = new byte[userPassBytes.length / 16 * 16 + 16];
         }
      } else {
         encryptedPass = new byte[128];
      }

      System.arraycopy(userPassBytes, 0, encryptedPass, 0, userPassBytes.length);

      for(int i = userPassBytes.length; i < encryptedPass.length; ++i) {
         encryptedPass[i] = 0;
      }

      MessageDigest md5 = this.getMd5Digest();
      byte[] lastBlock = new byte[16];

      for(int i = 0; i < encryptedPass.length; i += 16) {
         md5.reset();
         md5.update(sharedSecret);
         md5.update(i == 0 ? this.getAuthenticator() : lastBlock);
         byte[] bn = md5.digest();
         System.arraycopy(encryptedPass, i, lastBlock, 0, 16);

         for(int j = 0; j < 16; ++j) {
            encryptedPass[i + j] ^= bn[j];
         }
      }

      return encryptedPass;
   }

   private String decodePapPassword(byte[] encryptedPass, byte[] sharedSecret) throws RadiusException {
      if (encryptedPass != null && encryptedPass.length >= 16) {
         MessageDigest md5 = this.getMd5Digest();
         byte[] lastBlock = new byte[16];

         for(int i = 0; i < encryptedPass.length; i += 16) {
            md5.reset();
            md5.update(sharedSecret);
            md5.update(i == 0 ? this.getAuthenticator() : lastBlock);
            byte[] bn = md5.digest();
            System.arraycopy(encryptedPass, i, lastBlock, 0, 16);

            for(int j = 0; j < 16; ++j) {
               encryptedPass[i + j] ^= bn[j];
            }
         }

         int len;
         for(len = encryptedPass.length; len > 0 && encryptedPass[len - 1] == 0; --len) {
         }

         byte[] passtrunc = new byte[len];
         System.arraycopy(encryptedPass, 0, passtrunc, 0, len);
         return RadiusUtil.getStringFromUtf8(passtrunc);
      } else {
         logger.warn("invalid Radius packet: User-Password attribute with malformed PAP password, length = " + encryptedPass.length + ", but length must be greater than 15");
         throw new RadiusException("malformed User-Password attribute");
      }
   }

   private byte[] createChapChallenge() {
      byte[] challenge = new byte[16];
      random.nextBytes(challenge);
      return challenge;
   }

   private byte[] encodeChapPassword(String plaintext, byte[] chapChallenge) {
      byte chapIdentifier = (byte)random.nextInt(256);
      byte[] chapPassword = new byte[17];
      chapPassword[0] = chapIdentifier;
      MessageDigest md5 = this.getMd5Digest();
      md5.reset();
      md5.update(chapIdentifier);
      md5.update(RadiusUtil.getUtf8Bytes(plaintext));
      byte[] chapHash = md5.digest(chapChallenge);
      System.arraycopy(chapHash, 0, chapPassword, 1, 16);
      return chapPassword;
   }

   private boolean verifyChapPassword(String plaintext) throws RadiusException {
      if (plaintext != null && plaintext.length() != 0) {
         if (this.chapChallenge != null && this.chapChallenge.length == 16) {
            if (this.chapPassword != null && this.chapPassword.length == 17) {
               byte chapIdentifier = this.chapPassword[0];
               MessageDigest md5 = this.getMd5Digest();
               md5.reset();
               md5.update(chapIdentifier);
               md5.update(RadiusUtil.getUtf8Bytes(plaintext));
               byte[] chapHash = md5.digest(this.chapChallenge);

               for(int i = 0; i < 16; ++i) {
                  if (chapHash[i] != this.chapPassword[i + 1]) {
                     return false;
                  }
               }

               return true;
            } else {
               throw new RadiusException("CHAP password must be 17 bytes");
            }
         } else {
            throw new RadiusException("CHAP challenge must be 16 bytes");
         }
      } else {
         throw new IllegalArgumentException("plaintext must not be empty");
      }
   }
}
