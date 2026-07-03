package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.ProductoService;
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

@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void usuarioAutenticadoPuedeListarProductos() throws Exception {
        when(productoService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void usuarioAutenticadoPuedeObtenerProductoPorId() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(20);

        when(productoService.findById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void usuarioAutenticadoRecibeNotFoundSiProductoNoExiste() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeCrearProducto() throws Exception {
        Usuario admin = crearUsuario("admin");
        Producto producto = new Producto();
        producto.setId(1L);

        when(usuarioService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(productoService.guardarComoAdministrador(any(Producto.class), eq(admin))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(usuarioService).findByUsername("admin");
        verify(productoService).guardarComoAdministrador(any(Producto.class), eq(admin));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeCrearProducto() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(productoService, never()).guardarComoAdministrador(any(Producto.class), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeActualizarProductoExistente() throws Exception {
        Usuario admin = crearUsuario("admin");

        Producto productoExistente = new Producto();
        productoExistente.setId(1L);

        Producto productoActualizado = new Producto();
        productoActualizado.setId(1L);

        when(productoService.findById(1L)).thenReturn(productoExistente);
        when(usuarioService.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(productoService.guardarComoAdministrador(any(Producto.class), eq(admin))).thenReturn(productoActualizado);

        mockMvc.perform(put("/api/productos/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(productoService).findById(1L);
        verify(usuarioService).findByUsername("admin");
        verify(productoService).guardarComoAdministrador(any(Producto.class), eq(admin));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeActualizarProducto() throws Exception {
        mockMvc.perform(put("/api/productos/1")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(productoService, never()).guardarComoAdministrador(any(Producto.class), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorRecibeNotFoundSiProductoNoExisteAlActualizar() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/productos/99")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        verify(productoService).findById(99L);
        verify(productoService, never()).guardarComoAdministrador(any(Producto.class), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorPuedeEliminarProductoExistente() throws Exception {
        Usuario admin = crearUsuario("admin");

        Producto producto = new Producto();
        producto.setId(1L);

        when(productoService.findById(1L)).thenReturn(producto);
        when(usuarioService.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(delete("/api/productos/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productoService).findById(1L);
        verify(usuarioService).findByUsername("admin");
        verify(productoService).eliminarComoAdministrador(1L, admin);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeEliminarProducto() throws Exception {
        mockMvc.perform(delete("/api/productos/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(productoService, never()).eliminarComoAdministrador(eq(1L), any(Usuario.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void administradorRecibeNotFoundAlEliminarProductoInexistente() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/productos/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(productoService).findById(99L);
        verify(productoService, never()).eliminarComoAdministrador(eq(99L), any(Usuario.class));
    }

    private Usuario crearUsuario(String username) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        return usuario;
    }
}