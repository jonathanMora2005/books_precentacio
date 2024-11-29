package com.jonathan.utils;
import com.jonathan.services.CryptoUtils;
import com.jonathan.services.KeystoreUtils;
import rawhttp.core.*;
import rawhttp.core.body.HttpMessageBody;
import rawhttp.core.body.StringBody;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;


public class RestClientImpl implements RestClient {
    private final int port;
    private final String host;

    public RestClientImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public <T> T get(String path, Class<T> returnType, BodyDecoder decoder,HeaderEntry... entries) throws Exception {
        return execRequest("GET", path, null, returnType, decoder, null, entries);
    }

    @Override
    public <T> T[] getAll(String path, Class<T[]> returnType, BodyDecoder decoder, HeaderEntry... entries) throws Exception {
        return execRequest("GET", path, null, returnType, decoder, null, entries);
    }

    @Override
    public void post(String path, String body, BodyEncoder encoder,HeaderEntry... entries) throws Exception {
        execRequest("POST", path, body, Void.class, null, encoder, entries);
    }

    @Override
    public void put(String path, String body, BodyEncoder encoder, HeaderEntry... entries) throws Exception {
        execRequest("PUT", path, body, Void.class, null, encoder, entries);
    }
    @Override
    public void delete(String path, String body, HeaderEntry... entries) throws Exception {
        execRequest("DELETE", path, body, Void.class, null, null, entries);
    }


    protected <T> T execRequest(String method, String path, String body, Class<T> returnType, BodyDecoder decoder, BodyEncoder encoder, HeaderEntry... entries) throws Exception {
        var rawHttp = new RawHttp();

        var symmetricKey = CryptoUtils.createSecretKey();
        Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/client1.p12");
        System.out.println(PROPERTIES_FILE);

        var serverCertificate = KeystoreUtils.loadCertificate(PROPERTIES_FILE.toString(), "Teknos01.", "server");
        var encryptedSymmetricKey = CryptoUtils.asymmetricEncrypt(
                CryptoUtils.toBase64(symmetricKey.getEncoded()),  // Convertir la clave simétrica a Base64
                serverCertificate.getPublicKey()
        );





        if (method.equals("POST") || method.equals("PUT")) {
            body = CryptoUtils.encrypt(body, symmetricKey);  // Cifra el cuerpo con la clave simétrica
        }

        // Initialize socket and ensure it's closed after use
        try (var socket = new Socket(host, port)) {
            if (body == null) {
                body = "";
            }

            String bodyHash = CryptoUtils.getHash(body);

            System.out.println("encryptedSymmetricKey   " + encryptedSymmetricKey);
            System.out.println("bodyHash   " + bodyHash);

            String fullPath = String.format("http://%s:%d/%s/%s/%s", host, port, path, encryptedSymmetricKey, bodyHash);
            System.out.println(fullPath);
            // Crear la solicitud HTTP
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

// Crear el cuerpo del mensaje con los bytes
            HttpMessageBody messageBody = new StringBody(body);
            var request = rawHttp.parseRequest(
                    method + " " + fullPath + " HTTP/1.1\r\n" +
                            "Host: " + host + "\r\n" +
                            "User-Agent: RawHTTP\r\n" +
                            "Content-Type: application/json\r\n" +
                            "Accept: application/json\r\n"
            ).withBody(messageBody);
            System.out.println(request);
            // Send request
            request.writeTo(socket.getOutputStream());

            // Parse the response
            try {
                if (method.equals("GET")) {
                    System.out.println("pasa per aqui");

                    var response = rawHttp.parseResponse(socket.getInputStream()).eagerly();
                    // Check for response body and deserialize if necessary

                    if (response.getBody().isPresent()) {
                        String responseBody = new String(response.getBody().get().asRawBytes());
                        System.out.println(responseBody);
                        System.out.println("pasa per aqui");

                        String a = CryptoUtils.decrypt(responseBody, symmetricKey);
                        System.out.println(a);
                        try {
                            return Mappers.get().readValue(a, returnType);
                        } catch (Exception e) {
                            throw new RuntimeException("Error al deserializar la respuesta: " + responseBody, e);
                        }
                    }
                }

                // Return null for Void type
                return null;
            } catch (IOException e) {
                // Log or wrap the exception with additional context
                return null;
            }
        }
    }

}