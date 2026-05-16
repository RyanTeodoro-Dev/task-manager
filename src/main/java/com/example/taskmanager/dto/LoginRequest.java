package com.example.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO com os dados recebidos na requisição de login
@Data
public class LoginRequest {

    // E-mail do usuário (obrigatório e deve ser válido)
    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail obrigatório")
    private String email;

    // Senha do usuário (obrigatória)
    @NotBlank(message = "Senha obrigatória")
    private String password;
}