package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Classe de configuração de segurança da aplicação
@Configuration
public class SecurityConfig {

    // Define as regras de segurança HTTP da aplicação
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable()) // Desativa a proteção CSRF (a autenticação é feita via sessão no frontend)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permite acesso a todos os endpoints sem autenticação
            );

        return http.build();
    }

    // Define o encoder de senha usando BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}