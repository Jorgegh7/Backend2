package com.minimarket.security.model;

import com.minimarket.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * DTO (Data Transfer Object) utilizado para recibir los datos
 * de registro de un nuevo usuario desde el cliente (Postman, frontend, etc).
 * No es una entidad JPA, solo transporta datos de entrada.
 * Las anotaciones de validacion aseguran que los datos lleguen correctos
 * antes de procesarlos en el servicio.
 */
public class RegisterRequest {

    // Username obligatorio, entre 3 y 20 caracteres.
    // Solo permite letras, numeros, puntos, guiones y guion bajo.
    // Esto evita registrar valores con etiquetas HTML o scripts.
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El username solo puede contener letras, numeros, puntos, guiones y guion bajo"
    )
    private String username;

    // Contraseña obligatoria, minimo 6 caracteres para seguridad basica.
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    // Roles asignados al usuario (ej: ADMIN, EMPLEADO, CLIENTE).
    private Set<Rol> roles;

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }
}