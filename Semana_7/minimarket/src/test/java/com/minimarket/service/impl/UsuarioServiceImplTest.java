package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void findAllRetornaListaDeUsuarios() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.findAll();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("admin", resultado.get(0).getUsername());
        verify(usuarioRepository).findAll();
    }

    @Test
    void findByIdRetornaUsuarioCuandoExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals("admin", resultado.get().getUsername());
        verify(usuarioRepository).findById(1L);
    }

    @Test
    void findByIdRetornaOptionalVacioCuandoNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(usuarioRepository).findById(99L);
    }

    @Test
    void findByUsernameRetornaUsuarioCuandoExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.findByUsername("admin");

        assertTrue(resultado.isPresent());
        assertEquals("admin", resultado.get().getUsername());
        verify(usuarioRepository).findByUsername("admin");
    }

    @Test
    void findByUsernameRetornaOptionalVacioCuandoNoExiste() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.findByUsername("desconocido");

        assertTrue(resultado.isEmpty());
        verify(usuarioRepository).findByUsername("desconocido");
    }

    @Test
    void saveGuardaUsuarioCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");

        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.save(usuario);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("admin", resultado.getUsername());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deleteByIdEliminaUsuarioPorId() {
        usuarioService.deleteById(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void hasRolRetornaTrueCuandoUsuarioTieneRol() {
        Rol rolAdmin = new Rol("ADMIN");

        Usuario usuario = new Usuario();
        usuario.setRoles(Set.of(rolAdmin));

        boolean resultado = usuarioService.hasRol(usuario, "ADMIN");

        assertTrue(resultado);
    }

    @Test
    void hasRolRetornaTrueIgnorandoMayusculasYMinusculas() {
        Rol rolAdmin = new Rol("admin");

        Usuario usuario = new Usuario();
        usuario.setRoles(Set.of(rolAdmin));

        boolean resultado = usuarioService.hasRol(usuario, "ADMIN");

        assertTrue(resultado);
    }

    @Test
    void hasRolRetornaFalseCuandoUsuarioNoTieneRol() {
        Rol rolCliente = new Rol("CLIENTE");

        Usuario usuario = new Usuario();
        usuario.setRoles(Set.of(rolCliente));

        boolean resultado = usuarioService.hasRol(usuario, "ADMIN");

        assertFalse(resultado);
    }

    @Test
    void hasRolRetornaFalseCuandoUsuarioEsNull() {
        boolean resultado = usuarioService.hasRol(null, "ADMIN");

        assertFalse(resultado);
    }

    @Test
    void hasRolRetornaFalseCuandoRolBuscadoEsNull() {
        Rol rolAdmin = new Rol("ADMIN");

        Usuario usuario = new Usuario();
        usuario.setRoles(Set.of(rolAdmin));

        boolean resultado = usuarioService.hasRol(usuario, null);

        assertFalse(resultado);
    }

    @Test
    void hasRolRetornaFalseCuandoUsuarioNoTieneRoles() {
        Usuario usuario = new Usuario();
        usuario.setRoles(null);

        boolean resultado = usuarioService.hasRol(usuario, "ADMIN");

        assertFalse(resultado);
    }
}