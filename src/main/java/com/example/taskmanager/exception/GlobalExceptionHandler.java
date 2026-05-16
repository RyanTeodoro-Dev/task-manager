package com.example.taskmanager.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Classe responsável por tratar exceções de forma global na aplicação
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata erros de validação dos campos do formulário (ex: campo obrigatório vazio)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Percorre todos os erros de campo e adiciona no mapa de retorno
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),       // Nome do campo com erro
                                error.getDefaultMessage() // Mensagem de erro do campo
                        )
                );

        return errors;
    }

    // Trata exceções de negócio lançadas manualmente na aplicação
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(
            RuntimeException ex) {

        Map<String, String> error = new HashMap<>();

        // Retorna a mensagem da exceção no corpo da resposta
        error.put("message", ex.getMessage());

        return error;
    }
}