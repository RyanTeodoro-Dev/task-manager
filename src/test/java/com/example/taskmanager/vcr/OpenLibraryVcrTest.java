package com.example.taskmanager.vcr;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

// Testes VCR (cassetes gravados) para a Open Library usando WireMock e Testcontainers
// O WireMock intercepta as chamadas HTTP e devolve respostas gravadas, sem acessar a internet
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OpenLibraryVcrTest {

    // Sobe um container MongoDB real para os testes
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6");

    // Servidor WireMock que simula a Open Library API
    static WireMockServer wireMockServer;

    // Inicia o WireMock antes de todos os testes, carregando os cassetes da pasta wiremock/
    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort()
                        .usingFilesUnderClasspath("wiremock")
        );
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    // Para o WireMock após todos os testes
    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    // Limpa o histórico de requisições após cada teste
    @AfterEach
    void resetCassettes() {
        wireMockServer.resetRequests();
    }

    // Aponta o MongoDB e a Open Library para os servidores locais dos testes
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("openlibrary.base-url",
                () -> "http://localhost:" + wireMockServer.port());
    }

    @Autowired
    private MockMvc mockMvc;

    // Cassete: openlibrary-isbn-9780132350884.json
    // Deve retornar os metadados completos do livro quando o ISBN é válido
    @Test
    @DisplayName("VCR: deve retornar metadados completos para ISBN válido (Clean Code)")
    void shouldReturnRecordedMetadataForValidIsbn() throws Exception {
        mockMvc.perform(get("/books/isbn/9780132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.publisher").value("Prentice Hall"))
                .andExpect(jsonPath("$.pages").value(431))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));

        // Verifica que a chamada ao WireMock foi feita com os parâmetros corretos
        verify(1, getRequestedFor(urlPathEqualTo("/api/books"))
                .withQueryParam("bibkeys", equalTo("ISBN:9780132350884")));
    }

    // Cassete: openlibrary-isbn-not-found.json
    // Deve retornar found=false quando a API retorna {} (ISBN inexistente)
    @Test
    @DisplayName("VCR: deve retornar found=false para ISBN inexistente")
    void shouldReturnNotFoundForUnknownIsbn() throws Exception {
        mockMvc.perform(get("/books/isbn/0000000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.isbn").value("0000000000000"))
                .andExpect(jsonPath("$.message").value("Livro não encontrado na Open Library"));

        // Verifica que a chamada ao WireMock foi feita com os parâmetros corretos
        verify(1, getRequestedFor(urlPathEqualTo("/api/books"))
                .withQueryParam("bibkeys", equalTo("ISBN:0000000000000")));
    }

    // Cassete: openlibrary-isbn-error.json
    // Deve retornar found=false quando a API retorna erro 500
    @Test
    @DisplayName("VCR: deve retornar found=false quando a API retorna erro 500")
    void shouldReturnNotFoundWhenApiReturnsError() throws Exception {
        mockMvc.perform(get("/books/isbn/ERROR_ISBN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.isbn").value("ERROR_ISBN"))
                .andExpect(jsonPath("$.message").value("Livro não encontrado na Open Library"));
    }

    // Cassete: openlibrary-isbn-chave-diferente.json
    // Deve retornar found=false quando a resposta não contém a chave do ISBN solicitado
    @Test
    @DisplayName("VCR: deve retornar found=false quando resposta não contém a chave do ISBN")
    void shouldReturnNotFoundWhenResponseKeyIsMissing() throws Exception {
        mockMvc.perform(get("/books/isbn/CHAVE_DIFERENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.isbn").value("CHAVE_DIFERENTE"))
                .andExpect(jsonPath("$.message").value("Livro não encontrado na Open Library"));
    }

    // Cassete: openlibrary-isbn-sem-autores.json
    // Deve retornar found=true mas sem os campos author e publisher quando ausentes no cassete
    @Test
    @DisplayName("VCR: deve retornar found=true sem author e publisher quando ausentes no cassete")
    void shouldReturnBookWithoutAuthorsAndPublishers() throws Exception {
        mockMvc.perform(get("/books/isbn/SEM_AUTORES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.title").value("Livro Sem Autores"))
                .andExpect(jsonPath("$.isbn").value("SEM_AUTORES"))
                .andExpect(jsonPath("$.author").doesNotExist())
                .andExpect(jsonPath("$.publisher").doesNotExist());
    }

    // Cassete: openlibrary-isbn-autores-vazios.json
    // Deve retornar found=true mas sem author e publisher quando as listas existem mas estão vazias
    @Test
    @DisplayName("VCR: deve retornar found=true sem author e publisher quando listas são vazias")
    void shouldReturnBookWithEmptyAuthorsAndPublishersList() throws Exception {
        mockMvc.perform(get("/books/isbn/AUTORES_VAZIOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.title").value("Livro Com Listas Vazias"))
                .andExpect(jsonPath("$.isbn").value("AUTORES_VAZIOS"))
                .andExpect(jsonPath("$.author").doesNotExist())
                .andExpect(jsonPath("$.publisher").doesNotExist());
    }

    // Cassete: openlibrary-isbn-resposta-vazia.json
    // Deve retornar found=false quando a API retorna corpo em branco (isBlank)
    @Test
    @DisplayName("VCR: deve retornar found=false quando a API retorna corpo em branco")
    void shouldReturnNotFoundWhenResponseBodyIsBlank() throws Exception {
        mockMvc.perform(get("/books/isbn/RESPOSTA_VAZIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.isbn").value("RESPOSTA_VAZIA"))
                .andExpect(jsonPath("$.message").value("Livro não encontrado na Open Library"));
    }

    // Garante que nenhuma chamada fora dos cassetes mapeados foi feita durante os testes
    @Test
    @DisplayName("VCR: nenhuma chamada não mapeada deve ser feita à API externa")
    void shouldNotMakeUnmappedCallsToExternalApi() throws Exception {
        mockMvc.perform(get("/books/isbn/9780132350884"))
                .andExpect(status().isOk());

        // Verifica que todas as chamadas foram cobertas pelos cassetes VCR
        Assertions.assertEquals(0, wireMockServer.findUnmatchedRequests().getRequests().size(),
                "Existem chamadas HTTP não cobertas pelos cassetes VCR!");
    }
}