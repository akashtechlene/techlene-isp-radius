/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesCryptUtil {
    Cipher ecipher;
    Cipher dcipher;
    byte[] buf = new byte[1024];
    static final String HEXES = "0123456789ABCDEF";

    public AesCryptUtil() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            this.setupCrypto(kgen.generateKey());
        } catch (Exception var2) {
            Exception e = var2;
            e.printStackTrace();
        }

    }

    public AesCryptUtil(String key) {
        SecretKeySpec skey = new SecretKeySpec(getMD5(key), "AES");
        this.setupCrypto(skey);
    }

    private void setupCrypto(SecretKey key) {
        byte[] iv = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

        try {
            this.ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            this.dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            this.ecipher.init(1, key, paramSpec);
            this.dcipher.init(2, key, paramSpec);
        } catch (Exception var5) {
            Exception e = var5;
            e.printStackTrace();
        }

    }

    public void encrypt(InputStream in, OutputStream out) {
        try (CipherOutputStream cos = new CipherOutputStream(out, this.ecipher)) {

            int numRead;
            while ((numRead = in.read(this.buf)) != -1) {
                cos.write(this.buf, 0, numRead);
            }
            cos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String plaintext) {
        try {
            byte[] ciphertext = this.ecipher.doFinal(plaintext.getBytes("UTF-8"));
            return byteToHex(ciphertext);
        } catch (Exception var3) {
            Exception e = var3;
            e.printStackTrace();
            return null;
        }
    }

    public void decrypt(InputStream in, OutputStream out) {
        try {
            InputStream inputStream = new CipherInputStream(in, this.dcipher);

            int numRead;
            while((numRead = inputStream.read(this.buf)) >= 0) {
                out.write(this.buf, 0, numRead);
            }

            out.close();
        } catch (IOException var4) {
            IOException e = var4;
            e.printStackTrace();
        }

    }

    public String decrypt(String hexCipherText) {
        try {
            String plaintext = new String(this.dcipher.doFinal(hexToByte(hexCipherText)), "UTF-8");
            return plaintext;
        } catch (Exception var3) {
            Exception e = var3;
            e.printStackTrace();
            return null;
        }
    }

    public String decrypt(byte[] ciphertext) {
        try {
            String plaintext = new String(this.dcipher.doFinal(ciphertext), "UTF-8");
            return plaintext;
        } catch (Exception var3) {
            Exception e = var3;
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] getMD5(String input) {
        try {
            byte[] bytesOfMessage = input.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (Exception var3) {
            return null;
        }
    }

    public static String byteToHex(byte[] raw) {
        if (raw == null) {
            return null;
        } else {
            String result = "";

            for(int i = 0; i < raw.length; ++i) {
                result = result + Integer.toString((raw[i] & 255) + 256, 16).substring(1);
            }

            return result;
        }
    }

    public static byte[] hexToByte(String hexString) {
        int len = hexString.length();
        byte[] ba = new byte[len / 2];

        for(int i = 0; i < len; i += 2) {
            ba[i / 2] = (byte)((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }

        return ba;
    }

    public static void main(String[] args) {
        String result = null;
        String err = null;
        String key = null;
        String data = null;
        String action = null;
        if (args != null && args.length >= 3) {
            key = args[0];
            data = args[1];
            action = args[2];
            if (key == null) {
                err = "error: no key";
            } else if (key.length() < 32) {
                err = "error: key length less than 32 bytes";
            } else if (data != null && action != null) {
                if (action == null) {
                    err = "error: no action";
                } else if (!action.equals("enc") && !action.equals("dec")) {
                    err = "error: invalid action";
                }
            } else {
                err = "error: no data";
            }
        } else {
            err = "error: missing one or more arguments. Usage: AesCryptUtil key data <enc|dec>";
        }

        if (err == null) {
            try {
                AesCryptUtil encrypter = new AesCryptUtil(key);
                if (action.equals("enc")) {
                    result = encrypter.encrypt(data);
                } else {
                    result = encrypter.decrypt(data);
                }
            } catch (Exception var7) {
                Exception e = var7;
                err = "error : Exception in performing the requested operation : " + e;
            }
        }

        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println(err);
        }

    }
}
