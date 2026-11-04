package org.tinyradius.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.tinyradius.attribute.IntegerAttribute;
import org.tinyradius.attribute.IpAttribute;
import org.tinyradius.attribute.Ipv6Attribute;
import org.tinyradius.attribute.Ipv6PrefixAttribute;
import org.tinyradius.attribute.LongAttribute;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.attribute.StringAttribute;
import org.tinyradius.attribute.VendorSpecificAttribute;

public class DictionaryParser {
   public DictionaryParser() {
   }

   public static Dictionary parseDictionary(InputStream source) throws IOException {
      WritableDictionary d = new MemoryDictionary();
      parseDictionary(source, d);
      return d;
   }

   public static void parseDictionary(InputStream source, WritableDictionary dictionary) throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(source));
      int lineNum = -1;

      String line;
      while((line = in.readLine()) != null) {
         ++lineNum;
         line = line.trim();
         if (!line.startsWith("#") && line.length() != 0) {
            StringTokenizer tok = new StringTokenizer(line);
            if (tok.hasMoreTokens()) {
               String lineType = tok.nextToken().trim();
               if (lineType.equalsIgnoreCase("ATTRIBUTE")) {
                  parseAttributeLine(dictionary, tok, lineNum);
               } else if (lineType.equalsIgnoreCase("VALUE")) {
                  parseValueLine(dictionary, tok, lineNum);
               } else if (lineType.equalsIgnoreCase("$INCLUDE")) {
                  includeDictionaryFile(dictionary, tok, lineNum);
               } else if (lineType.equalsIgnoreCase("VENDORATTR")) {
                  parseVendorAttributeLine(dictionary, tok, lineNum);
               } else {
                  if (!lineType.equals("VENDOR")) {
                     throw new IOException("unknown line type: " + lineType + " line: " + lineNum);
                  }

                  parseVendorLine(dictionary, tok, lineNum);
               }
            }
         }
      }

   }

   private static void parseAttributeLine(WritableDictionary dictionary, StringTokenizer tok, int lineNum) throws IOException {
      if (tok.countTokens() != 3) {
         throw new IOException("syntax error on line " + lineNum);
      } else {
         String name = tok.nextToken().trim();
         int code = Integer.parseInt(tok.nextToken());
         String typeStr = tok.nextToken().trim();
         Class type;
         if (code == 26) {
            type = VendorSpecificAttribute.class;
         } else {
            type = getAttributeTypeClass(code, typeStr);
         }

         dictionary.addAttributeType(new AttributeType(code, name, type));
      }
   }

   private static void parseValueLine(WritableDictionary dictionary, StringTokenizer tok, int lineNum) throws IOException {
      if (tok.countTokens() != 3) {
         throw new IOException("syntax error on line " + lineNum);
      } else {
         String typeName = tok.nextToken().trim();
         String enumName = tok.nextToken().trim();
         String valStr = tok.nextToken().trim();
         AttributeType at = dictionary.getAttributeTypeByName(typeName);
         if (at == null) {
            throw new IOException("unknown attribute type: " + typeName + ", line: " + lineNum);
         } else {
            at.addEnumerationValue(Integer.parseInt(valStr), enumName);
         }
      }
   }

   private static void parseVendorAttributeLine(WritableDictionary dictionary, StringTokenizer tok, int lineNum) throws IOException {
      if (tok.countTokens() != 4) {
         throw new IOException("syntax error on line " + lineNum);
      } else {
         String vendor = tok.nextToken().trim();
         String name = tok.nextToken().trim();
         int code = Integer.parseInt(tok.nextToken().trim());
         String typeStr = tok.nextToken().trim();
         Class type = getAttributeTypeClass(code, typeStr);
         AttributeType at = new AttributeType(Integer.parseInt(vendor), code, name, type);
         dictionary.addAttributeType(at);
      }
   }

   private static void parseVendorLine(WritableDictionary dictionary, StringTokenizer tok, int lineNum) throws IOException {
      if (tok.countTokens() != 2) {
         throw new IOException("syntax error on line " + lineNum);
      } else {
         int vendorId = Integer.parseInt(tok.nextToken().trim());
         String vendorName = tok.nextToken().trim();
         dictionary.addVendor(vendorId, vendorName);
      }
   }

   private static void includeDictionaryFile(WritableDictionary dictionary, StringTokenizer tok, int lineNum) throws IOException {
      if (tok.countTokens() != 1) {
         throw new IOException("syntax error on line " + lineNum);
      } else {
         String includeFile = tok.nextToken();
         File incf = new File(includeFile);
         if (!incf.exists()) {
            throw new IOException("inclueded file '" + includeFile + "' not found, line " + lineNum);
         } else {
            FileInputStream fis = new FileInputStream(incf);
            parseDictionary(fis, dictionary);
         }
      }
   }

   private static Class getAttributeTypeClass(int attributeType, String typeStr) {
      Class type = RadiusAttribute.class;
      if (typeStr.equalsIgnoreCase("string")) {
         type = StringAttribute.class;
      } else if (typeStr.equalsIgnoreCase("octets")) {
         type = RadiusAttribute.class;
      } else if (!typeStr.equalsIgnoreCase("integer") && !typeStr.equalsIgnoreCase("date")) {
         if (typeStr.equalsIgnoreCase("long")) {
            type = LongAttribute.class;
         } else if (typeStr.equalsIgnoreCase("ipaddr")) {
            type = IpAttribute.class;
         } else if (typeStr.equalsIgnoreCase("ipv6addr")) {
            type = Ipv6Attribute.class;
         } else if (typeStr.equalsIgnoreCase("ipv6prefix")) {
            type = Ipv6PrefixAttribute.class;
         }
      } else {
         type = IntegerAttribute.class;
      }

      return type;
   }
}
