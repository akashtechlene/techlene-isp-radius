package org.tinyradius.attribute;

import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.dictionary.DefaultDictionary;
import org.tinyradius.dictionary.Dictionary;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusUtil;

public class RadiusAttribute {
   private Dictionary dictionary = DefaultDictionary.getDefaultDictionary();
   private int attributeType = -1;
   private int vendorId = -1;
   private byte[] attributeData = null;

   public RadiusAttribute() {
   }

   public RadiusAttribute(int type, byte[] data) {
      this.setAttributeType(type);
      this.setAttributeData(data);
   }

   public byte[] getAttributeData() {
      return this.attributeData;
   }

   public void setAttributeData(byte[] attributeData) {
      if (attributeData == null) {
         throw new NullPointerException("attribute data is null");
      } else {
         this.attributeData = attributeData;
      }
   }

   public int getAttributeType() {
      return this.attributeType;
   }

   public void setAttributeType(int attributeType) {
      if (attributeType >= 0 && attributeType <= 2566) {
         this.attributeType = attributeType;
      } else {
         throw new IllegalArgumentException("attribute type invalid: " + attributeType);
      }
   }

   public void setAttributeValue(String value) {
      throw new RuntimeException("cannot set the value of attribute " + this.attributeType + " as a string");
   }

   public String getAttributeValue() {
      return RadiusUtil.getHexString(this.getAttributeData());
   }

   public int getVendorId() {
      return this.vendorId;
   }

   public void setVendorId(int vendorId) {
      this.vendorId = vendorId;
   }

   public Dictionary getDictionary() {
      return this.dictionary;
   }

   public void setDictionary(Dictionary dictionary) {
      this.dictionary = dictionary;
   }

   public byte[] writeAttribute() {
      byte[] attr = null;

      try {
         if (this.getAttributeType() == -1) {
            throw new IllegalArgumentException("attribute type not set");
         }

         if (this.attributeData == null) {
            throw new NullPointerException("attribute data not set");
         }

         if (this.getAttributeType() < 255) {
            attr = new byte[2 + this.attributeData.length];
            attr[0] = (byte)this.getAttributeType();
            attr[1] = (byte)(2 + this.attributeData.length);
            System.arraycopy(this.attributeData, 0, attr, 2, this.attributeData.length);
         } else {
            if (this.getAttributeType() == 1801) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 9;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType());
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1803) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 11;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1804) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 12;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1805) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 13;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1806) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 14;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1807) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 15;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1808) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 16;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1809) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 17;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1810) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 18;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1811) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 19;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1812) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 20;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1813) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 21;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1814) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 22;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1815) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 23;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1816) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 24;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1820) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 28;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1821) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 29;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1822) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 30;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1823) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 31;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1824) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 32;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1825) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 33;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1826) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 34;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1827) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 35;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1828) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 36;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1829) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 37;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1830) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 38;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1831) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 39;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }

            if (this.getAttributeType() == 1832) {
               attr = new byte[3 + this.attributeData.length];
               attr[0] = 7;
               attr[1] = 40;
               System.out.println("Setting Attribute byte:" + (byte)this.getAttributeType() + ":Plain:" + this.getAttributeType() + ":" + attr[1]);
               attr[2] = (byte)(3 + this.attributeData.length);
               System.arraycopy(this.attributeData, 0, attr, 3, this.attributeData.length);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return attr;
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      if (length < 2) {
         throw new RadiusException("attribute length too small: " + length);
      } else {
         int attrType = data[offset] & 255;
         int attrLen = data[offset + 1] & 255;
         byte[] attrData = new byte[attrLen - 2];
         System.arraycopy(data, offset + 2, attrData, 0, attrLen - 2);
         this.setAttributeType(attrType);
         this.setAttributeData(attrData);
      }
   }

   public void readAttribute(boolean bMultiByteSubType, byte[] data, int offset, int length) throws RadiusException {
      if (length < 2) {
         throw new RadiusException("attribute length too small: " + length);
      } else {
         int attrType = -1;
         int attrLen = -1;
         if (bMultiByteSubType) {
            int val1 = data[offset] & 255;
            int val2 = data[offset + 1] & 255;
            attrType = 256 * val1 + val2;
            attrLen = data[offset + 2] & 255;
         } else {
            attrType = data[offset] & 255;
            attrLen = data[offset + 1] & 255;
         }

         byte[] attrData = new byte[attrLen - 3];
         System.arraycopy(data, offset + 3, attrData, 0, attrLen - 3);
         this.setAttributeType(attrType);
         this.setAttributeData(attrData);
      }
   }

   public String toString() {
      AttributeType at = this.getAttributeTypeObject();
      String name;
      if (at != null) {
         name = at.getName();
      } else if (this.getVendorId() != -1) {
         name = "Unknown-Sub-Attribute-" + this.getAttributeType();
      } else {
         name = "Unknown-Attribute-" + this.getAttributeType();
      }

      if (this.getVendorId() != -1) {
         name = "  " + name;
      }

      return name + ": " + this.getAttributeValue();
   }

   public AttributeType getAttributeTypeObject() {
      return this.getVendorId() != -1 ? this.dictionary.getAttributeTypeByCode(this.getVendorId(), this.getAttributeType()) : this.dictionary.getAttributeTypeByCode(this.getAttributeType());
   }

   public static RadiusAttribute createRadiusAttribute(Dictionary dictionary, int vendorId, int attributeType) {
      RadiusAttribute attribute = new RadiusAttribute();
      AttributeType at = dictionary.getAttributeTypeByCode(vendorId, attributeType);
      if (at != null && at.getAttributeClass() != null) {
         try {
            attribute = (RadiusAttribute)at.getAttributeClass().newInstance();
         } catch (Exception var6) {
         }
      }

      attribute.setAttributeType(attributeType);
      attribute.setDictionary(dictionary);
      attribute.setVendorId(vendorId);
      return attribute;
   }

   public static RadiusAttribute createRadiusAttribute(int vendorId, int attributeType) {
      Dictionary dictionary = DefaultDictionary.getDefaultDictionary();
      return createRadiusAttribute(dictionary, vendorId, attributeType);
   }

   public static RadiusAttribute createRadiusAttribute(int attributeType) {
      Dictionary dictionary = DefaultDictionary.getDefaultDictionary();
      return createRadiusAttribute(dictionary, -1, attributeType);
   }
}
