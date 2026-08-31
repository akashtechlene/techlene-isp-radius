package org.tinyradius.attribute;

import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.util.RadiusException;

public class LongAttribute extends RadiusAttribute {
   public LongAttribute() {
   }

   public LongAttribute(int type, int value) {
      this.setAttributeType(type);
      this.setAttributeValue((long)value);
   }

   public LongAttribute(int type, long value) {
      this.setAttributeType(type);
      this.setAttributeValue(value);
   }

   public long getAttributeValueLong() {
      byte[] data = this.getAttributeData();
      long result = 0L;
      result <<= 8;
      result |= (long)(data[0] & 255);
      result <<= 8;
      result |= (long)(data[1] & 255);
      result <<= 8;
      result |= (long)(data[2] & 255);
      result <<= 8;
      result |= (long)(data[3] & 255);
      return result;
   }

   public String getAttributeValue() {
      long value = this.getAttributeValueLong();
      AttributeType at = this.getAttributeTypeObject();
      if (at != null) {
         String name = at.getEnumeration(value);
         if (name != null) {
            return name;
         }
      }

      return Long.toString(value);
   }

   public void setAttributeValue(long value) {
      byte[] data = new byte[4];
      data[0] = (byte)((int)(value >> 24 & 255L));
      data[1] = (byte)((int)(value >> 16 & 255L));
      data[2] = (byte)((int)(value >> 8 & 255L));
      data[3] = (byte)((int)(value & 255L));
      this.setAttributeData(data);
   }

   public void setAttributeValue(String value) {
      AttributeType at = this.getAttributeTypeObject();
      if (at != null) {
         Integer val = at.getEnumeration(value);
         if (val != null) {
            this.setAttributeValue((long)val);
            return;
         }
      }

      this.setAttributeValue(Long.parseLong(value));
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      if (length != 6) {
         throw new RadiusException("long attribute: expected 4 bytes data");
      } else {
         super.readAttribute(data, offset, length);
      }
   }
}
