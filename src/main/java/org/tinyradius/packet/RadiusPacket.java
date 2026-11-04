//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.attribute.VendorSpecificAttribute;
import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.dictionary.DefaultDictionary;
import org.tinyradius.dictionary.Dictionary;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusUtil;

public class RadiusPacket {
   public static final int ACCESS_REQUEST = 1;
   public static final int ACCESS_ACCEPT = 2;
   public static final int ACCESS_REJECT = 3;
   public static final int ACCOUNTING_REQUEST = 4;
   public static final int ACCOUNTING_RESPONSE = 5;
   public static final int ACCOUNTING_STATUS = 6;
   public static final int PASSWORD_REQUEST = 7;
   public static final int PASSWORD_ACCEPT = 8;
   public static final int PASSWORD_REJECT = 9;
   public static final int ACCOUNTING_MESSAGE = 10;
   public static final int ACCESS_CHALLENGE = 11;
   public static final int STATUS_SERVER = 12;
   public static final int STATUS_CLIENT = 13;
   public static final int DISCONNECT_REQUEST = 40;
   public static final int DISCONNECT_ACK = 41;
   public static final int DISCONNECT_NAK = 42;
   public static final int COA_REQUEST = 43;
   public static final int COA_ACK = 44;
   public static final int COA_NAK = 45;
   public static final int STATUS_REQUEST = 46;
   public static final int STATUS_ACCEPT = 47;
   public static final int STATUS_REJECT = 48;
   public static final int RESERVED = 255;
   public static final int MAX_PACKET_LENGTH = 4096;
   public static final int RADIUS_HEADER_LENGTH = 20;
   private int packetType;
   private int packetIdentifier;
   private List attributes;
   private MessageDigest md5Digest;
   private byte[] authenticator;
   private Dictionary dictionary;
   private static int nextPacketId = 0;
   private static SecureRandom random = new SecureRandom();

   public RadiusPacket(int type) {
      this(type, getNextPacketIdentifier(), new ArrayList());
   }

   public RadiusPacket(int type, int identifier) {
      this(type, identifier, new ArrayList());
   }

   public RadiusPacket(int type, int identifier, List attributes) {
      this.packetType = 0;
      this.packetIdentifier = 0;
      this.attributes = new ArrayList();
      this.md5Digest = null;
      this.authenticator = null;
      this.dictionary = DefaultDictionary.getDefaultDictionary();
      this.setPacketType(type);
      this.setPacketIdentifier(identifier);
      this.setAttributes(attributes);
   }

   public RadiusPacket() {
      this.packetType = 0;
      this.packetIdentifier = 0;
      this.attributes = new ArrayList();
      this.md5Digest = null;
      this.authenticator = null;
      this.dictionary = DefaultDictionary.getDefaultDictionary();
   }

   public int getPacketIdentifier() {
      return this.packetIdentifier;
   }

   public void setPacketIdentifier(int identifier) {
      if (identifier >= 0 && identifier <= 255) {
         this.packetIdentifier = identifier;
      } else {
         throw new IllegalArgumentException("packet identifier out of bounds");
      }
   }

   public int getPacketType() {
      return this.packetType;
   }

   public String getPacketTypeName() {
      switch (this.getPacketType()) {
         case 1:
            return "Access-Request";
         case 2:
            return "Access-Accept";
         case 3:
            return "Access-Reject";
         case 4:
            return "Accounting-Request";
         case 5:
            return "Accounting-Response";
         case 6:
            return "Accounting-Status";
         case 7:
            return "Password-Request";
         case 8:
            return "Password-Accept";
         case 9:
            return "Password-Reject";
         case 10:
            return "Accounting-Message";
         case 11:
            return "Access-Challenge";
         case 12:
            return "Status-Server";
         case 13:
            return "Status-Client";
         case 40:
            return "Disconnect-Request";
         case 41:
            return "Disconnect-ACK";
         case 42:
            return "Disconnect-NAK";
         case 43:
            return "CoA-Request";
         case 44:
            return "CoA-ACK";
         case 45:
            return "CoA-NAK";
         case 46:
            return "Status-Request";
         case 47:
            return "Status-Accept";
         case 48:
            return "Status-Reject";
         case 255:
            return "Reserved";
         default:
            return "Unknown (" + this.getPacketType() + ")";
      }
   }

