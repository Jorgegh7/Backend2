package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la autenticacion del usuario (login).
 * Recibe las credenciales para validar y generar el token JWT.
 */
public class AuthRequest {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}