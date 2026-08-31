package org.tinyradius.attribute;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.dictionary.Dictionary;
import org.tinyradius.util.RadiusException;

public class VendorSpecificAttribute extends RadiusAttribute {
   public static final int VENDOR_SPECIFIC = 26;
   private List<RadiusAttribute> subAttributes = new ArrayList();
   private int childVendorId;

   public VendorSpecificAttribute() {
   }

   public VendorSpecificAttribute(int vendorId) {
      this.setAttributeType(26);
      this.setChildVendorId(vendorId);
   }

   public void setChildVendorId(int childVendorId) {
      this.childVendorId = childVendorId;
   }

   public int getChildVendorId() {
      return this.childVendorId;
   }

   public void setDictionary(Dictionary dictionary) {
      super.setDictionary(dictionary);

      for(RadiusAttribute attr : this.subAttributes) {
         attr.setDictionary(dictionary);
      }

   }

   public void addSubAttribute(RadiusAttribute attribute) {
      if (attribute.getVendorId() != this.getChildVendorId()) {
         throw new IllegalArgumentException("sub attribut has incorrect vendor ID");
      } else {
         this.subAttributes.add(attribute);
      }
   }

   public void addSubAttribute(String name, String value) {
      if (name != null && name.length() != 0) {
         if (value != null && value.length() != 0) {
            AttributeType type = this.getDictionary().getAttributeTypeByName(name);
            if (type == null) {
               throw new IllegalArgumentException("unknown attribute type '" + name + "'");
            } else if (type.getVendorId() == -1) {
               throw new IllegalArgumentException("attribute type '" + name + "' is not a Vendor-Specific sub-attribute");
            } else if (type.getVendorId() != this.getChildVendorId()) {
               throw new IllegalArgumentException("attribute type '" + name + "' does not belong to vendor ID " + this.getChildVendorId());
            } else {
               RadiusAttribute attribute = createRadiusAttribute(this.getDictionary(), this.getChildVendorId(), type.getTypeCode());
               attribute.setAttributeValue(value);
               this.addSubAttribute(attribute);
            }
         } else {
            throw new IllegalArgumentException("value is empty");
         }
      } else {
         throw new IllegalArgumentException("type name is empty");
      }
   }

   public void removeSubAttribute(RadiusAttribute attribute) {
      if (!this.subAttributes.remove(attribute)) {
         throw new IllegalArgumentException("no such attribute");
      }
   }

   public List<RadiusAttribute> getSubAttributes() {
      return this.subAttributes;
   }

   public List getSubAttributes(int attributeType) {
      if (attributeType >= 1 && attributeType <= 255) {
         LinkedList result = new LinkedList();

         for(RadiusAttribute a : this.subAttributes) {
            if (attributeType == a.getAttributeType()) {
               result.add(a);
            }
         }

         return result;
      } else {
         throw new IllegalArgumentException("sub-attribute type out of bounds");
      }
   }

   public RadiusAttribute getSubAttribute(int type) {
      List attrs = this.getSubAttributes(type);
      if (attrs.size() > 1) {
         throw new RuntimeException("multiple sub-attributes of requested type " + type);
      } else {
         return attrs.size() == 0 ? null : (RadiusAttribute)attrs.get(0);
      }
   }

   public RadiusAttribute getSubAttribute(String type) throws RadiusException {
      if (type != null && type.length() != 0) {
         AttributeType t = this.getDictionary().getAttributeTypeByName(type);
         if (t == null) {
            throw new IllegalArgumentException("unknown attribute type name '" + type + "'");
         } else if (t.getVendorId() != this.getChildVendorId()) {
            throw new IllegalArgumentException("vendor ID mismatch");
         } else {
            return this.getSubAttribute(t.getTypeCode());
         }
      } else {
         throw new IllegalArgumentException("type name is empty");
      }
   }

   public String getSubAttributeValue(String type) throws RadiusException {
      RadiusAttribute attr = this.getSubAttribute(type);
      return attr == null ? null : attr.getAttributeValue();
   }

