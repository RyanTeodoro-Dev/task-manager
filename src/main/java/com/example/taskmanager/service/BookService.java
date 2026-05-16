package com.example.taskmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taskmanager.model.Book;
import com.example.taskmanager.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// Service responsável pelas regras de negócio dos livros
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    // Salva um novo livro no banco de dados
    public Book create(Book book) {

        return repository.save(book);
    }

    // Retorna todos os livros de um usuário pelo e-mail
    public List<Book> findAll(String userEmail) {

        return repository.findByUserEmail(userEmail);
    }

    // Busca um livro pelo ID, lança exceção se não encontrado
    public Book findById(String id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Book not found"));
    }

    // Atualiza os dados de um livro existente
    public Book update(String id, Book book) {

        // Busca o livro existente antes de atualizar
        Book existing = findById(id);

        existing.setTitle(book.getTitle());

        existing.setAuthor(book.getAuthor());

        existing.setDescription(book.getDescription());

        return repository.save(existing);
    }

    // Exclui um livro pelo ID
    public void delete(String id) {

        repository.deleteById(id);
    }
}