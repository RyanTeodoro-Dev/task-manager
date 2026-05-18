# RTM — Matriz de Rastreabilidade de Requisitos
## Biblioteca Pessoal — Task Manager

> **Versão:** 1.0
> **Projeto:** Gerenciador de Biblioteca Pessoal
> **Prazo:** Semana de 18/05

---

## Sumário

- [Visão Geral da Cobertura](#visão-geral-da-cobertura)
- [RF-01 — Cadastro de Usuário](#rf-01--cadastro-de-usuário)
- [RF-02 — Login de Usuário](#rf-02--login-de-usuário)
- [RF-03 — Criar Livro](#rf-03--criar-livro)
- [RF-04 — Listar Livros do Usuário](#rf-04--listar-livros-do-usuário)
- [RF-05 — Buscar Livro por ID](#rf-05--buscar-livro-por-id)
- [RF-06 — Atualizar Livro](#rf-06--atualizar-livro)
- [RF-07 — Remover Livro](#rf-07--remover-livro)
- [RF-08 — Validação de Campos Obrigatórios](#rf-08--validação-de-campos-obrigatórios)
- [RF-09 — Enriquecimento por ISBN (Open Library / VCR)](#rf-09--enriquecimento-por-isbn-open-library--vcr)
- [RF-10 — Gerenciamento de Sessão no Frontend](#rf-10--gerenciamento-de-sessão-no-frontend)
- [Tabela Consolidada RTM](#tabela-consolidada-rtm)

---

## Visão Geral da Cobertura

| Requisito | Descrição | Testes | Cobertura |
|---|---|---|---|
| RF-01 | Cadastro de usuário | 8 | ✅ 100% |
| RF-02 | Login de usuário | 5 | ✅ 100% |
| RF-03 | Criar livro | 6 | ✅ 100% |
| RF-04 | Listar livros do usuário | 4 | ✅ 100% |
| RF-05 | Buscar livro por ID | 3 | ✅ 100% |
| RF-06 | Atualizar livro | 2 | ✅ 100% |
| RF-07 | Remover livro | 2 | ✅ 100% |
| RF-08 | Validação de campos obrigatórios | 8 | ✅ 100% |
| RF-09 | Enriquecimento por ISBN (VCR) | 8 | ✅ 100% |
| RF-10 | Gerenciamento de sessão (frontend) | — | ✅ Manual |
| **Total** | | **46** | **✅ 100%** |

---

## RF-01 — Cadastro de Usuário

**Descrição:** O sistema deve permitir que um novo usuário se cadastre informando nome, e-mail e senha. O e-mail deve ser único. A senha deve ser armazenada com hash BCrypt.

**Regras de negócio:**
- Nome, e-mail e senha são obrigatórios
- O e-mail deve ter formato válido
- Não pode existir outro usuário com o mesmo e-mail
- A senha nunca é armazenada em texto plano

### Diagrama UML de Sequência — RF-01 (Fluxo de sucesso)

```
Cliente          AuthController       AuthService        UserRepository      MongoDB
   |                   |                   |                   |                |
   |  POST /auth/      |                   |                   |                |
   |  register         |                   |                   |                |
   |  {name,email,pw}  |                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   | register(request) |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   | findByEmail(email)|                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.users.      |
   |                   |                   |                   | findOne(email) |
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   | Optional.empty()  |                |
   |                   |                   |◄──────────────────|                |
   |                   |                   |                   |                |
   |                   |                   | BCrypt.encode(pw) |                |
   |                   |                   |───(interno)──────►|                |
   |                   |                   |                   |                |
   |                   |                   |    save(user)     |                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.users.      |
   |                   |                   |                   | insertOne(user)|
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   | User(id,name,     |                |
   |                   |                   | email)            |                |
   |                   |                   |◄──────────────────|                |
   |                   | UserResponseDTO   |                   |                |
   |                   |◄──────────────────|                   |                |
   |  200 OK           |                   |                   |                |
   |  {id, name, email}|                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Diagrama UML de Sequência — RF-01 (E-mail duplicado)

```
Cliente          AuthController       AuthService        UserRepository
   |                   |                   |                   |
   |  POST /auth/register                  |                   |
   |  {email já existente}                 |                   |
   |──────────────────►|                   |                   |
   |                   | register(request) |                   |
   |                   |──────────────────►|                   |
   |                   |                   | findByEmail(email)|
   |                   |                   |──────────────────►|
   |                   |                   | Optional(user)    |
   |                   |                   |◄──────────────────|
   |                   |                   |                   |
   |                   |                   | throw             |
   |                   |                   | RuntimeException  |
   |                   |                   | ("E-mail já       |
   |                   |                   |  cadastrado")     |
   |                   |                   |                   |
   |                   | GlobalExceptionHandler.handleRuntime()|
   |  400 Bad Request  |                   |                   |
   |  {"message":"E-mail já cadastrado"}   |                   |
   |◄──────────────────|                   |                   |
```

### Testes cobrindo RF-01

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-01.1 | `AuthControllerTest` | `shouldRegisterUser` | Caixa-preta / E2E | Cadastro com dados válidos → 200 |
| T-01.2 | `AuthControllerTest` | `shouldReturnBadRequestWhenEmailAlreadyExists` | Caixa-preta / E2E | E-mail duplicado → 400 |
| T-01.3 | `AuthControllerTest` | `shouldReturnBadRequestWhenRegisterFieldsAreMissing` | Caixa-preta / E2E | Body vazio → 400 |
| T-01.4 | `AuthServiceTest` | `shouldRegisterUserSuccessfully` | Unitário (service) | Serviço retorna DTO correto |
| T-01.5 | `AuthServiceTest` | `shouldHashPasswordOnRegister` | Unitário / Caixa-branca | Senha armazenada com BCrypt ($2) |
| T-01.6 | `AuthServiceTest` | `shouldThrowExceptionWhenEmailAlreadyRegistered` | Unitário / Caixa-branca | Exceção com mensagem correta |
| T-01.7 | `ParameterizedControllerTest` | `shouldReturn400WhenEmailIsInvalid` | Parametrizado | 5 e-mails inválidos → 400 |
| T-01.8 | `ParameterizedControllerTest` | `shouldAcceptPasswordsOfVariousLengths` | Parametrizado | 3 tamanhos de senha → 200 |

---

## RF-02 — Login de Usuário

**Descrição:** O sistema deve autenticar um usuário a partir de e-mail e senha. A senha informada deve corresponder ao hash BCrypt armazenado.

**Regras de negócio:**
- E-mail e senha são obrigatórios
- Se o e-mail não existir, retornar erro
- Se a senha não corresponder ao hash, retornar erro
- Em caso de sucesso, retornar os dados públicos do usuário (sem senha)

### Diagrama UML de Sequência — RF-02 (Fluxo de sucesso)

```
Cliente          AuthController       AuthService        UserRepository      BCrypt
   |                   |                   |                   |                |
   |  POST /auth/login |                   |                   |                |
   |  {email, password}|                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   | login(email, pw)  |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   | findByEmail(email)|                |
   |                   |                   |──────────────────►|                |
   |                   |                   | Optional(user)    |                |
   |                   |                   |◄──────────────────|                |
   |                   |                   |                   |                |
   |                   |                   | matches(rawPw,    |                |
   |                   |                   |  hashedPw)        |                |
   |                   |                   |──────────────────────────────────►|
   |                   |                   |                   |      true      |
   |                   |                   |◄──────────────────────────────────|
   |                   | UserResponseDTO   |                   |                |
   |                   |◄──────────────────|                   |                |
   |  200 OK           |                   |                   |                |
   |  {id, name, email}|                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Diagrama UML de Sequência — RF-02 (Credenciais inválidas)

```
Cliente          AuthController       AuthService        UserRepository
   |                   |                   |                   |
   |  POST /auth/login |                   |                   |
   |  {email, senhaErrada}                 |                   |
   |──────────────────►|                   |                   |
   |                   | login(email, pw)  |                   |
   |                   |──────────────────►|                   |
   |                   |                   | findByEmail       |
   |                   |                   | .filter(          |
   |                   |                   |  matches = false) |
   |                   |                   | .orElseThrow()    |
   |                   |                   |                   |
   |                   |                   | throw RuntimeException
   |                   |                   | ("Invalid credentials")
   |                   |                   |                   |
   |                   | GlobalExceptionHandler.handleRuntime()|
   |  400 Bad Request  |                   |                   |
   |  {"message":"Invalid credentials"}    |                   |
   |◄──────────────────|                   |                   |
```

### Testes cobrindo RF-02

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-02.1 | `AuthControllerTest` | `shouldLoginUser` | Caixa-preta / E2E | Login com credenciais válidas → 200 |
| T-02.2 | `AuthControllerTest` | `shouldReturnBadRequestWhenPasswordIsWrong` | Caixa-preta / E2E | Senha errada → 400 |
| T-02.3 | `AuthControllerTest` | `shouldReturnBadRequestWhenUserNotFound` | Caixa-preta / E2E | E-mail não cadastrado → 400 |
| T-02.4 | `AuthServiceTest` | `shouldLoginSuccessfully` | Unitário (service) | Serviço retorna DTO correto |
| T-02.5 | `AuthServiceTest` | `shouldThrowExceptionWhenPasswordIsWrong` | Unitário / Caixa-branca | Exceção ao senha errada |
| T-02.6 | `AuthServiceTest` | `shouldThrowExceptionWhenEmailNotFound` | Unitário / Caixa-branca | Exceção ao e-mail não encontrado |

---

## RF-03 — Criar Livro

**Descrição:** O sistema deve permitir o cadastro de um livro com título, autor, descrição e e-mail do usuário.

**Regras de negócio:**
- Título, autor, descrição e e-mail do usuário são obrigatórios
- O livro deve ser associado ao e-mail do usuário dono

### Diagrama UML de Sequência — RF-03 (Fluxo de sucesso)

```
Cliente          BookController       BookService        BookRepository      MongoDB
   |                   |                   |                   |                |
   |  POST /books      |                   |                   |                |
   |  {title,author,   |                   |                   |                |
   |   desc,userEmail} |                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   |   create(book)    |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   |   save(book)      |                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.books.      |
   |                   |                   |                   | insertOne(book)|
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   | Book(id, ...)     |                |
   |                   |                   |◄──────────────────|                |
   |                   | Book salvo        |                   |                |
   |                   |◄──────────────────|                   |                |
   |  201 Created      |                   |                   |                |
   |  {id, title, ...} |                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Testes cobrindo RF-03

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-03.1 | `BookControllerTest` | `shouldCreateBook` | Caixa-preta / E2E | Criação com dados válidos → 201 |
| T-03.2 | `BookServiceTest` | `shouldCreateBook` | Unitário (service) | Serviço retorna livro com ID gerado |
| T-03.3 | `BookRepositoryTest` | `shouldSaveBook` | Repositório | Persistência e geração de ID |
| T-03.4 | `ParameterizedControllerTest` | `shouldCreateBookWithValidData` | Parametrizado | 4 combinações de título/autor → 201 |
| T-03.5 | `ParameterizedControllerTest` | `shouldReturn400WhenBookFieldIsMissing` | Parametrizado | 5 combinações de campos ausentes → 400 |
| T-03.6 | `GlobalExceptionHandlerTest` | `shouldReturnValidationErrorWhenBookFieldsAreMissing` | Caixa-preta | Erros de validação por campo → 400 |

---

## RF-04 — Listar Livros do Usuário

**Descrição:** O sistema deve retornar todos os livros associados a um e-mail de usuário.

**Regras de negócio:**
- A listagem deve filtrar apenas os livros do usuário informado
- Livros de outros usuários não devem aparecer

### Diagrama UML de Sequência — RF-04

```
Cliente          BookController       BookService        BookRepository      MongoDB
   |                   |                   |                   |                |
   |  GET /books/      |                   |                   |                |
   |  user/{email}     |                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   | findAll(email)    |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   | findByUserEmail   |                |
   |                   |                   |  (email)          |                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.books.find  |
   |                   |                   |                   | ({userEmail})  |
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   | List<Book>        |                |
   |                   |                   |◄──────────────────|                |
   |                   | List<Book>        |                   |                |
   |                   |◄──────────────────|                   |                |
   |  200 OK           |                   |                   |                |
   |  [{id,title,...}] |                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Testes cobrindo RF-04

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-04.1 | `BookControllerTest` | `shouldFindBooksByUser` | Caixa-preta / E2E | Retorna livros do usuário → 200 |
| T-04.2 | `BookServiceTest` | `shouldFindBooksByUserEmail` | Unitário (service) | Lista não vazia para usuário com livros |
| T-04.3 | `BookRepositoryTest` | `shouldFindBooksByUserEmail` | Repositório | Query por e-mail no MongoDB |
| T-04.4 | `ParameterizedControllerTest` | `shouldReturnBooksOnlyForGivenUser` | Parametrizado | 3 usuários — isolamento correto |

---

## RF-05 — Buscar Livro por ID

**Descrição:** O sistema deve retornar um livro específico pelo seu ID.

**Regras de negócio:**
- Se o ID não existir, retornar erro 400

### Diagrama UML de Sequência — RF-05

```
Cliente          BookController       BookService        BookRepository      MongoDB
   |                   |                   |                   |                |
   |  GET /books/      |                   |                   |                |
   |  id/{id}          |                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   | findById(id)      |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   | findById(id)      |                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.books.      |
   |                   |                   |                   | findById(id)   |
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   | Optional(book)    |                |
   |                   |                   |◄──────────────────|                |
   |                   |                   | .orElseThrow()    |                |
   |                   | Book              |                   |                |
   |                   |◄──────────────────|                   |                |
   |  200 OK           |                   |                   |                |
   |  {id, title, ...} |                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Testes cobrindo RF-05

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-05.1 | `BookControllerTest` | `shouldFindBookById` | Caixa-preta / E2E | ID válido → 200 com dados |
| T-05.2 | `BookControllerTest` | `shouldReturn400WhenBookNotFound` | Caixa-preta / E2E | ID inexistente → 400 |
| T-05.3 | `BookServiceTest` | `shouldFindBookById` | Unitário (service) | Retorna livro correto |
| T-05.4 | `BookServiceTest` | `shouldThrowExceptionWhenBookNotFound` | Unitário / Caixa-branca | Exceção "Book not found" |

---

## RF-06 — Atualizar Livro

**Descrição:** O sistema deve permitir a atualização dos dados de um livro existente.

**Regras de negócio:**
- O livro deve existir; caso contrário, retornar erro
- Apenas título, autor e descrição podem ser atualizados

### Diagrama UML de Sequência — RF-06

```
Cliente          BookController       BookService        BookRepository      MongoDB
   |                   |                   |                   |                |
   |  PUT /books/      |                   |                   |                |
   |  id/{id}          |                   |                   |                |
   |  {title,author,   |                   |                   |                |
   |   desc}           |                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   | update(id, book)  |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   | findById(id)      |                |
   |                   |                   |──────────────────►|                |
   |                   |                   | Book existente    |                |
   |                   |                   |◄──────────────────|                |
   |                   |                   | existing.setTitle |                |
   |                   |                   | existing.setAuthor|                |
   |                   |                   | existing.setDesc  |                |
   |                   |                   |                   |                |
   |                   |                   | save(existing)    |                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.books.      |
   |                   |                   |                   | replaceOne(id) |
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   | Book atualizado   |                |
   |                   |                   |◄──────────────────|                |
   |                   | Book atualizado   |                   |                |
   |                   |◄──────────────────|                   |                |
   |  200 OK           |                   |                   |                |
   |  {id, title, ...} |                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Testes cobrindo RF-06

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-06.1 | `BookControllerTest` | `shouldUpdateBook` | Caixa-preta / E2E | Atualização com dados válidos → 200 |
| T-06.2 | `BookServiceTest` | `shouldUpdateBook` | Unitário (service) | Campos atualizados corretamente |

---

## RF-07 — Remover Livro

**Descrição:** O sistema deve permitir a exclusão de um livro pelo ID.

### Diagrama UML de Sequência — RF-07

```
Cliente          BookController       BookService        BookRepository      MongoDB
   |                   |                   |                   |                |
   |  DELETE /books/   |                   |                   |                |
   |  id/{id}          |                   |                   |                |
   |──────────────────►|                   |                   |                |
   |                   | delete(id)        |                   |                |
   |                   |──────────────────►|                   |                |
   |                   |                   | deleteById(id)    |                |
   |                   |                   |──────────────────►|                |
   |                   |                   |                   | db.books.      |
   |                   |                   |                   | deleteOne(id)  |
   |                   |                   |                   |───────────────►|
   |                   |                   |                   |◄───────────────|
   |                   |                   |◄──────────────────|                |
   |                   |◄──────────────────|                   |                |
   |  200 OK           |                   |                   |                |
   |◄──────────────────|                   |                   |                |
```

### Testes cobrindo RF-07

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-07.1 | `BookControllerTest` | `shouldDeleteBook` | Caixa-preta / E2E | Livro removido → banco vazio |
| T-07.2 | `BookServiceTest` | `shouldDeleteBook` | Unitário (service) | Banco vazio após deleção |

---

## RF-08 — Validação de Campos Obrigatórios

**Descrição:** O sistema deve validar todos os campos obrigatórios nas entradas e retornar erros descritivos por campo.

**Regras de negócio:**
- Campos obrigatórios sem valor devem retornar 400 com mensagem por campo
- O handler global de exceções trata `MethodArgumentNotValidException`

### Diagrama UML de Sequência — RF-08

```
Cliente          BookController    GlobalExceptionHandler
   |                   |                   |
   |  POST /books      |                   |
   |  {} (body vazio)  |                   |
   |──────────────────►|                   |
   |                   | @Valid falha      |
   |                   | MethodArgumentNotValidException
   |                   |──────────────────►|
   |                   |                   | handleValidation()
   |                   |                   | → Map<campo, msg>
   |  400 Bad Request  |                   |
   |  {title: "...",   |                   |
   |   author: "...",  |                   |
   |   description:"..."}                  |
   |◄──────────────────|                   |
```

### Testes cobrindo RF-08

| ID | Classe | Método | Tipo | Cenário |
|---|---|---|---|---|
| T-08.1 | `BookControllerTest` | `shouldReturn400WhenInvalidBook` | Caixa-preta / E2E | Body vazio → 400 |
| T-08.2 | `AuthControllerTest` | `shouldReturnBadRequestWhenRegisterFieldsAreMissing` | Caixa-preta / E2E | Body vazio no register → 400 |
| T-08.3 | `GlobalExceptionHandlerTest` | `shouldReturnValidationErrorWhenBookFieldsAreMissing` | Caixa-preta | Erro por campo (title, author, description) |
| T-08.4 | `GlobalExceptionHandlerTest` | `shouldReturnMessageOnRuntimeException` | Caixa-preta | RuntimeException → campo `message` |
| T-08.5 | `ParameterizedControllerTest` | `shouldReturn400WhenBookFieldIsMissing` | Parametrizado | 5 corpos com campos ausentes → 400 |
| T-08.6 | `ParameterizedControllerTest` | `shouldReturn400WhenEmailIsInvalid` | Parametrizado | 5 formatos de e-mail inválidos → 400 |
| T-08.7 | `AuthServiceTest` | `shouldThrowExceptionWhenEmailAlreadyRegistered` | Unitário / Caixa-branca | Mensagem exata da exceção |
| T-08.8 | `BookServiceTest` | `shouldThrowExceptionWhenBookNotFound` | Unitário / Caixa-branca | Mensagem exata da exceção |

---

## RF-09 — Enriquecimento por ISBN (Open Library / VCR)

**Descrição:** O sistema deve consultar a Open Library API pelo ISBN e retornar os metadados do livro (título, autor, editora, páginas). As chamadas externas são interceptadas pelo WireMock (VCR) nos testes.

**Regras de negócio:**
- Se o ISBN for encontrado, retornar `found: true` com os metadados
- Se o ISBN não existir ou a API retornar erro, retornar `found: false`
- Se autores ou editoras estiverem ausentes, não incluir esses campos na resposta
- Nenhuma chamada real à internet deve ocorrer durante os testes

### Diagrama UML de Sequência — RF-09 (ISBN válido)

```
Cliente       BookEnrichmentController  BookEnrichmentService  OpenLibraryClient   WireMock (VCR)
   |                   |                       |                       |                  |
   |  GET /books/      |                       |                       |                  |
   |  isbn/{isbn}      |                       |                       |                  |
   |──────────────────►|                       |                       |                  |
   |                   | enrichByIsbn(isbn)    |                       |                  |
   |                   |──────────────────────►|                       |                  |
   |                   |                       | findByIsbn(isbn)      |                  |
   |                   |                       |──────────────────────►|                  |
   |                   |                       |                       | GET /api/books   |
   |                   |                       |                       | ?bibkeys=ISBN:.. |
   |                   |                       |                       |─────────────────►|
   |                   |                       |                       | cassete gravado  |
   |                   |                       |                       |◄─────────────────|
   |                   |                       |                       | parse JSON       |
   |                   |                       | Optional(response)    |                  |
   |                   |                       |◄──────────────────────|                  |
   |                   |                       | monta Map{found,      |                  |
   |                   |                       | title,author,...}     |                  |
   |                   | Map<String, Object>   |                       |                  |
   |                   |◄──────────────────────|                       |                  |
   |  200 OK           |                       |                       |                  |
   |  {found:true,     |                       |                       |                  |
   |   title,author,   |                       |                       |                  |
   |   publisher,pages}|                       |                       |                  |
   |◄──────────────────|                       |                       |                  |
```

### Diagrama UML de Sequência — RF-09 (ISBN não encontrado)

```
Cliente       BookEnrichmentController  BookEnrichmentService  OpenLibraryClient   WireMock (VCR)
   |                   |                       |                       |                  |
   |  GET /books/      |                       |                       |                  |
   |  isbn/{isbn}      |                       |                       |                  |
   |──────────────────►|                       |                       |                  |
   |                   | enrichByIsbn(isbn)    |                       |                  |
   |                   |──────────────────────►|                       |                  |
   |                   |                       | findByIsbn(isbn)      |                  |
   |                   |                       |──────────────────────►|                  |
   |                   |                       |                       | GET /api/books   |
   |                   |                       |                       |─────────────────►|
   |                   |                       |                       | cassete: {}      |
   |                   |                       |                       |◄─────────────────|
   |                   |                       |                       | raw == "{}"      |
   |                   |                       | Optional.empty()      |                  |
   |                   |                       |◄──────────────────────|                  |
   |                   |                       | monta Map{found:false,|                  |
   |                   |                       | isbn, message}        |                  |
   |                   | Map<String, Object>   |                       |                  |
   |                   |◄──────────────────────|                       |                  |
   |  200 OK           |                       |                       |                  |
   |  {found:false,    |                       |                       |                  |
   |   isbn, message}  |                       |                       |                  |
   |◄──────────────────|                       |                       |                  |
```

### Testes cobrindo RF-09

| ID | Classe | Método | Cassete | Cenário |
|---|---|---|---|---|
| T-09.1 | `OpenLibraryVcrTest` | `shouldReturnRecordedMetadataForValidIsbn` | `openlibrary-isbn-9780132350884.json` | ISBN válido → found=true, metadados completos |
| T-09.2 | `OpenLibraryVcrTest` | `shouldReturnNotFoundForUnknownIsbn` | `openlibrary-isbn-not-found.json` | ISBN inexistente → found=false |
| T-09.3 | `OpenLibraryVcrTest` | `shouldReturnNotFoundWhenApiReturnsError` | `openlibrary-isbn-error.json` | API retorna 500 → found=false |
| T-09.4 | `OpenLibraryVcrTest` | `shouldReturnNotFoundWhenResponseKeyIsMissing` | `openlibrary-isbn-chave-diferente.json` | Chave do ISBN ausente → found=false |
| T-09.5 | `OpenLibraryVcrTest` | `shouldReturnBookWithoutAuthorsAndPublishers` | `openlibrary-isbn-sem-autores.json` | JSON sem autores/editoras → found=true sem esses campos |
| T-09.6 | `OpenLibraryVcrTest` | `shouldReturnBookWithEmptyAuthorsAndPublishersList` | `openlibrary-isbn-autores-vazios.json` | Listas vazias → found=true sem esses campos |
| T-09.7 | `OpenLibraryVcrTest` | `shouldReturnNotFoundWhenResponseBodyIsBlank` | `openlibrary-isbn-resposta-vazia.json` | Corpo em branco → found=false |
| T-09.8 | `OpenLibraryVcrTest` | `shouldNotMakeUnmappedCallsToExternalApi` | — | Zero chamadas não mapeadas ao WireMock |

---

## RF-10 — Gerenciamento de Sessão no Frontend

**Descrição:** O frontend deve manter a sessão do usuário logado e redirecionar para login quando a sessão expirar ou não existir.

**Regras de negócio:**
- Após login bem-sucedido, os dados do usuário são armazenados em `sessionStorage`
- A página principal verifica a sessão ao carregar
- O logout limpa o `sessionStorage` e redireciona para login

**Cobertura:** Validação manual via browser. Não há testes automatizados de frontend neste projeto.

---

## Tabela Consolidada RTM

| ID Teste | Requisito | Classe | Método | Tipo |
|---|---|---|---|---|
| T-01.1 | RF-01 | `AuthControllerTest` | `shouldRegisterUser` | E2E |
| T-01.2 | RF-01 | `AuthControllerTest` | `shouldReturnBadRequestWhenEmailAlreadyExists` | E2E |
| T-01.3 | RF-01, RF-08 | `AuthControllerTest` | `shouldReturnBadRequestWhenRegisterFieldsAreMissing` | E2E |
| T-01.4 | RF-01 | `AuthServiceTest` | `shouldRegisterUserSuccessfully` | Unitário |
| T-01.5 | RF-01 | `AuthServiceTest` | `shouldHashPasswordOnRegister` | Caixa-branca |
| T-01.6 | RF-01, RF-08 | `AuthServiceTest` | `shouldThrowExceptionWhenEmailAlreadyRegistered` | Caixa-branca |
| T-01.7 | RF-01, RF-08 | `ParameterizedControllerTest` | `shouldReturn400WhenEmailIsInvalid` | Parametrizado |
| T-01.8 | RF-01 | `ParameterizedControllerTest` | `shouldAcceptPasswordsOfVariousLengths` | Parametrizado |
| T-02.1 | RF-02 | `AuthControllerTest` | `shouldLoginUser` | E2E |
| T-02.2 | RF-02 | `AuthControllerTest` | `shouldReturnBadRequestWhenPasswordIsWrong` | E2E |
| T-02.3 | RF-02 | `AuthControllerTest` | `shouldReturnBadRequestWhenUserNotFound` | E2E |
| T-02.4 | RF-02 | `AuthServiceTest` | `shouldLoginSuccessfully` | Unitário |
| T-02.5 | RF-02 | `AuthServiceTest` | `shouldThrowExceptionWhenPasswordIsWrong` | Caixa-branca |
| T-02.6 | RF-02 | `AuthServiceTest` | `shouldThrowExceptionWhenEmailNotFound` | Caixa-branca |
| T-03.1 | RF-03 | `BookControllerTest` | `shouldCreateBook` | E2E |
| T-03.2 | RF-03 | `BookServiceTest` | `shouldCreateBook` | Unitário |
| T-03.3 | RF-03 | `BookRepositoryTest` | `shouldSaveBook` | Repositório |
| T-03.4 | RF-03 | `ParameterizedControllerTest` | `shouldCreateBookWithValidData` | Parametrizado |
| T-03.5 | RF-03, RF-08 | `ParameterizedControllerTest` | `shouldReturn400WhenBookFieldIsMissing` | Parametrizado |
| T-03.6 | RF-03, RF-08 | `GlobalExceptionHandlerTest` | `shouldReturnValidationErrorWhenBookFieldsAreMissing` | E2E |
| T-04.1 | RF-04 | `BookControllerTest` | `shouldFindBooksByUser` | E2E |
| T-04.2 | RF-04 | `BookServiceTest` | `shouldFindBooksByUserEmail` | Unitário |
| T-04.3 | RF-04 | `BookRepositoryTest` | `shouldFindBooksByUserEmail` | Repositório |
| T-04.4 | RF-04 | `ParameterizedControllerTest` | `shouldReturnBooksOnlyForGivenUser` | Parametrizado |
| T-05.1 | RF-05 | `BookControllerTest` | `shouldFindBookById` | E2E |
| T-05.2 | RF-05 | `BookControllerTest` | `shouldReturn400WhenBookNotFound` | E2E |
| T-05.3 | RF-05 | `BookServiceTest` | `shouldFindBookById` | Unitário |
| T-05.4 | RF-05, RF-08 | `BookServiceTest` | `shouldThrowExceptionWhenBookNotFound` | Caixa-branca |
| T-06.1 | RF-06 | `BookControllerTest` | `shouldUpdateBook` | E2E |
| T-06.2 | RF-06 | `BookServiceTest` | `shouldUpdateBook` | Unitário |
| T-07.1 | RF-07 | `BookControllerTest` | `shouldDeleteBook` | E2E |
| T-07.2 | RF-07 | `BookServiceTest` | `shouldDeleteBook` | Unitário |
| T-08.1 | RF-08 | `BookControllerTest` | `shouldReturn400WhenInvalidBook` | E2E |
| T-08.2 | RF-08 | `GlobalExceptionHandlerTest` | `shouldReturnMessageOnRuntimeException` | E2E |
| T-09.1 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnRecordedMetadataForValidIsbn` | VCR |
| T-09.2 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnNotFoundForUnknownIsbn` | VCR |
| T-09.3 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnNotFoundWhenApiReturnsError` | VCR |
| T-09.4 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnNotFoundWhenResponseKeyIsMissing` | VCR |
| T-09.5 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnBookWithoutAuthorsAndPublishers` | VCR |
| T-09.6 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnBookWithEmptyAuthorsAndPublishersList` | VCR |
| T-09.7 | RF-09 | `OpenLibraryVcrTest` | `shouldReturnNotFoundWhenResponseBodyIsBlank` | VCR |
| T-09.8 | RF-09 | `OpenLibraryVcrTest` | `shouldNotMakeUnmappedCallsToExternalApi` | VCR |
| — | RF-10 | — | — | Manual |
