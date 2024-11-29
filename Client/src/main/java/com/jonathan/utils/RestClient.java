package com.jonathan.utils;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface RestClient {
    class HeaderEntry {
        public String key;
        public String value;

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public HeaderEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    @FunctionalInterface
    interface BodyDecoder {
        String decode(String body);
    }

    interface BodyEncoder {
        String encode(String body) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;
    }


    <T> T get(String path, Class<T> returnType, BodyDecoder decoder, HeaderEntry... entries) throws Exception;

    <T> T[] getAll(String path, Class<T[]> returnType, BodyDecoder decoder, HeaderEntry... entries) throws Exception;

    void post(String path, String body, BodyEncoder encoder, HeaderEntry... entries) throws Exception;

    void put(String path, String body, BodyEncoder encoder, HeaderEntry... entries) throws Exception;

    void delete(String path, String body, HeaderEntry... entries) throws Exception;
}