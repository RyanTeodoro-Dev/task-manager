package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Classe de configuração do RestTemplate
@Configuration
public class RestTemplateConfig {

    // Registra o RestTemplate como um bean disponível para injeção em toda a aplicação
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}