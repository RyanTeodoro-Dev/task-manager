package com.example.taskmanager.dto;

// DTO com os dados do usuário retornados nas respostas da API
public record UserResponseDTO(

        String id,    // ID do usuário
        String name,  // Nome do usuário
        String email  // E-mail do usuário

) {
}