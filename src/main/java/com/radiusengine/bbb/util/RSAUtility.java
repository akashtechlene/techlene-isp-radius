////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package com.radiusengine.bbb.util;
//
//import java.util.Iterator;
//import java.util.StringTokenizer;
//import java.util.TreeMap;
//
//public class RSAUtility {
//    public static final String KEY = "NeoFruition@2021";
//
//    public RSAUtility() {
//    }
//
//    public static String encrypt(String key, String plainText) {
//        try {
//            AesCryptUtil aesUtil = new AesCryptUtil(key);
//            String encRequest = aesUtil.encrypt(plainText);
//            return encRequest;
//        } catch (Exception var4) {
//            Exception e = var4;
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static String encrypt(String key, TreeMap<String, String> keyValueMap) {
//        try {
//            if (keyValueMap != null && keyValueMap.size() > 0) {
//                StringBuffer sbPlainText = new StringBuffer("");
//
//                String keyParam;
//                for(Iterator var3 = keyValueMap.keySet().iterator(); var3.hasNext(); sbPlainText = sbPlainText.append(keyParam).append("=").append((String)keyValueMap.get(keyParam)).append("&")) {
//                    keyParam = (String)var3.next();
//                }
//
//                if (sbPlainText.toString().length() > 0) {
//                    String plainText = sbPlainText.toString().substring(0, sbPlainText.toString().length() - 1);
//                    AesCryptUtil aesUtil = new AesCryptUtil(key);
//                    String encRequest = aesUtil.encrypt(plainText);
//                    return encRequest;
//                }
//            }
//        } catch (Exception var6) {
//            Exception e = var6;
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    public static TreeMap decrypt(String key, String encResp) {
//        TreeMap<String, String> keyValueMap = null;
//
//        try {
//            AesCryptUtil aesUtil = new AesCryptUtil(key);
//            String decResp = aesUtil.decrypt(encResp);
//            StringTokenizer tokenizer = new StringTokenizer(decResp, "&");
//            String pair = null;
//            String pname = null;
//            String pvalue = null;
//            keyValueMap = new TreeMap();
//
//            while(tokenizer.hasMoreTokens()) {
//                pair = tokenizer.nextToken();
//                if (pair != null) {
//                    StringTokenizer strTok = new StringTokenizer(pair, "=");
//                    pname = "";
//                    pvalue = "";
//                    if (strTok.hasMoreTokens()) {
//                        pname = strTok.nextToken();
//                        if (strTok.hasMoreTokens()) {
//                            pvalue = strTok.nextToken();
//                        }
//
//                        keyValueMap.put(pname, pvalue);
//                    }
//                }
//            }
//        } catch (Exception var10) {
//            Exception e = var10;
//            keyValueMap = null;
//            e.printStackTrace();
//        }
//
//        return keyValueMap;
//    }
//}
