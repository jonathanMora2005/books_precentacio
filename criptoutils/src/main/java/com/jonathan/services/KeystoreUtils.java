package com.jonathan.services;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class KeystoreUtils {

    public KeystoreUtils() throws Exception {
    }

    // Cargar la clave privada desde un keystore
    public static PrivateKey loadPrivateKey(String keystorePath, String keystorePassword, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        return (PrivateKey) keyStore.getKey(alias, keystorePassword.toCharArray());
    }
    PrivateKey privateKey = loadPrivateKey("server.p12", "Teknos01.", "server");

    // Cargar un certificado desde un keystore
    public static Certificate loadCertificate(String keystorePath, String keystorePassword, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        return keyStore.getCertificate(alias);
    }
}
