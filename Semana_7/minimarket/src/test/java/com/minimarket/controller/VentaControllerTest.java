package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentaController.class)
@Import(SecurityConfig.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService ventaService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroPuedeListarVentas() throws Exception {
        when(ventaService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeListarVentas() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroPuedeObtenerVentaExistente() throws Exception {
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaService.findById(1L)).thenReturn(venta);

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroRecibeNotFoundSiVentaNoExiste() throws Exception {
        when(ventaService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroPuedeGenerarVenta() throws Exception {
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaService.save(any(Venta.class))).thenReturn(venta);

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeGenerarVenta() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorNoPuedeGenerarVentaSiNoTieneRolCajero() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}