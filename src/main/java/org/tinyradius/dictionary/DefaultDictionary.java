//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.dictionary;

import java.io.IOException;
import java.io.InputStream;

public class DefaultDictionary extends MemoryDictionary {
   private static final String DICTIONARY_RESOURCE = "default_dictionary";
   private static DefaultDictionary instance = null;

   public static Dictionary getDefaultDictionary() {
      return instance;
   }

   private DefaultDictionary() {
   }

   static {
      try {
         instance = new DefaultDictionary();
         InputStream source = DefaultDictionary.class.getClassLoader().getResourceAsStream("default_dictionary");
         DictionaryParser.parseDictionary(source, instance);
      } catch (IOException e) {
         throw new RuntimeException("default dictionary unavailable", e);
      }
   }
}
