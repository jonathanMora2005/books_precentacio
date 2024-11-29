package com.jonathan.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathan.services.controllers.Controller;
import com.jonathan.services.controllers.GenreControler;
import com.uvic.teknos.book.models.Genre;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.HttpMessageBody;
import rawhttp.core.body.StringBody;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jonathan.services.KeystoreUtils.loadPrivateKey;

public class RequestRouterImpl implements RequestRouter {
    private static RawHttp rawHttp = new RawHttp();
    private final Map<String, Controller> controllers;

    public RequestRouterImpl(Map<String, Controller> controllers) {
        this.controllers = controllers;
    }

    /**
     *
     *
     * @param request
     * @return the http response to send to the client
     */

    public RawHttpResponse<?> execRequest(RawHttpRequest request) throws Exception {
        var path = request.getUri().getPath();
        var method = request.getMethod();
        var pathParts = path.split("/");
        var controllerName = pathParts[1];
        var responseJsonBody = "";



        switch (controllerName) {
            case "genre":
                responseJsonBody = manageGenre(request, method, pathParts, responseJsonBody);
                break;
            case "author":
                responseJsonBody = manageAuthor(request, method, pathParts, responseJsonBody);
                break;
            case "publishing":
                responseJsonBody = managePublishing(request, method, pathParts, responseJsonBody);
                break;
            case "readBook":
                responseJsonBody = manageReadBook(request, method, pathParts, responseJsonBody);
                break;
            case "peandingBook":
                responseJsonBody = managePeandingBook(request, method, pathParts, responseJsonBody);
                break;

        }

        RawHttpResponse response = null;

        HttpMessageBody messageBody = new StringBody(responseJsonBody);

        try {
            response = rawHttp.parseResponse(
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "\r\n"
            ).withBody(messageBody);
            System.out.println(response);
        } catch (Exception exception) {
            response = null;
        }
        return response;
    }

    private String managePeandingBook(RawHttpRequest request, String method, String[] pathParts, String responseJsonBody) {
        var controller = controllers.get(pathParts[1]);
        if (method.equals("POST")) {
            String studentJson;
            if (request.getBody().isPresent()) {
                // Si hay un cuerpo, lee el contenido
                studentJson = request.getBody().get().toString();
            } else {
                // Si no hay cuerpo, maneja el error
                System.out.println("El cuerpo de la solicitud no puede estar vacío.");
            }
            var publishingJson = request.getBody().get().toString();
            controller.post(publishingJson);

        } else if (method.equals("GET") && pathParts.length >= 2) {
            System.out.println("pasar per aqui");
            responseJsonBody = controller.get(Integer.parseInt(pathParts[2]));


        }else if (method.equals("GET")) {
            responseJsonBody = controller.get();

        } else if (method.equals("DELETE")) {
            var publishingId = Integer.parseInt(pathParts[2]);
            controller.delete(publishingId);
        } else if (method.equals("PUT")) {
            var publishingId = Integer.parseInt(pathParts[2]);
            var mapper = new ObjectMapper();
            var authorJson = request.getBody().get().toString();
            controller.put(publishingId, authorJson);
        }
        return responseJsonBody;
    }

    private String manageReadBook(RawHttpRequest request, String method, String[] pathParts, String responseJsonBody) {
        var controller = controllers.get(pathParts[1]);
        if (method.equals("POST")) {
            var readBookJson = request.getBody().get().toString();
            controller.post(readBookJson);

        } else if (method.equals("GET") && pathParts[2] == "0") {
            responseJsonBody = controller.get();
        }else if (method.equals("GET")) {
            responseJsonBody = controller.get(Integer.parseInt(pathParts[2]));

        } else if (method.equals("DELETE")) {
            var readBookId = Integer.parseInt(pathParts[2]);
            controller.delete(readBookId);
        } else if (method.equals("PUT")) {
            var readBookId = Integer.parseInt(pathParts[2]);
            var mapper = new ObjectMapper();
            var readBookJson = request.getBody().get().toString();
            controller.put(readBookId, readBookJson);
        }
        return responseJsonBody;
    }

