package com.example.taskmanager.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.taskmanager.client.OpenLibraryClient;
import com.example.taskmanager.client.OpenLibraryResponse;

import lombok.RequiredArgsConstructor;

// Service responsável por buscar e enriquecer dados de livros via Open Library
@Service
@RequiredArgsConstructor
public class BookEnrichmentService {

    private final OpenLibraryClient openLibraryClient;

    // Busca metadados de um livro pelo ISBN e retorna um mapa com as informações
    public Map<String, Object> enrichByIsbn(String isbn) {

        // Consulta a Open Library pelo ISBN
        Optional<OpenLibraryResponse> result = openLibraryClient.findByIsbn(isbn);

        // Retorna mensagem de erro se o livro não for encontrado
        if (result.isEmpty()) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("found", false);
            notFound.put("isbn", isbn);
            notFound.put("message", "Livro não encontrado na Open Library");
            return notFound;
        }

        // Monta o mapa com os dados encontrados
        OpenLibraryResponse book = result.get();
        Map<String, Object> enriched = new HashMap<>();
        enriched.put("found", true);
        enriched.put("isbn", isbn);
        enriched.put("title", book.getTitle());
        enriched.put("pages", book.getNumberOfPages());

        // Adiciona o primeiro autor, se disponível
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            enriched.put("author", book.getAuthors().get(0).getName());
        }

        // Adiciona a primeira editora, se disponível
        if (book.getPublishers() != null && !book.getPublishers().isEmpty()) {
            enriched.put("publisher", book.getPublishers().get(0).getName());
        }

        return enriched;
    }
}