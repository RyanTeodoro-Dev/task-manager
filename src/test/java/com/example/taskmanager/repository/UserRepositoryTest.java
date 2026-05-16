package com.example.taskmanager.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.example.taskmanager.model.User;

// Testes de integração do UserRepository usando MongoDB real via Testcontainers
@DataMongoTest
@Testcontainers
public class UserRepositoryTest {

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
    private UserRepository repository;

    // Limpa o banco antes de cada teste para evitar dados duplicados
    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    // Deve salvar um usuário e verificar que o ID foi gerado
    @Test
    void shouldSaveUser() {

        User user = new User();

        user.setName("Ryan");
        user.setEmail("ryan@email.com");
        user.setPassword("123456");

        User saved = repository.save(user);

        // Verifica que o ID foi gerado automaticamente pelo MongoDB
        assertNotNull(saved.getId());

        // Verifica que o nome foi salvo corretamente
        assertEquals("Ryan", saved.getName());
    }

    // Deve encontrar um usuário pelo e-mail
    @Test
    void shouldFindUserByEmail() {

        User user = new User();

        user.setName("Ryan");
        user.setEmail("find@email.com");
        user.setPassword("123456");

        repository.save(user);

        Optional<User> found =
                repository.findByEmail("find@email.com");

        // Verifica que o usuário foi encontrado
        assert(found.isPresent());

        // Verifica que o e-mail retornado é o correto
        assertEquals(
                "find@email.com",
                found.get().getEmail()
        );
    }
}