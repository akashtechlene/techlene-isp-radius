//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.attribute;

import java.io.UnsupportedEncodingException;

public class StringAttribute extends RadiusAttribute {
   public StringAttribute() {
   }

   public StringAttribute(int type, String value) {
      this.setAttributeType(type);
      this.setAttributeValue(value);
   }

   public String getAttributeValue() {
      try {
         return new String(this.getAttributeData(), "UTF-8");
      } catch (UnsupportedEncodingException var2) {
         return new String(this.getAttributeData());
      }
   }

   public void setAttributeValue(String value) {
      if (value == null) {
         throw new NullPointerException("string value not set");
      } else {
         try {
            this.setAttributeData(value.getBytes("UTF-8"));
         } catch (UnsupportedEncodingException var3) {
            this.setAttributeData(value.getBytes());
         }

      }
   }
}
