package com.minimarket.security.model;

import java.util.List;

/**
 * DTO de respuesta despues de una autenticacion exitosa.
 * Se envia al cliente con los datos del token JWT generado.
 *
 * Campos:
 * - token: el JWT generado para el usuario
 * - type: tipo de token (generalmente "Bearer")
 * - expiresIn: tiempo de expiracion del token en milisegundos
 * - username: nombre del usuario autenticado
 * - roles: lista de roles asignados al usuario
 */
public class AuthResponse {

    private String token;
    private String type;
    private long expiresIn;
    private String username;
    private List<String> roles;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}