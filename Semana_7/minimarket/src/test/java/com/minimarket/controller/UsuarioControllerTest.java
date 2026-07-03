package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeListarUsuarios() throws Exception {
        when(usuarioService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrearUsuario() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeCrearUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorObtieneUsuarioExistente() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorRecibeNotFoundSiUsuarioNoExiste() throws Exception {
        when(usuarioService.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeActualizarUsuarioExistente() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(put("/api/usuarios/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeActualizarUsuario() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorRecibeNotFoundAlActualizarUsuarioInexistente() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuarios/99")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeEliminarUsuarioExistente() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));

        mockMvc.perform(delete("/api/usuarios/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeEliminarUsuario() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorRecibeNotFoundAlEliminarUsuarioInexistente() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/usuarios/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}