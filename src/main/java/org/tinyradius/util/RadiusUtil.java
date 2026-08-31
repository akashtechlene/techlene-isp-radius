package org.tinyradius.util;

import java.io.UnsupportedEncodingException;

public class RadiusUtil {
   public static byte[] getUtf8Bytes(String str) {
      try {
         return str.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         return str.getBytes();
      }
   }

   public static String getStringFromUtf8(byte[] utf8) {
      try {
         return new String(utf8, "UTF-8");
      } catch (UnsupportedEncodingException var2) {
         return new String(utf8);
      }
   }

   public static String getHexString(byte[] data) {
      StringBuffer hex = new StringBuffer("0x");
      if (data != null) {
         for(int i = 0; i < data.length; ++i) {
            String digit = Integer.toString(data[i] & 255, 16);
            if (digit.length() < 2) {
               hex.append('0');
            }

            hex.append(digit);
         }
      }

      return hex.toString();
   }
}
