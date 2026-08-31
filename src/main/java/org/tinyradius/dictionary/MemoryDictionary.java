package org.tinyradius.dictionary;

import java.util.HashMap;
import java.util.Map;

public class MemoryDictionary implements WritableDictionary {
   private Map<Integer, String> vendorsByCode = new HashMap();
   private Map attributesByCode = new HashMap();
   private Map attributesByName = new HashMap();

   public AttributeType getAttributeTypeByCode(int typeCode) {
      return this.getAttributeTypeByCode(-1, typeCode);
   }

   public AttributeType getAttributeTypeByCode(int vendorCode, int typeCode) {
      Map vendorAttributes = (Map)this.attributesByCode.get(new Integer(vendorCode));
      return vendorAttributes == null ? null : (AttributeType)vendorAttributes.get(new Integer(typeCode));
   }

   public AttributeType getAttributeTypeByName(String typeName) {
      return (AttributeType)this.attributesByName.get(typeName);
   }

   public int getVendorId(String vendorName) {
      for(Map.Entry e : this.vendorsByCode.entrySet()) {
         if (e.getValue().equals(vendorName)) {
            return (Integer)e.getKey();
         }
      }

      return -1;
   }

   public String getVendorName(int vendorId) {
      return (String)this.vendorsByCode.get(new Integer(vendorId));
   }

   public void addVendor(int vendorId, String vendorName) {
      if (vendorId < 0) {
         throw new IllegalArgumentException("vendor ID must be positive");
      } else if (this.getVendorName(vendorId) != null) {
         throw new IllegalArgumentException("duplicate vendor code");
      } else if (vendorName != null && vendorName.length() != 0) {
         this.vendorsByCode.put(new Integer(vendorId), vendorName);
      } else {
         throw new IllegalArgumentException("vendor name empty");
      }
   }

   public void addAttributeType(AttributeType attributeType) {
      if (attributeType == null) {
         throw new IllegalArgumentException("attribute type must not be null");
      } else {
         Integer vendorId = new Integer(attributeType.getVendorId());
         Integer typeCode = new Integer(attributeType.getTypeCode());
         String attributeName = attributeType.getName();
         if (this.attributesByName.containsKey(attributeName)) {
            throw new IllegalArgumentException("duplicate attribute name: " + attributeName);
         } else {
            Map vendorAttributes = (Map)this.attributesByCode.get(vendorId);
            if (vendorAttributes == null) {
               vendorAttributes = new HashMap();
               this.attributesByCode.put(vendorId, vendorAttributes);
            }

            if (vendorAttributes.containsKey(typeCode)) {
               throw new IllegalArgumentException("duplicate type code: " + typeCode);
            } else {
               this.attributesByName.put(attributeName, attributeType);
               vendorAttributes.put(typeCode, attributeType);
            }
         }
      }
   }
}