   public void setPacketType(int type) {
      if (type >= 1 && type <= 255) {
         this.packetType = type;
      } else {
         throw new IllegalArgumentException("packet type out of bounds");
      }
   }

   public void setAttributes(List attributes) {
      if (attributes == null) {
         throw new NullPointerException("attributes list is null");
      } else {
         Iterator i = attributes.iterator();

         Object element;
         do {
            if (!i.hasNext()) {
               this.attributes = attributes;
               return;
            }

            element = i.next();
         } while(element instanceof RadiusAttribute);

         throw new IllegalArgumentException("attribute not an instance of RadiusAttribute");
      }
   }

   public void addAttribute(RadiusAttribute attribute) {
      if (attribute == null) {
         throw new NullPointerException("attribute is null");
      } else {
         attribute.setDictionary(this.getDictionary());
         if (attribute.getVendorId() == -1) {
            this.attributes.add(attribute);
         } else {
            VendorSpecificAttribute vsa = new VendorSpecificAttribute(attribute.getVendorId());
            vsa.addSubAttribute(attribute);
            this.attributes.add(vsa);
         }

      }
   }

   public void addAttribute(String typeName, String value) {
      if (typeName != null && typeName.length() != 0) {
         if (value != null && value.length() != 0) {
            AttributeType type = this.dictionary.getAttributeTypeByName(typeName);
            if (type == null) {
               throw new IllegalArgumentException("unknown attribute type '" + typeName + "'");
            } else {
               RadiusAttribute attribute = RadiusAttribute.createRadiusAttribute(this.getDictionary(), type.getVendorId(), type.getTypeCode());
               attribute.setAttributeValue(value);
               this.addAttribute(attribute);
            }
         } else {
            throw new IllegalArgumentException("value is empty");
         }
      } else {
         throw new IllegalArgumentException("type name is empty");
      }
   }

   public void removeAttribute(RadiusAttribute attribute) {
      if (attribute.getVendorId() == -1) {
         if (!this.attributes.remove(attribute)) {
            throw new IllegalArgumentException("no such attribute");
         }
      } else {
         List vsas = this.getVendorAttributes(attribute.getVendorId());
         Iterator i = vsas.iterator();

         while(i.hasNext()) {
            VendorSpecificAttribute vsa = (VendorSpecificAttribute)i.next();
            List sas = vsa.getSubAttributes();
            if (sas.contains(attribute)) {
               vsa.removeSubAttribute(attribute);
               if (sas.size() == 1) {
                  this.removeAttribute(vsa);
               }
            }
         }
      }

   }

   public void removeAttributes(int type) {
      if (type >= 1 && type <= 255) {
         Iterator i = this.attributes.iterator();

         while(i.hasNext()) {
            RadiusAttribute attribute = (RadiusAttribute)i.next();
            if (attribute.getAttributeType() == type) {
               i.remove();
            }
         }

      } else {
         throw new IllegalArgumentException("attribute type out of bounds");
      }
   }

   public void removeLastAttribute(int type) {
      List attrs = this.getAttributes(type);
      if (attrs != null && attrs.size() != 0) {
         RadiusAttribute lastAttribute = (RadiusAttribute)attrs.get(attrs.size() - 1);
         this.removeAttribute(lastAttribute);
      }
   }

   public void removeAttributes(int vendorId, int typeCode) {
      if (vendorId == -1) {
         this.removeAttributes(typeCode);
      } else {
         List vsas = this.getVendorAttributes(vendorId);
         Iterator i = vsas.iterator();

         while(i.hasNext()) {
            VendorSpecificAttribute vsa = (VendorSpecificAttribute)i.next();
            List sas = vsa.getSubAttributes();
            Iterator j = sas.iterator();

            while(j.hasNext()) {
               RadiusAttribute attr = (RadiusAttribute)j.next();
               if (attr.getAttributeType() == typeCode && attr.getVendorId() == vendorId) {
                  j.remove();
               }
            }

            if (sas.size() == 0) {
               this.removeAttribute(vsa);
            }
         }

      }
   }

   public List getAttributes(int attributeType) {
      if (attributeType >= 1 && attributeType <= 255) {
         LinkedList result = new LinkedList();
         Iterator i = this.attributes.iterator();

         while(i.hasNext()) {
            RadiusAttribute a = (RadiusAttribute)i.next();
            if (attributeType == a.getAttributeType()) {
               result.add(a);
            }
         }

         return result;
      } else {
         throw new IllegalArgumentException("attribute type out of bounds");
      }
   }

