package com.example.taskmanager.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

// DTO com os campos retornados pela Open Library API
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos extras que a API possa retornar
public class OpenLibraryResponse {

    // Título do livro
    private String title;

    // Número de páginas (nome diferente no JSON da API)
    @JsonProperty("number_of_pages")
    private Integer numberOfPages;

    // Lista de autores do livro
    private List<Author> authors;

    // Lista de editoras do livro
    private List<Publisher> publishers;

    // Classe interna que representa um autor
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
    }

    // Classe interna que representa uma editora
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Publisher {
        private String name;
    }
}