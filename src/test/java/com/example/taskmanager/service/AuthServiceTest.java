package com.example.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.dto.UserResponseDTO;
import com.example.taskmanager.repository.UserRepository;

// Testes de integração do AuthService usando MongoDB real via Testcontainers
@SpringBootTest
@Testcontainers
class AuthServiceTest {

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
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // Limpa o banco antes de cada teste para evitar dados duplicados
    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    // Deve cadastrar um usuário com sucesso e retornar os dados corretos
    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = buildRegisterRequest(
                "Ryan", "ryan@email.com", "123456"
        );

        UserResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("Ryan", response.name());
        assertEquals("ryan@email.com", response.email());
    }

    // Deve salvar a senha criptografada (BCrypt começa com "$2")
    @Test
    void shouldHashPasswordOnRegister() {

        RegisterRequest request = buildRegisterRequest(
                "Ryan", "hash@email.com", "minha-senha"
        );

        authService.register(request);

        // Busca a senha armazenada no banco
        String storedPassword = userRepository
                .findByEmail("hash@email.com")
                .orElseThrow()
                .getPassword();

        // Verifica que a senha não foi salva em texto puro
        org.junit.jupiter.api.Assertions.assertNotEquals(
                "minha-senha", storedPassword
        );

        // Verifica que a senha foi criptografada com BCrypt
        org.junit.jupiter.api.Assertions.assertTrue(
                storedPassword.startsWith("$2")
        );
    }

    // Deve lançar exceção ao tentar cadastrar um e-mail já existente
    @Test
    void shouldThrowExceptionWhenEmailAlreadyRegistered() {

        RegisterRequest request = buildRegisterRequest(
                "Ryan", "duplicado@email.com", "123456"
        );

        authService.register(request);

        // Segunda tentativa com o mesmo e-mail deve lançar exceção
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals("E-mail já cadastrado", ex.getMessage());
    }

    // Deve realizar login com sucesso após cadastro
    @Test
    void shouldLoginSuccessfully() {

        RegisterRequest register = buildRegisterRequest(
                "Ryan", "login@email.com", "123456"
        );
        authService.register(register);

        UserResponseDTO response =
                authService.login("login@email.com", "123456");

        assertNotNull(response);
        assertEquals("login@email.com", response.email());
        assertEquals("Ryan", response.name());
    }

    // Deve lançar exceção ao tentar login com senha incorreta
    @Test
    void shouldThrowExceptionWhenPasswordIsWrong() {

        RegisterRequest register = buildRegisterRequest(
                "Ryan", "senhaerrada@email.com", "correta"
        );
        authService.register(register);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login("senhaerrada@email.com", "errada")
        );

        assertEquals("Invalid credentials", ex.getMessage());
    }

    // Deve lançar exceção ao tentar login com e-mail não cadastrado
    @Test
    void shouldThrowExceptionWhenEmailNotFound() {

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login("naoexiste@email.com", "qualquer")
        );

        assertEquals("Invalid credentials", ex.getMessage());
    }

    // Método auxiliar para criar um RegisterRequest
    private RegisterRequest buildRegisterRequest(
            String name, String email, String password) {

        RegisterRequest r = new RegisterRequest();
        r.setName(name);
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }
}