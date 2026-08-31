package org.tinyradius.attribute;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import org.tinyradius.util.RadiusException;

public class Ipv6Attribute extends RadiusAttribute {
   public Ipv6Attribute() {
   }

   public Ipv6Attribute(int type, String value) {
      this.setAttributeType(type);
      this.setAttributeValue(value);
   }

   public String getAttributeValue() {
      byte[] data = this.getAttributeData();
      if (data != null && data.length == 16) {
         try {
            Inet6Address addr = (Inet6Address)Inet6Address.getByAddress((String)null, data);
            return addr.getHostAddress();
         } catch (UnknownHostException e) {
            throw new IllegalArgumentException("bad IPv6 address", e);
         }
      } else {
         throw new RuntimeException("ip attribute: expected 16 bytes attribute data");
      }
   }

   public void setAttributeValue(String value) {
      if (value != null && value.length() >= 3) {
         try {
            Inet6Address addr = (Inet6Address)Inet6Address.getByName(value);
            byte[] data = addr.getAddress();
            this.setAttributeData(data);
         } catch (UnknownHostException e) {
            throw new IllegalArgumentException("bad IPv6 address : " + value, e);
         }
      } else {
         throw new IllegalArgumentException("bad IPv6 address : " + value);
      }
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      if (length != 18) {
         throw new RadiusException("IP attribute: expected 16 bytes data");
      } else {
         super.readAttribute(data, offset, length);
      }
   }
}
