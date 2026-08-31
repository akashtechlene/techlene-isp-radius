package org.tinyradius.attribute;

import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.util.RadiusException;

public class IntegerAttribute extends RadiusAttribute {
   public IntegerAttribute() {
   }

   public IntegerAttribute(int type, int value) {
      this.setAttributeType(type);
      this.setAttributeValue(value);
   }

   public int getAttributeValueInt() {
      byte[] data = this.getAttributeData();
      return (data[0] & 255) << 24 | (data[1] & 255) << 16 | (data[2] & 255) << 8 | data[3] & 255;
   }

   public String getAttributeValue() {
      int value = this.getAttributeValueInt();
      AttributeType at = this.getAttributeTypeObject();
      if (at != null) {
         String name = at.getEnumeration(value);
         if (name != null) {
            return name;
         }
      }

      return Integer.toString(value);
   }

   public void setAttributeValue(int value) {
      byte[] data = new byte[4];
      data[0] = (byte)(value >> 24 & 255);
      data[1] = (byte)(value >> 16 & 255);
      data[2] = (byte)(value >> 8 & 255);
      data[3] = (byte)(value & 255);
      this.setAttributeData(data);
   }

   public void setAttributeValue(String value) {
      AttributeType at = this.getAttributeTypeObject();
      if (at != null) {
         Integer val = at.getEnumeration(value);
         if (val != null) {
            this.setAttributeValue(val);
            return;
         }
      }

      this.setAttributeValue(Integer.parseInt(value));
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      if (length != 6) {
         throw new RadiusException("integer attribute: expected 4 bytes data");
      } else {
         super.readAttribute(data, offset, length);
      }
   }
}
