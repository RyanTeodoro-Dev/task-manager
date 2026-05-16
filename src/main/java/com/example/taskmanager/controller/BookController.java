package com.example.taskmanager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskmanager.model.Book;
import com.example.taskmanager.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Controller responsável pelos endpoints de gerenciamento de livros
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@CrossOrigin("*") // Permite requisições de qualquer origem (frontend)
public class BookController {

    private final BookService service;

    // Endpoint para cadastrar um novo livro
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Retorna status 201 ao criar com sucesso
    public Book create(@Valid @RequestBody Book book) {

        return service.create(book);
    }

    // Endpoint para listar todos os livros de um usuário pelo e-mail
    @GetMapping("/user/{email}")
    public List<Book> findAll(
            @PathVariable String email) {

        return service.findAll(email);
    }

    // Endpoint para buscar um livro pelo ID
    @GetMapping("/id/{id}")
    public Book findById(
            @PathVariable String id) {

        return service.findById(id);
    }

    // Endpoint para atualizar os dados de um livro pelo ID
    @PutMapping("/id/{id}")
    public Book update(
        @PathVariable String id,
        @Valid @RequestBody Book book) {

        return service.update(id, book);
    }

    // Endpoint para excluir um livro pelo ID
    @DeleteMapping("/id/{id}")
    public void delete(
            @PathVariable String id) {

        service.delete(id);
    }
}