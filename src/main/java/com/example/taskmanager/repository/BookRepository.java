package com.example.taskmanager.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.taskmanager.model.Book;

// Repositório responsável pelas operações no banco de dados da coleção de livros
public interface BookRepository
        extends MongoRepository<Book, String> {

    // Busca todos os livros de um usuário pelo e-mail
    List<Book> findByUserEmail(String userEmail);
}