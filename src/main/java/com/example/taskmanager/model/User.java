package com.example.taskmanager.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo que representa um usuário salvo no MongoDB
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users") // Mapeia para a coleção "users" no MongoDB
public class User {

    @Id
    private String id; // ID gerado automaticamente pelo MongoDB

    @NotBlank(message = "Nome obrigatório")
    private String name; // Nome do usuário

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail obrigatório")
    private String email; // E-mail do usuário (usado como login)

    @NotBlank(message = "Senha obrigatória")
    private String password; // Senha do usuário (armazenada criptografada)
}