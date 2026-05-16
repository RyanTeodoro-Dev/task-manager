package com.example.taskmanager.controller;

import org.junit.jupiter.api.BeforeEach;
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

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// Testes de integração do AuthController usando MongoDB real via Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerTest {

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
    private UserRepository userRepository;

    // Limpa o banco antes de cada teste para evitar dados duplicados
    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    // Deve cadastrar um usuário com sucesso
    @Test
    void shouldRegisterUser() throws Exception {

        RegisterRequest request = buildRegister("Ryan", "ryan@email.com", "123456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ryan@email.com"));
    }

    // Deve retornar erro ao tentar cadastrar um e-mail já existente
    @Test
    void shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {

        RegisterRequest request = buildRegister("Ryan", "duplicado@email.com", "123456");

        // Primeiro cadastro
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Segundo cadastro com o mesmo e-mail deve falhar
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    // Deve retornar erro ao enviar campos vazios no cadastro
    @Test
    void shouldReturnBadRequestWhenRegisterFieldsAreMissing() throws Exception {

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // Deve realizar login com sucesso após cadastro
    @Test
    void shouldLoginUser() throws Exception {

        RegisterRequest register = buildRegister("Ryan", "login@email.com", "123456");

        // Cadastra o usuário antes de fazer login
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = buildLogin("login@email.com", "123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ryan"))
                .andExpect(jsonPath("$.email").value("login@email.com"));
    }

    // Deve retornar erro ao tentar login com senha incorreta
    @Test
    void shouldReturnBadRequestWhenPasswordIsWrong() throws Exception {

        RegisterRequest register = buildRegister("Ryan", "senhaerrada@email.com", "123456");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest wrongLogin = buildLogin("senhaerrada@email.com", "senhaErrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isBadRequest());
    }

    // Deve retornar erro ao tentar login com usuário não cadastrado
    @Test
    void shouldReturnBadRequestWhenUserNotFound() throws Exception {

        LoginRequest login = buildLogin("naocadastrado@email.com", "qualquer");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }

    // Método auxiliar para criar um RegisterRequest
    private RegisterRequest buildRegister(String name, String email, String password) {
        RegisterRequest r = new RegisterRequest();
        r.setName(name);
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    // Método auxiliar para criar um LoginRequest
    private LoginRequest buildLogin(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }
}