package com.example.taskmanager.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.dto.UserResponseDTO;
import com.example.taskmanager.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Controller responsável pelos endpoints de autenticação
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*") // Permite requisições de qualquer origem (frontend)
public class AuthController {

    private final AuthService authService;

    // Endpoint para cadastrar um novo usuário
    @PostMapping("/register")
    public UserResponseDTO register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    // Endpoint para autenticar um usuário existente
    @PostMapping("/login")
    public UserResponseDTO login(
        @Valid @RequestBody LoginRequest request) {

        return authService.login(
                request.getEmail(),
                request.getPassword()
        );
    }
}