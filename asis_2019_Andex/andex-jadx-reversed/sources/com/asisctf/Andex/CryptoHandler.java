package com.asisctf.Andex;

import android.util.Base64;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHandler {
    static void encrypt(String str, String str2) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fileInputStream = new FileInputStream(str);
        FileOutputStream fileOutputStream = new FileOutputStream(str2);
        String str3 = "AES";
        SecretKeySpec secretKeySpec = new SecretKeySpec("MyDifficultPassw".getBytes(), str3);
        Cipher instance = Cipher.getInstance(str3);
        instance.init(1, secretKeySpec);
        CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, instance);
        byte[] bArr = new byte[8];
        while (true) {
            int read = fileInputStream.read(bArr);
            if (read != -1) {
                cipherOutputStream.write(bArr, 0, read);
            } else {
                cipherOutputStream.flush();
                cipherOutputStream.close();
                fileInputStream.close();
                return;
            }
        }
    }

    static void decrypt(String str, String str2, String str3) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fileInputStream = new FileInputStream(str2);
        FileOutputStream fileOutputStream = new FileOutputStream(str);
        String str4 = "AES";
        SecretKeySpec secretKeySpec = new SecretKeySpec(str3.getBytes(), str4);
        Cipher instance = Cipher.getInstance(str4);
        instance.init(2, secretKeySpec);
        CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, instance);
        byte[] bArr = new byte[8];
        while (true) {
            int read = cipherInputStream.read(bArr);
            if (read != -1) {
                fileOutputStream.write(bArr, 0, read);
            } else {
                fileOutputStream.flush();
                fileOutputStream.close();
                cipherInputStream.close();
                return;
            }
        }
    }

    public static final String md5(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes());
            byte[] digest = instance.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hexString = Integer.toHexString(b & 255);
                while (hexString.length() < 2) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("0");
                    sb2.append(hexString);
                    hexString = sb2.toString();
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String b64Decode(String str) {
        try {
            return new String(Base64.decode(str, 0), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
