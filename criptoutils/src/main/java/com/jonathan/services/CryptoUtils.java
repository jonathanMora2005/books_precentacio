package com.jonathan.services;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptoUtils {
    // Cambiar a URL-safe Base64 encoder y decoder
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    public static String getHash(String text) throws Exception {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(text.getBytes());
            return toBase64(hash); // Usar URL-safe Base64
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        }
    }

    public static SecretKey createSecretKey() throws Exception {
        try {
            return KeyGenerator.getInstance("AES").generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(e);
        }
    }

    public static SecretKey decodeSecretKey(String base64SecretKey) {
        var bytes = decoder.decode(base64SecretKey); // Decodificar usando URL-safe Base64
        return new SecretKeySpec(bytes, 0, bytes.length, "AES");
    }

    public static String encrypt(String plainText, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        var cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        var encryptedBytes = cipher.doFinal(plainText.getBytes());
        return toBase64(encryptedBytes); // Usar URL-safe Base64
    }

    public static String decrypt(String encryptedTextBase64, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        var cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        var encryptedBytes = fromBase64(encryptedTextBase64); // Decodificar usando URL-safe Base64
        var decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    public static String asymmetricEncrypt(String plainTextBase64, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        var cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        var encryptedBytes = cipher.doFinal(plainTextBase64.getBytes());
        return toBase64(encryptedBytes); // Usar URL-safe Base64
    }

    public static String asymmetricDecrypt(String encryptedTextBase64, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        var cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        var encryptedBytes = fromBase64(encryptedTextBase64); // Decodificar usando URL-safe Base64
        var decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    // Métodos privados actualizados para usar URL-safe Base64
    public static String toBase64(byte[] bytes) {
        return encoder.encodeToString(bytes);
    }

    private static byte[] fromBase64(String base64) {
        return decoder.decode(base64);
    }
}
