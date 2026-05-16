package com.example.taskmanager.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.taskmanager.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;

// Testes de integração do GlobalExceptionHandler usando MongoDB real via Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GlobalExceptionHandlerTest {

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

    // Deve retornar erros de validação para cada campo obrigatório do livro ausente
    @Test
    void shouldReturnValidationErrorWhenBookFieldsAreMissing() throws Exception {

        // Livro sem nenhum campo preenchido
        Book book = new Book();

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").exists())           // Resposta não vazia
                .andExpect(jsonPath("$.title").exists())     // Erro no campo título
                .andExpect(jsonPath("$.author").exists())    // Erro no campo autor
                .andExpect(jsonPath("$.description").exists()); // Erro no campo descrição
    }

    // Deve retornar uma mensagem de erro ao lançar uma RuntimeException
    @Test
    void shouldReturnMessageOnRuntimeException() throws Exception {

        // Tenta fazer login com usuário inexistente para forçar a exceção
        String body = """
                {"email":"naoexiste@email.com","password":"errada"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists()); // Verifica que a mensagem de erro foi retornada
    }
}