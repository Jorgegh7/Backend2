package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void gettersYSettersFuncionanCorrectamente() {
        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPassword("123456");

        assertEquals(1L, usuario.getId());
        assertEquals("admin", usuario.getUsername());
        assertEquals("123456", usuario.getPassword());
    }

    @Test
    void usuarioPermiteAsociarRoles() {
        Usuario usuario = new Usuario();
        Rol rolAdmin = new Rol("ADMIN");
        Rol rolCajero = new Rol("CAJERO");

        usuario.setRoles(Set.of(rolAdmin, rolCajero));

        assertNotNull(usuario.getRoles());
        assertEquals(2, usuario.getRoles().size());
        assertTrue(usuario.getRoles().stream()
                .anyMatch(rol -> "ADMIN".equals(rol.getNombre())));
        assertTrue(usuario.getRoles().stream()
                .anyMatch(rol -> "CAJERO".equals(rol.getNombre())));
    }
}