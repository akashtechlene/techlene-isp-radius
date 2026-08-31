package org.tinyradius.dictionary;

import java.util.HashMap;
import java.util.Map;
import org.tinyradius.attribute.RadiusAttribute;

public class AttributeType {
   private int vendorId = -1;
   private int typeCode;
   private String name;
   private Class attributeClass;
   private Map<Integer, String> enumeration = null;

   public AttributeType(int code, String name, Class type) {
      this.setTypeCode(code);
      this.setName(name);
      this.setAttributeClass(type);
   }

   public AttributeType(int vendor, int code, String name, Class type) {
      this.setTypeCode(code);
      this.setName(name);
      this.setAttributeClass(type);
      this.setVendorId(vendor);
   }

   public int getTypeCode() {
      return this.typeCode;
   }

   public void setTypeCode(int code) {
      if (code >= 1 && code <= 2566) {
         this.typeCode = code;
      } else {
         throw new IllegalArgumentException("code out of bounds");
      }
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      if (name != null && name.length() != 0) {
         this.name = name;
      } else {
         throw new IllegalArgumentException("name is empty");
      }
   }

   public Class getAttributeClass() {
      return this.attributeClass;
   }

   public void setAttributeClass(Class type) {
      if (type == null) {
         throw new NullPointerException("type is null");
      } else if (!RadiusAttribute.class.isAssignableFrom(type)) {
         throw new IllegalArgumentException("type is not a RadiusAttribute descendant");
      } else {
         this.attributeClass = type;
      }
   }

   public int getVendorId() {
      return this.vendorId;
   }

   public void setVendorId(int vendorId) {
      this.vendorId = vendorId;
   }

   public String getEnumeration(int value) {
      return this.enumeration != null ? (String)this.enumeration.get(new Integer(value)) : null;
   }

   public String getEnumeration(long value) {
      return this.enumeration != null ? (String)this.enumeration.get(new Long(value)) : null;
   }

   public Integer getEnumeration(String value) {
      if (value != null && value.length() != 0) {
         if (this.enumeration == null) {
            return null;
         } else {
            for(Map.Entry e : this.enumeration.entrySet()) {
               if (e.getValue().equals(value)) {
                  return (Integer)e.getKey();
               }
            }

            return null;
         }
      } else {
         throw new IllegalArgumentException("value is empty");
      }
   }

   public void addEnumerationValue(int num, String name) {
      if (name != null && name.length() != 0) {
         if (this.enumeration == null) {
            this.enumeration = new HashMap();
         }

         this.enumeration.put(new Integer(num), name);
      } else {
         throw new IllegalArgumentException("name is empty");
      }
   }

   public String toString() {
      String s = this.getTypeCode() + "/" + this.getName() + ": " + this.attributeClass.getName();
      if (this.getVendorId() != -1) {
         s = s + " (vendor " + this.getVendorId() + ")";
      }

      return s;
   }
}
