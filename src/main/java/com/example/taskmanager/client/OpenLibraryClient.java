package com.example.taskmanager.client;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

// Cliente HTTP para buscar informações de livros na Open Library API
@Service
@RequiredArgsConstructor
public class OpenLibraryClient {

    // Ferramentas para fazer requisições HTTP e converter JSON
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // URL base da API, configurável pelo application.properties
    @Value("${openlibrary.base-url:https://openlibrary.org}")
    private String baseUrl;

    // Busca informações de um livro pelo ISBN na Open Library
    public Optional<OpenLibraryResponse> findByIsbn(String isbn) {

        // Monta a URL com o ISBN para consultar a API
        String url = baseUrl + "/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";

        try {
            // Faz a requisição e recebe a resposta como texto
            String raw = restTemplate.getForObject(url, String.class);

            // Retorna vazio se a resposta for nula ou não tiver dados
            if (raw == null || raw.isBlank() || raw.equals("{}")) {
                return Optional.empty();
            }

            // A API retorna um mapa onde a chave é "ISBN:XXXX"
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(raw, Map.class);
            String key = "ISBN:" + isbn;

            // Retorna vazio se o ISBN não estiver presente na resposta
            if (!responseMap.containsKey(key)) {
                return Optional.empty();
            }

            // Converte os dados do livro para o objeto de resposta
            String bookJson = objectMapper.writeValueAsString(responseMap.get(key));
            OpenLibraryResponse response = objectMapper.readValue(bookJson, OpenLibraryResponse.class);
            return Optional.of(response);

        } catch (Exception e) {
            // Retorna vazio em caso de qualquer erro na requisição ou conversão
            return Optional.empty();
        }
    }
}