    private String managePublishing(RawHttpRequest request, String method, String[] pathParts, String responseJsonBody) throws Exception {
        var controller = controllers.get(pathParts[1]);

        if (method.equals("POST")) {
            String jsonBody = request.getBody()
                    .map(bodyReader -> {
                        try {
                            // Leer todos los bytes del cuerpo
                            byte[] bodyBytes = bodyReader.asRawBytes();
                            // Convertir los bytes a String utilizando UTF-8
                            return new String(bodyBytes, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // Manejo de errores al leer el cuerpo
                            e.printStackTrace();
                            return "{sdfsdf}"; // Devuelve un cuerpo vacío en caso de error
                        }
                    })
                    .orElse("{}"); // Proporciona un cuerpo vacío si no hay cuerpo

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonBody);
            String nombre = jsonNode.get("nombre").asText();
            String country = jsonNode.get("country").asText();
            String email = jsonNode.get("email").asText();
            System.out.println(nombre);
            System.out.println(country);
            System.out.println(email);
            var publishingJson = nombre+"/"+country+"/"+email;
            controller.post(publishingJson);

        } else if (method.equals("GET") && Integer.parseInt(pathParts[2]) == 0) {

            responseJsonBody = controller.get();
            System.out.println(responseJsonBody);


        }else if (method.equals("GET")) {

            responseJsonBody = controller.get();
            System.out.println(responseJsonBody);

        } else if (method.equals("DELETE")) {
            var publishingId = Integer.parseInt(pathParts[2]);
            controller.delete(publishingId);
        } else if (method.equals("PUT")) {
            var publishingId = Integer.parseInt(pathParts[2]);
            String jsonBody = request.getBody()
                    .map(bodyReader -> {
                        try {
                            // Leer todos los bytes del cuerpo
                            byte[] bodyBytes = bodyReader.asRawBytes();
                            // Convertir los bytes a String utilizando UTF-8
                            return new String(bodyBytes, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // Manejo de errores al leer el cuerpo
                            e.printStackTrace();
                            return "{sdfsdf}"; // Devuelve un cuerpo vacío en caso de error
                        }
                    })
                    .orElse("{}"); // Proporciona un cuerpo vacío si no hay cuerpo



            controller.put(publishingId, jsonBody);
        }
        String encryptedSymmetricKey = pathParts[3];

        // Ruta del archivo del keystore
        Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

        // Cargar la clave privada del servidor
        PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

        SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
        );
        var body = CryptoUtils.encrypt(responseJsonBody, symmetricKey);

        return body;
    }

    private String manageAuthor(RawHttpRequest request, String method, String[] pathParts, String responseJsonBody) throws Exception {
        var controller = controllers.get(pathParts[1]);
        System.out.println("author");
        if (method.equals("POST")) {
            // Extraer parámetros del path
            System.out.println("post");
            String encryptedSymmetricKey = pathParts[3];
            String receivedBodyHash = pathParts[4];

            // Ruta del archivo del keystore
            Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

            // Cargar la clave privada del servidor
            PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

            // Descifrar la clave simétrica enviada por el cliente
            SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                    CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
            );
            String jsonBody = request.getBody()
                    .map(bodyReader -> {
                        try {
                            // Leer todos los bytes del cuerpo
                            byte[] bodyBytes = bodyReader.asRawBytes();
                            // Convertir los bytes a String utilizando UTF-8
                            return new String(bodyBytes, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // Manejo de errores al leer el cuerpo
                            e.printStackTrace();
                            return "{}"; // Devuelve un cuerpo vacío en caso de error
                        }
                    })
                    .orElse("{}"); // Proporciona un cuerpo vacío si no hay cuerpo

            String calculatedHash = CryptoUtils.getHash(jsonBody);
            if (!calculatedHash.equals(receivedBodyHash)) {
                System.out.println("Hash mismatch, invalid request.");
                return ""; // Manejo del error
            }

            // Descifrar el cuerpo
            String decryptedBody = CryptoUtils.decrypt(jsonBody, symmetricKey);
            System.out.println(decryptedBody);
            controller.post(decryptedBody);
        } else if (method.equals("GET") && Integer.parseInt(pathParts[2]) != 0) {
            System.out.println("get ");
            responseJsonBody = controller.get(Integer.parseInt(pathParts[2]));
            System.out.println(responseJsonBody);
        }else if (method.equals("GET")) {
            System.out.println("get all");
            responseJsonBody = controller.get();
            System.out.println(responseJsonBody);
        } else if (method.equals("DELETE")) {
            var genreId = Integer.parseInt(pathParts[2]);
            controller.delete(genreId);
        } else if (method.equals("PUT")) {
            System.out.println("PUT");
            System.out.println("put");
            String encryptedSymmetricKey = pathParts[3];
            String receivedBodyHash = pathParts[4];

            // Ruta del archivo del keystore
            Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

            // Cargar la clave privada del servidor
            PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

            // Descifrar la clave simétrica enviada por el cliente
            SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                    CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
            );
            var authorId = Integer.parseInt(pathParts[2]);
            String jsonBody = request.getBody()
                    .map(bodyReader -> {
                        try {
                            // Leer todos los bytes del cuerpo
                            byte[] bodyBytes = bodyReader.asRawBytes();
                            // Convertir los bytes a String utilizando UTF-8
                            return new String(bodyBytes, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // Manejo de errores al leer el cuerpo
                            e.printStackTrace();
                            return "{sdfsdf}"; // Devuelve un cuerpo vacío en caso de error
                        }
                    })
                    .orElse("{}"); // Proporciona un cuerpo vacío si no hay cuerpo
            String calculatedHash = CryptoUtils.getHash(jsonBody);
            if (!calculatedHash.equals(receivedBodyHash)) {
                System.out.println("Hash mismatch, invalid request.");
                return ""; // Manejo del error
            }
            String decryptedBody = CryptoUtils.decrypt(jsonBody, symmetricKey);
            System.out.println(decryptedBody);
            controller.put(authorId, decryptedBody);

        }
        String encryptedSymmetricKey = pathParts[3];

        // Ruta del archivo del keystore
        Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

        // Cargar la clave privada del servidor
        PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

        SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
        );
        var body = CryptoUtils.encrypt(responseJsonBody, symmetricKey);

