package com.example.taskmanager.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.dto.UserResponseDTO;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// Service responsável pelas regras de negócio de autenticação
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder; // Usado para criptografar e validar senhas

    // Cadastra um novo usuário na aplicação
    public UserResponseDTO register(RegisterRequest request) {

        // Verifica se o e-mail já está em uso
        boolean exists =
                repository.findByEmail(request.getEmail()).isPresent();

        if (exists) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        // Cria o usuário com a senha criptografada
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .build();

        // Salva o usuário no banco e retorna os dados sem a senha
        User saved = repository.save(user);

        return new UserResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getEmail()
        );
    }

    // Autentica o usuário verificando e-mail e senha
    public UserResponseDTO login(String email, String rawPassword) {

        // Busca o usuário pelo e-mail e valida a senha
        User user = repository.findByEmail(email)
                .filter(u -> encoder.matches(rawPassword, u.getPassword()))
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials"));

        // Retorna os dados do usuário sem a senha
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}