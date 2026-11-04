
package org.tinyradius.dictionary;

public interface Dictionary {
   AttributeType getAttributeTypeByName(String var1);

   AttributeType getAttributeTypeByCode(int var1);

   AttributeType getAttributeTypeByCode(int var1, int var2);

   String getVendorName(int var1);

   int getVendorId(String var1);
}