        return body;
    }

    private String readRequestBody(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    private String manageGenre(RawHttpRequest request, String method, String[] pathParts, String responseJsonBody) throws Exception {
        System.out.println("pasa per genere");
        var controller = controllers.get(pathParts[1]);
        if (method.equals("POST")) {
            System.out.println("Es un POST");

            // Extraer parámetros del path
            String encryptedSymmetricKey = pathParts[3];
            String receivedBodyHash = pathParts[4];

            // Ruta del archivo del keystore
            Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

            // Cargar la clave privada del servidor
            PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

            // Descifrar la clave simétrica enviada por el cliente
            SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                    CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
            );

            // Leer el cuerpo de la solicitud
            String encryptedBody = request.getBody()
                    .map(bodyReader -> {
                        try {
                            byte[] bodyBytes = bodyReader.asRawBytes();
                            return new String(bodyBytes, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }
                    })
                    .orElse("");

            // Validar el hash del cuerpo cifrado (antes de descifrar)
            String calculatedHash = CryptoUtils.getHash(encryptedBody);
            if (!calculatedHash.equals(receivedBodyHash)) {
                System.out.println("Hash mismatch, invalid request.");
                return ""; // Manejo del error
            }

            // Descifrar el cuerpo
            String decryptedBody = CryptoUtils.decrypt(encryptedBody, symmetricKey);

            // Parsear el JSON del cuerpo descifrado
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(decryptedBody);

            // Extraer campos necesarios del JSON
            String nombre = jsonNode.get("description").asText();
            System.out.println("Descripción recibida: " + nombre);

            // Llamar al controlador con los datos procesados
            controller.post(nombre);
        }
        else if (method.equals("GET") && Integer.parseInt(pathParts[2]) != 0) {
            responseJsonBody = controller.get(Integer.parseInt(pathParts[2]));


        }else if (method.equals("GET")) {
            responseJsonBody = controller.get();
        } else if (method.equals("DELETE")) {
            var genreId = Integer.parseInt(pathParts[2]);
            controller.delete(genreId);
        } else if (method.equals("PUT")) {
            System.out.println("put");
            String encryptedSymmetricKey = pathParts[3];
            String receivedBodyHash = pathParts[4];

            // Ruta del archivo del keystore
            Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

            // Cargar la clave privada del servidor
            PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

            // Descifrar la clave simétrica enviada por el cliente
            SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                    CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
            );

            String jsonBody = request.getBody()
                    .map(bodyReader -> {
                        try {
                            // Leer todos los bytes del cuerpo
                            byte[] bodyBytes = bodyReader.asRawBytes();
                            // Convertir los bytes a String utilizando UTF-8
                            return new String(bodyBytes, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // Manejo de errores al leer el cuerpo
                            e.printStackTrace();
                            return "{sdfsdf}"; // Devuelve un cuerpo vacío en caso de error
                        }
                    })
                    .orElse("{}"); // Proporciona un cuerpo vacío si no hay cuerpo
            String calculatedHash = CryptoUtils.getHash(jsonBody);
            if (!calculatedHash.equals(receivedBodyHash)) {
                System.out.println("Hash mismatch, invalid request.");
                return ""; // Manejo del error
            }
            String decryptedBody = CryptoUtils.decrypt(jsonBody, symmetricKey);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(decryptedBody);
            System.out.println(jsonNode);
            String nombre = jsonNode.get("name").asText();
            var genreId = jsonNode.get("id").asText();
            System.out.println(nombre);
            controller.put(Integer.parseInt(genreId), nombre);

        }
        String encryptedSymmetricKey = pathParts[3];

        // Ruta del archivo del keystore
        Path PROPERTIES_FILE = Paths.get("criptoutils/src/main/resources/server.p12");

        // Cargar la clave privada del servidor
        PrivateKey privateKey = loadPrivateKey(String.valueOf(PROPERTIES_FILE), "Teknos01.", "server");

        SecretKey symmetricKey = CryptoUtils.decodeSecretKey(
                CryptoUtils.asymmetricDecrypt(encryptedSymmetricKey, privateKey)
        );
        var body = CryptoUtils.encrypt(responseJsonBody, symmetricKey);

        return body;
    }
}