//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.attribute;

import java.util.StringTokenizer;
import org.tinyradius.util.RadiusException;

public class IpAttribute extends RadiusAttribute {
   public IpAttribute() {
   }

   public IpAttribute(int type, String value) {
      this.setAttributeType(type);
      this.setAttributeValue(value);
   }

   public IpAttribute(int type, long ipNum) {
      this.setAttributeType(type);
      this.setIpAsLong(ipNum);
   }

   public String getAttributeValue() {
      StringBuffer ip = new StringBuffer();
      byte[] data = this.getAttributeData();
      if (data != null && data.length == 4) {
         ip.append(data[0] & 255);
         ip.append(".");
         ip.append(data[1] & 255);
         ip.append(".");
         ip.append(data[2] & 255);
         ip.append(".");
         ip.append(data[3] & 255);
         return ip.toString();
      } else {
         throw new RuntimeException("ip attribute: expected 4 bytes attribute data");
      }
   }

   public void setAttributeValue(String value) {
      if (value != null && value.length() >= 7 && value.length() <= 15) {
         StringTokenizer tok = new StringTokenizer(value, ".");
         if (tok.countTokens() != 4) {
            throw new IllegalArgumentException("bad IP number: 4 numbers required");
         } else {
            byte[] data = new byte[4];

            for(int i = 0; i < 4; ++i) {
               int num = Integer.parseInt(tok.nextToken());
               if (num < 0 || num > 255) {
                  throw new IllegalArgumentException("bad IP number: num out of bounds");
               }

               data[i] = (byte)num;
            }

            this.setAttributeData(data);
         }
      } else {
         throw new IllegalArgumentException("bad IP number");
      }
   }

   public long getIpAsLong() {
      byte[] data = this.getAttributeData();
      if (data != null && data.length == 4) {
         return (long)(data[0] & 255) << 24 | (long)((data[1] & 255) << 16) | (long)((data[2] & 255) << 8) | (long)(data[3] & 255);
      } else {
         throw new RuntimeException("expected 4 bytes attribute data");
      }
   }

   public void setIpAsLong(long ip) {
      byte[] data = new byte[]{(byte)((int)(ip >> 24 & 255L)), (byte)((int)(ip >> 16 & 255L)), (byte)((int)(ip >> 8 & 255L)), (byte)((int)(ip & 255L))};
      this.setAttributeData(data);
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      if (length != 6) {
         throw new RadiusException("IP attribute: expected 4 bytes data");
      } else {
         super.readAttribute(data, offset, length);
      }
   }
}
