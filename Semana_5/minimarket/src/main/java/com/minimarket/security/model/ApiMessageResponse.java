package com.minimarket.security.model;

/**
 * DTO generico para devolver mensajes de la API.
 * Se usa para respuestas simples que solo necesitan un texto,
 * como confirmaciones, errores o avisos.
 */
public class ApiMessageResponse {

    private String message;

    public ApiMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}