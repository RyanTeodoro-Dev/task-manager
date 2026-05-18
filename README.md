# Biblioteca Pessoal — Task Manager

Sistema completo para gerenciamento de uma biblioteca pessoal, com autenticação de usuários, CRUD de livros, enriquecimento de dados via Open Library e interface web responsiva.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Testes](#testes)
- [Cobertura de Código](#cobertura-de-código)
- [CI/CD e Qualidade](#cicd-e-qualidade)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Frontend](#frontend)

---

## Visão Geral

A aplicação permite que usuários criem contas, façam login e gerenciem sua coleção de livros pessoal. Cada livro pertence a um usuário identificado por e-mail. O sistema integra com a **Open Library API** para enriquecer os dados de um livro a partir do ISBN. Todas as operações são expostas via API REST e consumidas por um frontend estático.

---

## Tecnologias

### Backend
| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.14 | Framework web |
| Spring Data MongoDB | — | Persistência |
| Spring Security | — | Autenticação e BCrypt |
| Spring Validation | — | Validação de entrada |
| Lombok | — | Redução de boilerplate |
| JaCoCo | 0.8.11 | Cobertura de testes |

### Testes
| Tecnologia | Uso |
|---|---|
| JUnit 5 | Framework de testes |
| Testcontainers (MongoDB) | Persistência real em testes, sem mocks |
| WireMock | VCR — gravação e replay de chamadas HTTP externas |
| Spring MockMvc | Testes de controller (caixa-preta / E2E) |

### Frontend
| Tecnologia | Uso |
|---|---|
| HTML5 / CSS3 | Estrutura e estilo |
| JavaScript (Vanilla) | Lógica de sessão e chamadas à API |

### Infraestrutura
| Tecnologia | Uso |
|---|---|
| MongoDB | Banco de dados NoSQL |
| Docker | Container para MongoDB em testes (Testcontainers) |
| GitHub Actions | Pipeline de CI |
| SonarCloud | Análise de qualidade de código |

---

## Arquitetura

O projeto segue a arquitetura **MVC (Model-View-Controller)**:

```
frontend/               ← View (HTML/CSS/JS)
    ├── index.html
    ├── login.html
    ├── register.html
    └── assets/

src/main/java/.../
    ├── controller/     ← Controller (recebe requisições HTTP)
    │   ├── AuthController.java
    │   ├── BookController.java
    │   └── BookEnrichmentController.java
    ├── service/        ← Service (regras de negócio)
    │   ├── AuthService.java
    │   ├── BookService.java
    │   └── BookEnrichmentService.java
    ├── client/         ← Client (integração com API externa)
    │   ├── OpenLibraryClient.java
    │   └── OpenLibraryResponse.java
    ├── repository/     ← Repository (acesso ao MongoDB)
    │   ├── UserRepository.java
    │   └── BookRepository.java
    ├── model/          ← Model (entidades)
    │   ├── User.java
    │   └── Book.java
    ├── dto/            ← Data Transfer Objects
    │   ├── LoginRequest.java
    │   ├── RegisterRequest.java
    │   └── UserResponseDTO.java
    ├── exception/      ← Tratamento global de erros
    │   └── GlobalExceptionHandler.java
    └── config/         ← Configurações
        ├── SecurityConfig.java
        └── RestTemplateConfig.java
```

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (necessário para rodar os testes com Testcontainers)

---

## Como executar

### 1. Clonar o repositório

```bash
git clone https://github.com/RyanTeodoro-Dev/task-manager.git
cd task-manager
```

### 2. Subir o MongoDB com Docker

```bash
docker run -d -p 27017:27017 --name mongodb mongo:6
```

### 3. Executar o backend

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

O servidor sobe em `http://localhost:8080`.

### 4. Abrir o frontend

Abra a pasta `frontend/` no VS Code e clique com o botão direito em `login.html` → **Open with Live Server**.

> O backend precisa estar rodando em `http://localhost:8080` antes de abrir o frontend.

---

## Endpoints da API

### Autenticação — `/auth`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Cadastra novo usuário |
| `POST` | `/auth/login` | Autentica usuário |

**Exemplo de registro:**
```json
POST /auth/register
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "minhasenha"
}
```

**Resposta (200):**
```json
{
  "id": "6647a1b2c3d4e5f6a7b8c9d0",
  "name": "João Silva",
  "email": "joao@email.com"
}
```

---

### Livros — `/books`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/books` | Cria um livro |
| `GET` | `/books/user/{email}` | Lista livros do usuário |
| `GET` | `/books/id/{id}` | Busca livro por ID |
| `PUT` | `/books/id/{id}` | Atualiza livro |
| `DELETE` | `/books/id/{id}` | Remove livro |

**Exemplo de criação:**
```json
POST /books
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "description": "Boas práticas de programação",
  "userEmail": "joao@email.com"
}
```

**Resposta (201):**
```json
{
  "id": "6647a1b2c3d4e5f6a7b8c9d1",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "description": "Boas práticas de programação",
  "userEmail": "joao@email.com"
}
```

---

### Enriquecimento por ISBN — `/books/isbn`

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/books/isbn/{isbn}` | Busca metadados de um livro na Open Library pelo ISBN |

**Exemplo:**
```
GET /books/isbn/9780132350884
```

**Resposta (200):**
```json
{
  "found": true,
  "isbn": "9780132350884",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "publisher": "Prentice Hall",
  "pages": 431
}
```

---

### Códigos de resposta

| Código | Situação |
|---|---|
| `200 OK` | Sucesso (GET, login, register) |
| `201 Created` | Livro criado com sucesso |
| `400 Bad Request` | Erro de validação ou regra de negócio |

---

## Testes

O projeto **proíbe o uso de Mocks**. Todos os testes utilizam **Testcontainers** para levantar um MongoDB real em Docker. As chamadas à API externa (Open Library) são interceptadas pelo **WireMock** (VCR), garantindo testes determinísticos sem acesso à internet.

### Executar todos os testes e verificar cobertura

**Windows:**
```powershell
.\mvnw.cmd verify
```

**Linux/Mac:**
```bash
./mvnw verify
```

### Executar apenas os testes (sem gate de cobertura)

**Windows:**
```powershell
.\mvnw.cmd test
```

**Linux/Mac:**
```bash
./mvnw test
```

### Tipos de testes implementados

| Tipo | Classe | Descrição |
|---|---|---|
| Integração / Caixa-Preta (E2E) | `AuthControllerTest` | Endpoints de autenticação via HTTP |
| Integração / Caixa-Preta (E2E) | `BookControllerTest` | Endpoints de livros via HTTP |
| Parametrizados | `ParameterizedControllerTest` | Múltiplos cenários com `@CsvSource`, `@ValueSource`, `@MethodSource` |
| Unitário / Caixa-Branca | `AuthServiceTest` | Lógica de negócio de autenticação |
| Unitário / Caixa-Branca | `BookServiceTest` | Lógica de negócio de livros |
| Repositório | `BookRepositoryTest` | Persistência e consulta de livros |
| Repositório | `UserRepositoryTest` | Persistência e consulta de usuários |
| Handler de exceções | `GlobalExceptionHandlerTest` | Tratamento global de erros HTTP |
| VCR (WireMock) | `OpenLibraryVcrTest` | Integração com Open Library usando cassetes gravados |
| Contexto | `TaskManagerApplicationTests` | Carregamento do contexto Spring |

### Estratégia VCR

Os testes de integração com a Open Library usam **WireMock** como servidor de VCR. Os cassetes (respostas gravadas) ficam em `src/test/resources/wiremock/` e cobrem os seguintes cenários:

| Cassete | Cenário |
|---|---|
| `openlibrary-isbn-9780132350884.json` | ISBN válido — retorna metadados completos |
| `openlibrary-isbn-not-found.json` | ISBN inexistente — retorna `{}` |
| `openlibrary-isbn-error.json` | API retorna erro 500 |
| `openlibrary-isbn-chave-diferente.json` | Resposta sem a chave do ISBN |
| `openlibrary-isbn-sem-autores.json` | Livro sem autores/editoras no JSON |
| `openlibrary-isbn-autores-vazios.json` | Listas de autores/editoras vazias |
| `openlibrary-isbn-resposta-vazia.json` | Corpo da resposta em branco |

Nenhuma chamada real à internet é feita durante os testes.

---

## Cobertura de Código

O projeto usa **JaCoCo** com mínimo de **80%** em instruções e branches. O build falha automaticamente se cair abaixo desse limite.

### Resultado atual

| Métrica | Cobertura |
|---|---|
| Instructions | 98% |
| Branches | 95% |

### Visualizar o relatório

Após rodar `verify`, abra:
```
target/site/jacoco/index.html
```

As seguintes classes são excluídas da contagem por não conterem lógica de negócio:
- `TaskManagerApplication`
- Classes em `dto/`
- Classes em `model/`
- `SecurityConfig`
- `RestTemplateConfig`

---

## CI/CD e Qualidade

### GitHub Actions

O pipeline executa automaticamente em todo push e pull request para `main`:

1. Checkout do código
2. Setup do Java 21
3. Cache do Maven
4. Execução de todos os testes com Testcontainers + WireMock
5. Geração e publicação do relatório JaCoCo
6. Análise com SonarCloud

Arquivo de configuração: `.github/workflows/ci.yml`

### SonarCloud

Métricas monitoradas automaticamente:
- Cobertura de código (mínimo 80%)
- Bugs e vulnerabilidades
- Code smells
- Duplicações

---

## Estrutura do Projeto

```
task-manager/
├── .github/
│   └── workflows/
│       └── ci.yml                        ← Pipeline de CI
├── frontend/
│   ├── index.html                        ← Página principal (livros)
│   ├── login.html                        ← Página de login
│   ├── register.html                     ← Página de cadastro
│   └── assets/
│       ├── css/
│       │   ├── index.css
│       │   ├── login.css
│       │   └── register.css
│       └── js/
│           ├── auth.js                   ← Lógica de login/registro
│           └── script.js                 ← Lógica de livros
├── src/
│   ├── main/java/com/example/taskmanager/
│   │   ├── client/
│   │   │   ├── OpenLibraryClient.java    ← Integração Open Library
│   │   │   └── OpenLibraryResponse.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── RestTemplateConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── BookController.java
│   │   │   └── BookEnrichmentController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── BookService.java
│   │   │   └── BookEnrichmentService.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── BookRepository.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   └── Book.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── UserResponseDTO.java
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   └── TaskManagerApplication.java
│   ├── main/resources/
│   │   └── application.properties
│   └── test/java/com/example/taskmanager/
│       ├── controller/
│       │   ├── AuthControllerTest.java
│       │   ├── BookControllerTest.java
│       │   └── ParameterizedControllerTest.java
│       ├── service/
│       │   ├── AuthServiceTest.java
│       │   └── BookServiceTest.java
│       ├── repository/
│       │   ├── BookRepositoryTest.java
│       │   └── UserRepositoryTest.java
│       ├── exception/
│       │   └── GlobalExceptionHandlerTest.java
│       ├── vcr/
│       │   └── OpenLibraryVcrTest.java
│       └── TaskManagerApplicationTests.java
├── src/test/resources/wiremock/
│   ├── mappings/                         ← Cassetes VCR (mapeamentos)
│   └── __files/                          ← Cassetes VCR (corpos de resposta)
├── pom.xml
└── README.md
```

---

## Frontend

O frontend é uma aplicação estática que consome a API REST. O gerenciamento de sessão é feito via `sessionStorage`:

- Após o login, os dados do usuário (id, nome, e-mail) são armazenados em `sessionStorage`
- A página principal (`index.html`) verifica a sessão ao carregar e redireciona para `login.html` se não houver sessão ativa
- O botão "Sair" limpa o `sessionStorage` e redireciona para o login

### Fluxo de navegação

```
login.html ──(sucesso)──► index.html ──(logout)──► login.html
register.html ──(sucesso)──► login.html
```