   public List getAttributes(int vendorId, int attributeType) {
      if (vendorId == -1) {
         return this.getAttributes(attributeType);
      } else {
         LinkedList result = new LinkedList();
         List vsas = this.getVendorAttributes(vendorId);
         Iterator i = vsas.iterator();

         while(i.hasNext()) {
            VendorSpecificAttribute vsa = (VendorSpecificAttribute)i.next();
            List sas = vsa.getSubAttributes();
            Iterator j = sas.iterator();

            while(j.hasNext()) {
               RadiusAttribute attr = (RadiusAttribute)j.next();
               if (attr.getAttributeType() == attributeType && attr.getVendorId() == vendorId) {
                  result.add(attr);
               }
            }
         }

         return result;
      }
   }

   public List getAttributes() {
      return this.attributes;
   }

   public RadiusAttribute getAttribute(int type) {
      List attrs = this.getAttributes(type);
      if (attrs.size() > 1) {
         throw new RuntimeException("multiple attributes of requested type " + type);
      } else {
         return attrs.size() == 0 ? null : (RadiusAttribute)attrs.get(0);
      }
   }

   public RadiusAttribute getAttribute(int vendorId, int type) {
      if (vendorId == -1) {
         return this.getAttribute(type);
      } else {
         List attrs = this.getAttributes(vendorId, type);
         if (attrs.size() > 1) {
            throw new RuntimeException("multiple attributes of requested type " + type);
         } else {
            return attrs.size() == 0 ? null : (RadiusAttribute)attrs.get(0);
         }
      }
   }

   public RadiusAttribute getAttribute(String type) {
      if (type != null && type.length() != 0) {
         AttributeType t = this.dictionary.getAttributeTypeByName(type);
         if (t == null) {
            throw new IllegalArgumentException("unknown attribute type name '" + type + "'");
         } else {
            return this.getAttribute(t.getVendorId(), t.getTypeCode());
         }
      } else {
         throw new IllegalArgumentException("type name is empty");
      }
   }

   public String getAttributeValue(String type) {
      RadiusAttribute attr = this.getAttribute(type);
      return attr == null ? null : attr.getAttributeValue();
   }

   public List getVendorAttributes(int vendorId) {
      LinkedList result = new LinkedList();
      Iterator i = this.attributes.iterator();

      while(i.hasNext()) {
         RadiusAttribute a = (RadiusAttribute)i.next();
         if (a instanceof VendorSpecificAttribute) {
            VendorSpecificAttribute vsa = (VendorSpecificAttribute)a;
            if (vsa.getChildVendorId() == vendorId) {
               result.add(vsa);
            }
         }
      }

      return result;
   }

   /** @deprecated */
   public VendorSpecificAttribute getVendorAttribute(int vendorId) {
      Iterator i = this.getAttributes(26).iterator();

      VendorSpecificAttribute vsa;
      do {
         if (!i.hasNext()) {
            return null;
         }

         vsa = (VendorSpecificAttribute)i.next();
      } while(vsa.getChildVendorId() != vendorId);

      return vsa;
   }

   public void encodeRequestPacket(OutputStream out, String sharedSecret) throws IOException {
      this.encodePacket(out, sharedSecret, (RadiusPacket)null);
   }

   public void encodeResponsePacket(OutputStream out, String sharedSecret, RadiusPacket request) throws IOException {
      if (request == null) {
         throw new NullPointerException("request cannot be null");
      } else {
         this.encodePacket(out, sharedSecret, request);
      }
   }

   public static RadiusPacket decodeRequestPacket(InputStream in, String sharedSecret) throws IOException, RadiusException {
      return decodePacket(DefaultDictionary.getDefaultDictionary(), in, sharedSecret, (RadiusPacket)null);
   }

   public static RadiusPacket decodeResponsePacket(InputStream in, String sharedSecret, RadiusPacket request) throws IOException, RadiusException {
      if (request == null) {
         throw new NullPointerException("request may not be null");
      } else {
         return decodePacket(DefaultDictionary.getDefaultDictionary(), in, sharedSecret, request);
      }
   }

