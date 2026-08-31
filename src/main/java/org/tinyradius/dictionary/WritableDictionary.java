package org.tinyradius.dictionary;

public interface WritableDictionary extends Dictionary {
   void addVendor(int var1, String var2);

   void addAttributeType(AttributeType var1);
}
