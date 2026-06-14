package com.minimarket.security.model;

import com.minimarket.entity.Rol;
import java.util.Set;

/**
 * DTO de respuesta con los datos del usuario.
 * Se usa para devolver informacion del usuario sin exponer
 * datos sensibles como la contraseña.
 *
 * Campos:
 * - id: identificador del usuario
 * - username: nombre de usuario
 * - nombreCompleto: nombre completo del usuario
 * - roles: conjunto de roles asignados
 *
 * Ejemplo de respuesta JSON:
 * {
 *     "id": 1,
 *     "username": "admin",
 *     "nombreCompleto": "Juan Perez",
 *     "roles": [{"id": 1, "nombre": "ADMIN"}]
 * }
 */
public class UsuarioResponse {

    private Long id;
    private String username;
    private String nombreCompleto;
    private Set<Rol> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }
}