   public byte[] writeAttribute() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(255);
      bos.write(this.getChildVendorId() >> 24 & 255);
      bos.write(this.getChildVendorId() >> 16 & 255);
      bos.write(this.getChildVendorId() >> 8 & 255);
      bos.write(this.getChildVendorId() & 255);

      try {
         for(RadiusAttribute a : this.subAttributes) {
            bos.write(a.writeAttribute());
         }
      } catch (IOException ioe) {
         throw new RuntimeException("error writing data", ioe);
      }

      byte[] attrData = bos.toByteArray();
      int len = attrData.length;
      if (len > 253) {
         throw new RuntimeException("Vendor-Specific attribute too long: " + bos.size());
      } else {
         byte[] attr = new byte[len + 2];
         attr[0] = 26;
         attr[1] = (byte)(len + 2);
         System.arraycopy(attrData, 0, attr, 2, len);
         return attr;
      }
   }

   public void readAttribute(byte[] data, int offset, int length) throws RadiusException {
      if (length < 6) {
         throw new RadiusException("Vendor-Specific attribute too short: " + length);
      } else {
         int vsaCode = data[offset];
         int vsaLen = (data[offset + 1] & 255) - 6;
         if (vsaCode != 26) {
            throw new RadiusException("not a Vendor-Specific attribute");
         } else {
            int vendorId = unsignedByteToInt(data[offset + 2]) << 24 | unsignedByteToInt(data[offset + 3]) << 16 | unsignedByteToInt(data[offset + 4]) << 8 | unsignedByteToInt(data[offset + 5]);
            this.setChildVendorId(vendorId);
            int pos = 0;
            int count = 0;
            boolean bMultiByteSubType = false;
            if (vendorId == 637) {
               bMultiByteSubType = true;
            }

            while(pos < vsaLen) {
               if (pos + 1 >= vsaLen) {
                  throw new RadiusException("Vendor-Specific attribute malformed");
               }

               int vsaSubLen = -1;
               if (bMultiByteSubType) {
                  vsaSubLen = data[offset + 6 + pos + 2] & 255;
               } else {
                  vsaSubLen = data[offset + 6 + pos + 1] & 255;
               }

               pos += vsaSubLen;
               ++count;
            }

            if (pos != vsaLen) {
               throw new RadiusException("Vendor-Specific attribute malformed");
            } else {
               this.subAttributes = new ArrayList(count);

               int sublength;
               for(int var14 = 0; var14 < vsaLen; var14 += sublength) {
                  int subtype = -1;
                  sublength = -1;
                  if (bMultiByteSubType) {
                     int val1 = data[offset + 6 + var14] & 255;
                     int val2 = data[offset + 6 + var14 + 1] & 255;
                     subtype = 256 * val1 + val2;
                     sublength = data[offset + 6 + var14 + 2] & 255;
                  } else {
                     subtype = data[offset + 6 + var14] & 255;
                     sublength = data[offset + 6 + var14 + 1] & 255;
                  }

                  RadiusAttribute a = createRadiusAttribute(this.getDictionary(), vendorId, subtype);
                  if (bMultiByteSubType) {
                     a.readAttribute(true, data, offset + 6 + var14, sublength);
                  } else {
                     a.readAttribute(data, offset + 6 + var14, sublength);
                  }

                  this.subAttributes.add(a);
               }

            }
         }
      }
   }

   private static int unsignedByteToInt(byte b) {
      return b & 255;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("Vendor-Specific: ");
      int vendorId = this.getChildVendorId();
      String vendorName = this.getDictionary().getVendorName(vendorId);
      if (vendorName != null) {
         sb.append(vendorName);
         sb.append(" (");
         sb.append(vendorId);
         sb.append(")");
      } else {
         sb.append("vendor ID ");
         sb.append(vendorId);
      }

      for(RadiusAttribute attr : this.getSubAttributes()) {
         sb.append("\n");
         sb.append(attr.toString());
      }

      return sb.toString();
   }
}
