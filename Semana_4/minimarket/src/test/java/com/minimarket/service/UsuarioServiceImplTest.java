package com.minimarket.service;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    @BeforeEach
    public void setUp() {
        Rol rol = new Rol("USER");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente_1");
        usuario.setPassword("cliente123");
        usuario.setRoles(roles);
    }

    @Test
    public void testGuardarUsuario() {
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.save(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("cliente_1", resultado.getUsername());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    public void testUsuarioTieneDatosObligatoriosCompletos() {
        // Arrange
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.save(usuario);

        // Assert
        assertNotNull(resultado.getUsername());
        assertNotNull(resultado.getPassword());
        assertNotNull(resultado.getRoles());
        assertFalse(resultado.getRoles().isEmpty());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    public void testEncontrarUsuarioPorId(){

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        //Act
        Optional<Usuario> resultado = usuarioService.findById(1L);

        //Assert
        assertTrue(resultado.isPresent());
        assertEquals("cliente_1", resultado.get().getUsername());
        verify(usuarioRepository).findById(1L);
    }

    @Test
    public void testEncontrarTodosLosUsuarios() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        // Act
        List<Usuario> resultado = usuarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(usuarioRepository).findAll();
    }



    @Test
    public void testEliminarUsuario() {
        // Act
        usuarioService.deleteById(1L);

        // Assert
        verify(usuarioRepository).deleteById(1L);
    }


    @Test
    public void testEncontrarUsuarioPorUsername(){
        //Arrange
        Usuario usuario= new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente_1");

        when(usuarioRepository.findByUsername("cliente_1")).thenReturn(Optional.of(usuario));

        //Act
        Optional<Usuario> resultado = usuarioService.findByUsername("cliente_1");

        //Assert
        assertTrue(resultado.isPresent());
        assertEquals("cliente_1", resultado.get().getUsername());
        verify(usuarioRepository).findByUsername("cliente_1");
    }

    @Test
    public void testTieneRolValido() {
        boolean resultado = usuarioService.hasRol(usuario, "USER");
        assertTrue(resultado);
    }

    @Test
    public void testNoTieneRolValido() {
        boolean resultado = usuarioService.hasRol(usuario, "ADMIN");
        assertFalse(resultado);
    }

}