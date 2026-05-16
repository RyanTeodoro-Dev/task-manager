package com.example.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO com os dados recebidos na requisição de cadastro
@Data
public class RegisterRequest {

    // Nome do usuário (obrigatório)
    @NotBlank
    private String name;

    // E-mail do usuário (obrigatório e deve ser válido)
    @Email
    @NotBlank
    private String email;

    // Senha do usuário (obrigatória)
    @NotBlank
    private String password;
}