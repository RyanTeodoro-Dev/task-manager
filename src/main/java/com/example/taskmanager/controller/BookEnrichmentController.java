package com.example.taskmanager.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskmanager.service.BookEnrichmentService;

import lombok.RequiredArgsConstructor;

// Controller responsável por buscar informações de livros pelo ISBN na Open Library
@RestController
@RequestMapping("/books/isbn")
@RequiredArgsConstructor
@CrossOrigin("*") // Permite requisições de qualquer origem (frontend)
public class BookEnrichmentController {

    private final BookEnrichmentService enrichmentService;

    // Endpoint para buscar os metadados de um livro pelo ISBN
    @GetMapping("/{isbn}")
    public Map<String, Object> findByIsbn(@PathVariable String isbn) {
        return enrichmentService.enrichByIsbn(isbn);
    }
}