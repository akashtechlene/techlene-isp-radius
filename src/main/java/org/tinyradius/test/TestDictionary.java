//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.test;

import java.io.FileInputStream;
import java.io.InputStream;
import org.tinyradius.attribute.IpAttribute;
import org.tinyradius.dictionary.Dictionary;
import org.tinyradius.dictionary.DictionaryParser;
import org.tinyradius.packet.AccessRequest;

public class TestDictionary {
   public TestDictionary() {
   }

   public static void main(String[] args) throws Exception {
      InputStream source = new FileInputStream("test.dictionary");
      Dictionary dictionary = DictionaryParser.parseDictionary(source);
      AccessRequest ar = new AccessRequest("UserName", "UserPassword");
      ar.setDictionary(dictionary);
      ar.addAttribute("WISPr-Location-ID", "LocationID");
      ar.addAttribute(new IpAttribute(8, 1234567L));
      System.out.println(ar);
   }
}
