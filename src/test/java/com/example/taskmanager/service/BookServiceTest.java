package com.example.taskmanager.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.taskmanager.model.Book;
import com.example.taskmanager.repository.BookRepository;

// Testes de integração do BookService usando MongoDB real via Testcontainers
@SpringBootTest
@Testcontainers
public class BookServiceTest {

    // Sobe um container MongoDB real para os testes
    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6");

    // Configura a URI do MongoDB para apontar para o container
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                mongoDBContainer::getReplicaSetUrl
        );
    }

    @Autowired
    private BookService service;

    @Autowired
    private BookRepository repository;

    private Book book;

    // Limpa o banco e prepara um livro base antes de cada teste
    @BeforeEach
    void setup() {
        repository.deleteAll();

        book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setDescription("Livro sobre boas práticas");
        book.setUserEmail("user@email.com");
    }

    // Deve criar um livro e verificar que o ID foi gerado
    @Test
    void shouldCreateBook() {

        Book saved = service.create(book);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Clean Code", saved.getTitle());
    }

    // Deve retornar os livros do usuário pelo e-mail
    @Test
    void shouldFindBooksByUserEmail() {

        repository.save(book);

        List<Book> books = service.findAll("user@email.com");

        assertFalse(books.isEmpty());
        assertEquals(1, books.size());
    }

    // Deve encontrar um livro pelo ID
    @Test
    void shouldFindBookById() {

        Book saved = repository.save(book);

        Book found = service.findById(saved.getId());

        assertNotNull(found);
        assertEquals("Clean Code", found.getTitle());
    }

    // Deve atualizar os dados de um livro existente
    @Test
    void shouldUpdateBook() {

        Book saved = repository.save(book);

        Book updatedBook = new Book();
        updatedBook.setTitle("Novo Livro");
        updatedBook.setAuthor("Novo Autor");
        updatedBook.setDescription("Nova descrição");
        updatedBook.setUserEmail("user@email.com");

        Book result = service.update(saved.getId(), updatedBook);

        assertNotNull(result);
        assertEquals("Novo Livro", result.getTitle());
        assertEquals("Novo Autor", result.getAuthor());
    }

    // Deve excluir um livro e confirmar que o banco ficou vazio
    @Test
    void shouldDeleteBook() {

        Book saved = repository.save(book);

        service.delete(saved.getId());

        // Verifica que o livro foi removido do banco
        List<Book> books = repository.findAll();

        assertTrue(books.isEmpty());
    }

    // Deve lançar exceção ao buscar um livro com ID inexistente
    @Test
    void shouldThrowExceptionWhenBookNotFound() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.findById("999")
                );

        assertEquals("Book not found", exception.getMessage());
    }
}