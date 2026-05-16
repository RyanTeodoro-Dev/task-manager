package com.example.taskmanager.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.taskmanager.model.User;

// Repositório responsável pelas operações no banco de dados da coleção de usuários
public interface UserRepository extends MongoRepository<User, String> {

    // Busca um usuário pelo e-mail
    Optional<User> findByEmail(String email);
}