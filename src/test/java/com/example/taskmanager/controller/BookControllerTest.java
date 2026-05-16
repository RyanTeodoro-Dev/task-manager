package com.example.taskmanager.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.taskmanager.model.Book;
import com.example.taskmanager.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// Testes de integração do BookController usando MongoDB real via Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BookControllerTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository repository;

    // Limpa o banco antes de cada teste para evitar dados duplicados
    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    // Deve cadastrar um livro com sucesso
    @Test
    void shouldCreateBook() throws Exception {

        Book book = new Book();

        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setDescription("Livro sobre boas práticas");
        book.setUserEmail("teste@email.com");

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated());
    }

    // Deve retornar os livros de um usuário pelo e-mail
    @Test
    void shouldFindBooksByUser() throws Exception {

        Book book = Book.builder()
                .title("Java")
                .author("Autor")
                .description("Descricao")
                .userEmail("user@email.com")
                .build();

        // Salva o livro direto no banco para o teste
        repository.save(book);

        mockMvc.perform(get("/books/user/user@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"));
    }

    // Deve retornar um livro pelo ID
    @Test
    void shouldFindBookById() throws Exception {

        Book saved = repository.save(
                Book.builder()
                        .title("Livro")
                        .author("Autor")
                        .description("Descricao")
                        .userEmail("user@email.com")
                        .build()
        );

        mockMvc.perform(get("/books/id/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Livro"));
    }

    // Deve atualizar os dados de um livro existente
    @Test
    void shouldUpdateBook() throws Exception {

        Book saved = repository.save(
                Book.builder()
                        .title("Old")
                        .author("Autor")
                        .description("Descricao")
                        .userEmail("user@email.com")
                        .build()
        );

        Book updated = Book.builder()
                .title("New Title")
                .author("Novo Autor")
                .description("Nova descricao")
                .userEmail("user@email.com")
                .build();

        mockMvc.perform(put("/books/id/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    // Deve excluir um livro e confirmar que o banco ficou vazio
    @Test
    void shouldDeleteBook() throws Exception {

        Book saved = repository.save(
                Book.builder()
                        .title("Livro")
                        .author("Autor")
                        .description("Descricao")
                        .userEmail("user@email.com")
                        .build()
        );

        mockMvc.perform(delete("/books/id/" + saved.getId()))
                .andExpect(status().isOk());

        // Verifica que o livro foi removido do banco
        List<Book> books = repository.findAll();

        assertTrue(books.isEmpty());
    }

    // Deve retornar erro ao tentar cadastrar um livro com campos vazios
    @Test
    void shouldReturn400WhenInvalidBook() throws Exception {

        Book book = new Book();

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest());
    }

    // Deve retornar erro ao buscar um livro com ID inexistente
    @Test
    void shouldReturn400WhenBookNotFound() throws Exception {

        mockMvc.perform(get("/books/id/123"))
                .andExpect(status().isBadRequest());
    }
}