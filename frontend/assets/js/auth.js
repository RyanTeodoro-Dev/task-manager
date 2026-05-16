// URL base da API de autenticação
const authApi = "http://localhost:8080/auth";

// Função responsável por cadastrar um novo usuário
async function register() {

    // Coleta os valores dos campos do formulário
    const name =
        document.getElementById("name").value.trim();

    const email =
        document.getElementById("email").value.trim();

    const password =
        document.getElementById("password").value.trim();

    const confirmPassword =
        document.getElementById("confirmPassword").value.trim();

    // Verifica se todos os campos foram preenchidos
    if (!name || !email || !password || !confirmPassword) {

        alert("Preencha todos os campos!");

        return;
    }

    // Verifica se as senhas coincidem
    if (password !== confirmPassword) {

        alert("As senhas não coincidem!");

        return;
    }

    try {

        // Envia os dados do novo usuário para a API via POST
        const response = await fetch(`${authApi}/register`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                name,
                email,
                password
            })
        });

        // Se o cadastro for bem-sucedido, redireciona para o login
        if (response.ok) {

            alert("Usuário cadastrado com sucesso!");

            window.location.href = "login.html";

        } else {

            // Exibe a mensagem de erro retornada pela API
            const error = await response.text();

            console.log(error);

            alert(error);
        }

    } catch (error) {

        // Erro de conexão com o servidor
        console.error(error);

        alert("Erro ao conectar com o servidor");
    }
}

// Função responsável por autenticar o usuário
async function login() {

    // Coleta os valores dos campos do formulário
    const email =
        document.getElementById("email").value.trim();

    const password =
        document.getElementById("password").value.trim();

    // Verifica se todos os campos foram preenchidos
    if (!email || !password) {

        alert("Preencha todos os campos!");

        return;
    }

    try {

        // Envia as credenciais para a API via POST
        const response = await fetch(`${authApi}/login`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                email,
                password
            })
        });

        // Se o login for bem-sucedido, salva o usuário na sessão e redireciona
        if (response.ok) {

            const user = await response.json();

            // Armazena os dados do usuário no sessionStorage para manter a sessão
            sessionStorage.setItem(
                "user",
                JSON.stringify(user)
            );

            window.location.href = "index.html";

        } else {

            // Exibe a mensagem de erro retornada pela API
            const error = await response.text();

            console.log(error);

            alert(error);
        }

    } catch (error) {

        // Erro de conexão com o servidor
        console.error(error);

        alert("Erro ao conectar com o servidor");
    }
}