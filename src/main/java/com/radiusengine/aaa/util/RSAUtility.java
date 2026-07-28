//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.aaa.util;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class RSAUtility {
    public static final String KEY = "NeoFruition@2021";

    public RSAUtility() {
    }

    public static String encrypt(String key, String plainText) {
        try {
            AesCryptUtil aesUtil = new AesCryptUtil(key);
            String encRequest = aesUtil.encrypt(plainText);
            return encRequest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypt(String key, TreeMap<String, String> keyValueMap) {
        try {
            if (keyValueMap != null && keyValueMap.size() > 0) {
                StringBuffer sbPlainText = new StringBuffer("");

                String keyParam;
                for(Iterator var3 = keyValueMap.keySet().iterator(); var3.hasNext(); sbPlainText = sbPlainText.append(keyParam).append("=").append((String)keyValueMap.get(keyParam)).append("&")) {
                    keyParam = (String)var3.next();
                }

                if (sbPlainText.toString().length() > 0) {
                    String plainText = sbPlainText.toString().substring(0, sbPlainText.toString().length() - 1);
                    AesCryptUtil aesUtil = new AesCryptUtil(key);
                    String encRequest = aesUtil.encrypt(plainText);
                    return encRequest;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static TreeMap<String, String> decrypt(String key, String encResp) {
        TreeMap<String, String> keyValueMap = null;

        try {
            AesCryptUtil aesUtil = new AesCryptUtil(key);
            String decResp = aesUtil.decrypt(encResp);
            StringTokenizer tokenizer = new StringTokenizer(decResp, "&");
            String pair = null;
            String pname = null;
            String pvalue = null;
            keyValueMap = new TreeMap<>();

            while(tokenizer.hasMoreTokens()) {
                pair = tokenizer.nextToken();
                if (pair != null) {
                    StringTokenizer strTok = new StringTokenizer(pair, "=");
                    pname = "";
                    pvalue = "";
                    if (strTok.hasMoreTokens()) {
                        pname = strTok.nextToken();
                        if (strTok.hasMoreTokens()) {
                            pvalue = strTok.nextToken();
                        }

                        keyValueMap.put(pname, pvalue);
                    }
                }
            }
        } catch (Exception e) {
            keyValueMap = null;
            e.printStackTrace();
        }

        return keyValueMap;
    }
    public static void main(String args[]){
        TreeMap<String, String> classParamMap = RSAUtility.decrypt("NeoFruition@2021", "d11861c5652f0f07d43caaa1197e50ad1a991553cb5bff1fd75b1789fd03c9f4");
        System.out.println("classParamMap :"+classParamMap);
    }
}
