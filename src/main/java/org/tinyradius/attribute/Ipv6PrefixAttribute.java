package org.tinyradius.attribute;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.tinyradius.util.RadiusException;

public class Ipv6PrefixAttribute extends RadiusAttribute {
   public Ipv6PrefixAttribute() {
   }

   public Ipv6PrefixAttribute(int type, String value) {
      this.setAttributeType(type);
      this.setAttributeValue(value);
   }

   public String getAttributeValue() {
      byte[] data = this.getAttributeData();

      try {
         int prefix = data[1] & 255;
         Inet6Address addr = (Inet6Address)Inet6Address.getByAddress((String)null, Arrays.copyOfRange(data, 2, data.length));
         return addr.getHostAddress() + "/" + prefix;
      } catch (UnknownHostException e) {
         throw new IllegalArgumentException("bad IPv6 prefix", e);
      }
   }

   public void setAttributeValue(String value) {
      if (value != null && value.length() >= 3) {
         try {
            byte[] data = new byte[18];
            data[0] = 0;
            int slashPos = value.indexOf("/");
            data[1] = (byte)(Integer.valueOf(value.substring(slashPos + 1)) & 255);
            Inet6Address addr = (Inet6Address)Inet6Address.getByName(value.substring(0, slashPos));
            byte[] ipData = addr.getAddress();

            for(int i = 0; i < ipData.length; ++i) {
               data[i + 2] = ipData[i];
            }

            this.setAttributeData(data);
            System.out.print("IPV6Prefix set value Called - Data Value:" + data);
         } catch (UnknownHostException e) {
            throw new IllegalArgumentException("bad IPv6 address : " + value, e);
         }
      } else {
         throw new IllegalArgumentException("bad IPv6 address : " + value);
      }
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      super.readAttribute(data, offset, length);
   }
}
