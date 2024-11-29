package com.jonathan.services.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jonathan.mybooklist.RepositoryFactory;
import com.jonathan.mybooklist.models.Author;
import com.jonathan.mybooklist.models.Genre;
import com.jonathan.mybooklist.models.ModelFactory;
import com.jonathan.mybooklist.repositories.AuthorRepository;
import com.jonathan.mybooklist.repositories.PersonalinformationRepository;

import java.util.Set;

public class AuthorControler implements Controller{
    AuthorRepository repository;

    private final RepositoryFactory repositoryFactory;
    private final ModelFactory modelFactory;
    public AuthorControler(RepositoryFactory repositoryFactory, ModelFactory modelFactory) {
        this.repositoryFactory = repositoryFactory;
        this.modelFactory = modelFactory;
    }
    @Override
    public String get(int k) {
        Author genre = repositoryFactory.getAuthorRepository().get(k);
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{")
                .append("\"id\":").append(genre.getId()).append(",")
                .append("\"name\":\"").append(genre.getnom()).append("\",")
                .append("\"surname\":\"").append(genre.getsurname()).append("\"")
                .append("}");
        return jsonBuilder.toString();
    }


    @Override
    public String get() {
        Set<Author> genres = repositoryFactory.getAuthorRepository().getAll();
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");

        int count = 0; // Contador para saber la posición actual
        int size = genres.size(); // Tamaño total del conjunto de autores

        for (Author genre : genres) {
            jsonBuilder.append("{")
                    .append("\"id\":").append(genre.getId()).append(",")
                    .append("\"name\":\"").append(genre.getnom()).append("\",")
                    .append("\"surname\":\"").append(genre.getsurname()).append("\"")
                    .append("}"); // Cierra el objeto JSON.

            // Solo añade una coma si NO es el último objeto.
            if (count < size - 1) {
                jsonBuilder.append(",");
            }

            count++;
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }



    @Override
    public void post(String value) {
        try {
            // Parse the input value if necessary
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonValue = mapper.readTree(value);

            // Validate the input
            if (!jsonValue.has("name") || jsonValue.get("name").asText().isEmpty()) {
                throw new IllegalArgumentException("Author name is required.");
            }

            // Create and populate the Author object
            Author author = new com.uvic.teknos.book.models.Author();
            author.setId(0); // Consider if this is necessary
            author.setnom(jsonValue.get("name").asText());
            author.setsurname(jsonValue.get("surname").asText());


            // Save to the repository
            repositoryFactory.getAuthorRepository().save(author);
        } catch (Exception e) {
            // Log and handle exceptions
            System.err.println("Error saving author: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save author.", e);
        }
    }


    @Override
    public void put(int key, String value) {
        Author author =new com.uvic.teknos.book.models.Author();
        author.setId(key);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String nombre = jsonNode.get("name").asText();
        String surname = jsonNode.get("surname").asText();
        author.setnom(nombre);
        author.setsurname(surname);
        repositoryFactory.getAuthorRepository().save(author); ;

    }

    @Override
    public void delete(int key) {
        Author author =new com.uvic.teknos.book.models.Author();
        author.setId(key);
        repositoryFactory.getAuthorRepository().delete(author) ;
    }
}