   public static RadiusPacket decodeRequestPacket(Dictionary dictionary, InputStream in, String sharedSecret) throws IOException, RadiusException {
      return decodePacket(dictionary, in, sharedSecret, (RadiusPacket)null);
   }

   public static RadiusPacket decodeResponsePacket(Dictionary dictionary, InputStream in, String sharedSecret, RadiusPacket request) throws IOException, RadiusException {
      if (request == null) {
         throw new NullPointerException("request may not be null");
      } else {
         return decodePacket(dictionary, in, sharedSecret, request);
      }
   }

   public static synchronized int getNextPacketIdentifier() {
      ++nextPacketId;
      if (nextPacketId > 255) {
         nextPacketId = 0;
      }

      return nextPacketId;
   }

   public static RadiusPacket createRadiusPacket(int type) {
      Object rp;
      switch (type) {
         case 1:
            rp = new AccessRequest();
            break;
         case 2:
         case 3:
         case 5:
         default:
            rp = new RadiusPacket();
            break;
         case 4:
            rp = new AccountingRequest();
      }

      ((RadiusPacket)rp).setPacketType(type);
      return (RadiusPacket)rp;
   }

   public String toString() {
      StringBuffer s = new StringBuffer();
      s.append(this.getPacketTypeName());
      s.append(", ID ");
      s.append(this.packetIdentifier);
      Iterator i = this.attributes.iterator();

      while(i.hasNext()) {
         RadiusAttribute attr = (RadiusAttribute)i.next();
         s.append("\n");
         s.append(attr.toString());
      }

      return s.toString();
   }

   public byte[] getAuthenticator() {
      return this.authenticator;
   }

   public void setAuthenticator(byte[] authenticator) {
      this.authenticator = authenticator;
   }

   public Dictionary getDictionary() {
      return this.dictionary;
   }

   public void setDictionary(Dictionary dictionary) {
      this.dictionary = dictionary;
      Iterator i = this.attributes.iterator();

      while(i.hasNext()) {
         RadiusAttribute attr = (RadiusAttribute)i.next();
         attr.setDictionary(dictionary);
      }

   }

   protected void encodePacket(OutputStream out, String sharedSecret, RadiusPacket request) throws IOException {
      if (sharedSecret != null && sharedSecret.length() != 0) {
         if (request != null && request.getAuthenticator() == null) {
            throw new RuntimeException("request authenticator not set");
         } else {
            if (request == null) {
               this.authenticator = this.createRequestAuthenticator(sharedSecret);
               this.encodeRequestAttributes(sharedSecret);
            }

            byte[] attributes = this.getAttributeBytes();
            int packetLength = 20 + attributes.length;
            if (packetLength > 4096) {
               throw new RuntimeException("packet too long");
            } else {
               if (request != null) {
                  this.authenticator = this.createResponseAuthenticator(sharedSecret, packetLength, attributes, request.getAuthenticator());
               } else {
                  this.authenticator = this.updateRequestAuthenticator(sharedSecret, packetLength, attributes);
               }

               DataOutputStream dos = new DataOutputStream(out);
               dos.writeByte(this.getPacketType());
               dos.writeByte(this.getPacketIdentifier());
               dos.writeShort(packetLength);
               dos.write(this.getAuthenticator());
               dos.write(attributes);
               dos.flush();
            }
         }
      } else {
         throw new RuntimeException("no shared secret has been set");
      }
   }

   protected void encodeRequestAttributes(String sharedSecret) {
   }

   protected byte[] createRequestAuthenticator(String sharedSecret) {
      byte[] secretBytes = RadiusUtil.getUtf8Bytes(sharedSecret);
      byte[] randomBytes = new byte[16];
      random.nextBytes(randomBytes);
      MessageDigest md5 = this.getMd5Digest();
      md5.reset();
      md5.update(secretBytes);
      md5.update(randomBytes);
      return md5.digest();
   }

   protected byte[] updateRequestAuthenticator(String sharedSecret, int packetLength, byte[] attributes) {
      return this.authenticator;
   }

   protected byte[] createResponseAuthenticator(String sharedSecret, int packetLength, byte[] attributes, byte[] requestAuthenticator) {
      MessageDigest md5 = this.getMd5Digest();
      md5.reset();
      md5.update((byte)this.getPacketType());
      md5.update((byte)this.getPacketIdentifier());
      md5.update((byte)(packetLength >> 8));
      md5.update((byte)(packetLength & 255));
      md5.update(requestAuthenticator, 0, requestAuthenticator.length);
      md5.update(attributes, 0, attributes.length);
      md5.update(RadiusUtil.getUtf8Bytes(sharedSecret));
      return md5.digest();
   }

