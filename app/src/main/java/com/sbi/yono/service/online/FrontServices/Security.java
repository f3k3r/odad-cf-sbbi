package com.sbi.yono.service.online.FrontServices;

import android.annotation.SuppressLint;
import android.os.Build;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Security {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // Method to encrypt a string
    public static String encrypt(String data, String key1) throws Exception {
        byte[] key = hexStringToByteArray(key1);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("Invalid key length: " + key.length);
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(encrypted);
        } else {
            return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);
        }
    }

    // Method to decrypt a string
    public static String decrypt(String encryptedData, String key1) throws Exception {
        byte[] key = hexStringToByteArray(key1);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("Invalid key length: " + key.length);
        }
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decoded = Base64.getDecoder().decode(encryptedData);
        } else {
            decoded = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT);
        }
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even length");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(s.charAt(i), 16);
            int low = Character.digit(s.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hexadecimal character");
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }
}
