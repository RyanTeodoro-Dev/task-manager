package com.example.taskmanager.controller;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.taskmanager.model.Book;
import com.example.taskmanager.repository.BookRepository;
import com.example.taskmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// Testes parametrizados dos controllers usando MongoDB real via Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ParameterizedControllerTest {

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
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    // Limpa o banco antes de cada teste para evitar dados duplicados
    @BeforeEach
    void cleanDatabase() {
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    // Deve cadastrar livros com diferentes títulos, autores e descrições válidos
    @ParameterizedTest(name = "[{index}] título={0}, autor={1}")
    @CsvSource({
            "Clean Code,       Robert Martin, Boas práticas de programação",
            "The Pragmatic Programmer, Hunt e Thomas, Dicas para devs",
            "Design Patterns,  Gang of Four,  Padrões de projeto clássicos",
            "Refactoring,      Martin Fowler,  Melhorando código legado"
    })
    void shouldCreateBookWithValidData(String title, String author, String description)
            throws Exception {

        Book book = new Book();
        book.setTitle(title.strip());
        book.setAuthor(author.strip());
        book.setDescription(description.strip());
        book.setUserEmail("user@email.com");

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title.strip()));
    }

    // Deve retornar erro 400 para cada corpo de livro com campo obrigatório faltando
    @ParameterizedTest(name = "[{index}] corpo inválido → 400")
    @MethodSource("invalidBookBodies")
    void shouldReturn400WhenBookFieldIsMissing(String jsonBody) throws Exception {

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    // Fornece diferentes corpos JSON com campos obrigatórios ausentes
    static Stream<String> invalidBookBodies() {
        return Stream.of(
                // Sem título
                """
                {"author":"Autor","description":"Desc","userEmail":"u@e.com"}
                """,
                // Sem autor
                """
                {"title":"Titulo","description":"Desc","userEmail":"u@e.com"}
                """,
                // Sem descrição
                """
                {"title":"Titulo","author":"Autor","userEmail":"u@e.com"}
                """,
                // Sem e-mail
                """
                {"title":"Titulo","author":"Autor","description":"Desc"}
                """,
                // Corpo completamente vazio
                "{}"
        );
    }

    // Deve retornar apenas os livros do usuário informado, ignorando os de outros usuários
    @ParameterizedTest(name = "[{index}] usuário={0}")
    @ValueSource(strings = {
            "alice@email.com",
            "bob@email.com",
            "carol@email.com"
    })
    void shouldReturnBooksOnlyForGivenUser(String email) throws Exception {

        // Salva um livro para o usuário do teste
        Book book = Book.builder()
                .title("Livro de " + email)
                .author("Autor")
                .description("Desc")
                .userEmail(email)
                .build();
        bookRepository.save(book);

        // Salva um livro de outro usuário para garantir o isolamento
        bookRepository.save(Book.builder()
                .title("Livro de outro")
                .author("Outro Autor")
                .description("Outra Desc")
                .userEmail("outro@email.com")
                .build());

        // Verifica que apenas o livro do usuário correto é retornado
        mockMvc.perform(get("/books/user/" + email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value(email));
    }

    // Deve retornar erro 400 para diferentes formatos de e-mail inválidos no cadastro
    @ParameterizedTest(name = "[{index}] e-mail inválido={0}")
    @ValueSource(strings = {
            "naoéumemail",
            "sem-arroba.com",
            "@semlocal.com",
            "duplo@@email.com",
            ""
    })
    void shouldReturn400WhenEmailIsInvalid(String email) throws Exception {

        String body = String.format(
                "{\"name\":\"User\",\"email\":\"%s\",\"password\":\"123456\"}",
                email
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // Deve aceitar senhas de diferentes tamanhos no cadastro
    @ParameterizedTest(name = "[{index}] tamanho da senha={0} chars")
    @CsvSource({
            "a,         1",
            "ab12cd,    6",
            "abcdefghij,10"
    })
    void shouldAcceptPasswordsOfVariousLengths(String password, int length)
            throws Exception {

        // Gera um e-mail único para cada tamanho de senha testado
        String email = "user" + length + "@email.com";
        String body = String.format(
                "{\"name\":\"User\",\"email\":\"%s\",\"password\":\"%s\"}",
                email, password.strip()
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}