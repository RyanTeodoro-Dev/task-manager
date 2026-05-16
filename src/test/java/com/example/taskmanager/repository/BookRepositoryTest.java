package com.example.taskmanager.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.taskmanager.model.Book;

// Testes de integração do BookRepository usando MongoDB real via Testcontainers
@DataMongoTest
@Testcontainers
public class BookRepositoryTest {

    // Sobe um container MongoDB real para os testes
    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6");

    // Configura a URI do MongoDB para apontar para o container
    @DynamicPropertySource
    static void setProperties(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.mongodb.uri",
                mongoDBContainer::getReplicaSetUrl
        );
    }

    @Autowired
    private BookRepository repository;

    // Limpa o banco antes de cada teste para evitar dados duplicados
    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    // Deve salvar um livro e verificar que o ID foi gerado
    @Test
    void shouldSaveBook() {

        Book book = new Book();

        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setDescription("Livro Java");
        book.setUserEmail("user@email.com");

        Book saved = repository.save(book);

        // Verifica que o ID foi gerado automaticamente pelo MongoDB
        assertNotNull(saved.getId());

        // Verifica que o título foi salvo corretamente
        assertEquals(
                "Clean Code",
                saved.getTitle()
        );
    }

    // Deve retornar os livros de um usuário pelo e-mail
    @Test
    void shouldFindBooksByUserEmail() {

        Book book = new Book();

        book.setTitle("Java");
        book.setAuthor("Autor");
        book.setDescription("Descricao");
        book.setUserEmail("user@email.com");

        repository.save(book);

        List<Book> books =
                repository.findByUserEmail(
                        "user@email.com"
                );

        // Verifica que a lista não está vazia
        assertFalse(books.isEmpty());

        // Verifica que o livro correto foi retornado
        assertEquals(
                "Java",
                books.get(0).getTitle()
        );
    }
}