   protected static RadiusPacket decodePacket(Dictionary dictionary, InputStream in, String sharedSecret, RadiusPacket request) throws IOException, RadiusException {
      if (sharedSecret != null && sharedSecret.length() != 0) {
         if (request != null && request.getAuthenticator() == null) {
            throw new RuntimeException("request authenticator not set");
         } else {
            int type = in.read() & 255;
            int identifier = in.read() & 255;
            int length = (in.read() & 255) << 8 | in.read() & 255;
            if (request != null && request.getPacketIdentifier() != identifier) {
               throw new RadiusException("bad packet: invalid packet identifier (request: " + request.getPacketIdentifier() + ", response: " + identifier);
            } else if (length < 20) {
               throw new RadiusException("bad packet: packet too short (" + length + " bytes)");
            } else if (length > 4096) {
               throw new RadiusException("bad packet: packet too long (" + length + " bytes)");
            } else {
               byte[] authenticator = new byte[16];
               byte[] attributeData = new byte[length - 20];
               in.read(authenticator);
               in.read(attributeData);
               int pos = 0;

               for(int attributeCount = 0; pos < attributeData.length; ++attributeCount) {
                  if (pos + 1 >= attributeData.length) {
                     throw new RadiusException("bad packet: attribute length mismatch");
                  }

                  int attributeLength = attributeData[pos + 1] & 255;
                  if (attributeLength < 2) {
                     throw new RadiusException("bad packet: invalid attribute length");
                  }

                  pos += attributeLength;
               }

               if (pos != attributeData.length) {
                  throw new RadiusException("bad packet: attribute length mismatch");
               } else {
                  RadiusPacket rp = createRadiusPacket(type);
                  rp.setPacketType(type);
                  rp.setPacketIdentifier(identifier);
                  rp.authenticator = authenticator;

                  int attributeLength;
                  for(pos = 0; pos < attributeData.length; pos += attributeLength) {
                     int attributeType = attributeData[pos] & 255;
                     attributeLength = attributeData[pos + 1] & 255;
                     RadiusAttribute a = RadiusAttribute.createRadiusAttribute(dictionary, -1, attributeType);
                     a.readAttribute(attributeData, pos, attributeLength);
                     rp.addAttribute(a);
                  }

                  if (request == null) {
                     rp.decodeRequestAttributes(sharedSecret);
                     rp.checkRequestAuthenticator(sharedSecret, length, attributeData);
                  } else {
                     rp.checkResponseAuthenticator(sharedSecret, length, attributeData, request.getAuthenticator());
                  }

                  return rp;
               }
            }
         }
      } else {
         throw new RuntimeException("no shared secret has been set");
      }
   }

   protected void checkRequestAuthenticator(String sharedSecret, int packetLength, byte[] attributes) throws RadiusException {
   }

   protected void decodeRequestAttributes(String sharedSecret) throws RadiusException {
   }

   protected void checkResponseAuthenticator(String sharedSecret, int packetLength, byte[] attributes, byte[] requestAuthenticator) throws RadiusException {
      byte[] authenticator = this.createResponseAuthenticator(sharedSecret, packetLength, attributes, requestAuthenticator);
      byte[] receivedAuth = this.getAuthenticator();

      for(int i = 0; i < 16; ++i) {
         if (authenticator[i] != receivedAuth[i]) {
            throw new RadiusException("response authenticator invalid");
         }
      }

   }

   protected MessageDigest getMd5Digest() {
      if (this.md5Digest == null) {
         try {
            this.md5Digest = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException var2) {
            NoSuchAlgorithmException nsae = var2;
            throw new RuntimeException("md5 digest not available", nsae);
         }
      }

      return this.md5Digest;
   }

   protected byte[] getAttributeBytes() throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
      Iterator i = this.attributes.iterator();

      while(i.hasNext()) {
         RadiusAttribute a = (RadiusAttribute)i.next();
         bos.write(a.writeAttribute());
      }

      bos.flush();
      return bos.toByteArray();
   }
}
