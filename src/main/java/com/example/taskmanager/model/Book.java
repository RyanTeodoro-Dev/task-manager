package com.example.taskmanager.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Modelo que representa um livro salvo no MongoDB
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "books") // Mapeia para a coleção "books" no MongoDB
public class Book {

    @Id
    private String id; // ID gerado automaticamente pelo MongoDB

    @NotBlank(message = "Título obrigatório")
    private String title; // Título do livro

    @NotBlank(message = "Autor obrigatório")
    private String author; // Autor do livro

    @NotBlank(message = "Descrição obrigatória")
    private String description; // Descrição do livro

    @NotBlank(message = "E-mail obrigatório")
    private String userEmail; // E-mail do usuário dono do livro
}