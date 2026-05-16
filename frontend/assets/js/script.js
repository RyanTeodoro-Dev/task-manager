// Recupera o usuário salvo na sessão
const user = sessionStorage.getItem("user");

// Se não houver usuário na sessão, redireciona para o login
if (!user) {
    window.location.href = "login.html";
}

// Converte o usuário de string JSON para objeto
const loggedUser = JSON.parse(user);

// Exibe o nome do usuário logado na tela, se o elemento existir
const loggedUserElement =
    document.getElementById("loggedUser");

if (loggedUserElement) {
    loggedUserElement.innerText =
        `Olá, ${loggedUser.name}`;
}

// URL da API para buscar os livros do usuário logado
const api =
    `http://localhost:8080/books/user/${loggedUser.email}`;

// Função responsável por carregar e exibir os livros do usuário
async function loadBooks() {

    // Busca os livros do usuário na API
    const response = await fetch(api);

    const books = await response.json();

    // Seleciona o elemento onde os livros serão exibidos
    const booksDiv =
        document.getElementById("books");

    // Limpa os livros exibidos antes de renderizar novamente
    booksDiv.innerHTML = "";

    // Percorre cada livro e adiciona seu card na tela
    books.forEach(book => {

        // Escapa aspas simples para evitar erros ao passar valores nos botões
        booksDiv.innerHTML += `
            <div class="book">

                <h3>${book.title}</h3>

                <p>
                    <strong>Autor:</strong>
                    ${book.author}
                </p>

                <p>${book.description}</p>

                <div class="book-actions">
                    <button class="edit-btn" onclick="editBook(
                        '${book.id}',
                        '${book.title.replace(/'/g, "\\'")}',
                        '${book.author.replace(/'/g, "\\'")}',
                        '${book.description.replace(/'/g, "\\'")}'
                    )">
                        Editar
                    </button>

                    <button class="delete-btn" onclick="deleteBook('${book.id}')">
                        Excluir
                    </button>
                </div>

            </div>
        `;
    });
}

// Função responsável por salvar ou atualizar um livro
async function saveBook() {

    // Coleta os valores do formulário
    const id =
        document.getElementById("bookId").value;

    const title =
        document.getElementById("title").value.trim();

    const author =
        document.getElementById("author").value.trim();

    const description =
        document.getElementById("description").value.trim();

    // Verifica se todos os campos foram preenchidos
    if (!title || !author || !description) {

        alert("Preencha todos os campos!");

        return;
    }

    // Monta o objeto do livro com os dados do formulário e o e-mail do usuário
    const book = {
        title,
        author,
        description,
        userEmail: loggedUser.email
    };

    // Se houver ID, atualiza o livro existente; caso contrário, cadastra um novo
    if (id) {

        // Atualiza o livro via PUT
        await fetch(`http://localhost:8080/books/id/${id}`, {

            method: "PUT",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(book)
        });

        alert("Livro atualizado!");

    } else {

        // Cadastra um novo livro via POST
        await fetch("http://localhost:8080/books", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(book)
        });

        alert("Livro cadastrado!");
    }

    // Limpa o formulário e recarrega a lista de livros
    clearForm();

    loadBooks();
}

// Função responsável por preencher o formulário com os dados do livro a editar
function editBook(id, title, author, description) {

    document.getElementById("bookId").value = id;

    document.getElementById("title").value = title;

    document.getElementById("author").value = author;

    document.getElementById("description").value = description;
    
    // Rola a página até o formulário para facilitar a edição
    document.querySelector(".container").scrollIntoView({ behavior: "smooth" });
}

// Função responsável por excluir um livro
async function deleteBook(id) {

    // Solicita confirmação antes de excluir
    const confirmDelete =
        confirm("Deseja excluir este livro?");

    if (!confirmDelete) {
        return;
    }

    // Envia requisição DELETE para a API
    await fetch(`http://localhost:8080/books/id/${id}`, {
        method: "DELETE"
    });

    alert("Livro excluído!");

    // Recarrega a lista após a exclusão
    loadBooks();
}

// Função responsável por limpar todos os campos do formulário
function clearForm() {

    document.getElementById("bookId").value = "";

    document.getElementById("title").value = "";

    document.getElementById("author").value = "";

    document.getElementById("description").value = "";
}

// Função responsável por encerrar a sessão do usuário
function logout() {

    // Remove o usuário do sessionStorage e redireciona para o login
    sessionStorage.removeItem("user");

    window.location.href = "login.html";
}

// Carrega os livros assim que a página é aberta
loadBooks();