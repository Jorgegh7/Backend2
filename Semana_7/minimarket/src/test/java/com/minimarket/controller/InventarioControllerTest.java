package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Usuario;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.InventarioService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioController.class)
@Import(SecurityConfig.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeListarInventario() throws Exception {
        when(inventarioService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeListarInventario() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeObtenerInventarioPorId() throws Exception {
        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioService.findById(1L)).thenReturn(inventario);

        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isOk());

        verify(inventarioService).findById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorRecibeNotFoundSiInventarioNoExiste() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/inventario/99"))
                .andExpect(status().isNotFound());

        verify(inventarioService).findById(99L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeRegistrarInventario() throws Exception {
        Usuario admin = crearUsuario("admin");

        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(usuarioService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(inventarioService.registrarMovimientoComoAdministrador(any(Inventario.class), eq(admin)))
                .thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(usuarioService).findByUsername("admin");
        verify(inventarioService).registrarMovimientoComoAdministrador(any(Inventario.class), eq(admin));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeRegistrarInventario() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(inventarioService, never())
                .registrarMovimientoComoAdministrador(any(Inventario.class), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeActualizarInventarioExistente() throws Exception {
        Usuario admin = crearUsuario("admin");

        Inventario inventarioExistente = new Inventario();
        inventarioExistente.setId(1L);

        Inventario inventarioActualizado = new Inventario();
        inventarioActualizado.setId(1L);

        when(inventarioService.findById(1L)).thenReturn(inventarioExistente);
        when(usuarioService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(inventarioService.actualizarComoAdministrador(any(Inventario.class), eq(admin)))
                .thenReturn(inventarioActualizado);

        mockMvc.perform(put("/api/inventario/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(inventarioService).findById(1L);
        verify(usuarioService).findByUsername("admin");
        verify(inventarioService).actualizarComoAdministrador(any(Inventario.class), eq(admin));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeActualizarInventario() throws Exception {
        mockMvc.perform(put("/api/inventario/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(inventarioService, never())
                .actualizarComoAdministrador(any(Inventario.class), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorRecibeNotFoundAlActualizarInventarioInexistente() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/inventario/99")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        verify(inventarioService).findById(99L);
        verify(inventarioService, never())
                .actualizarComoAdministrador(any(Inventario.class), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeEliminarInventarioExistente() throws Exception {
        Usuario admin = crearUsuario("admin");

        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioService.findById(1L)).thenReturn(inventario);
        when(usuarioService.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(delete("/api/inventario/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(inventarioService).findById(1L);
        verify(usuarioService).findByUsername("admin");
        verify(inventarioService).eliminarComoAdministrador(1L, admin);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeEliminarInventario() throws Exception {
        mockMvc.perform(delete("/api/inventario/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(inventarioService, never())
                .eliminarComoAdministrador(eq(1L), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorRecibeNotFoundAlEliminarInventarioInexistente() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/inventario/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(inventarioService).findById(99L);
        verify(inventarioService, never())
                .eliminarComoAdministrador(eq(99L), any(Usuario.class));
    }

    private Usuario crearUsuario(String username) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        return usuario;
    }
}