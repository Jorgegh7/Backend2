package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
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
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    void findAllRetornaListaDeVentas() {
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        List<Venta> resultado = ventaService.findAll();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(ventaRepository).findAll();
    }

    @Test
    void findByIdRetornaVentaCuandoExiste() {
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Venta resultado = ventaService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(ventaRepository).findById(1L);
    }

    @Test
    void findByIdRetornaNullCuandoNoExiste() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        Venta resultado = ventaService.findById(99L);

        assertNull(resultado);
        verify(ventaRepository).findById(99L);
    }

    @Test
    void saveGuardaVentaCuandoUsuarioEsCajeroYTieneDetalles() {
        Usuario cajero = new Usuario();
        cajero.setId(1L);
        cajero.setUsername("cajero1");
        cajero.setRoles(Set.of(new Rol("CAJERO")));

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(cajero);
        venta.setDetalles(List.of(detalle));

        when(usuarioService.hasRol(cajero, "CAJERO")).thenReturn(true);
        when(ventaRepository.save(venta)).thenReturn(venta);

        Venta resultado = ventaService.save(venta);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(cajero, resultado.getUsuario());
        assertEquals(1, resultado.getDetalles().size());

        verify(usuarioService).hasRol(cajero, "CAJERO");
        verify(ventaRepository).save(venta);
    }

    @Test
    void saveLanzaExcepcionCuandoVentaEsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.save(null)
        );

        assertEquals("La venta no puede ser nula", exception.getMessage());
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void saveLanzaExcepcionCuandoVentaNoTieneUsuario() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setDetalles(List.of(detalle));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.save(venta)
        );

        assertEquals("La venta debe estar asociada a un usuario", exception.getMessage());
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void saveLanzaExcepcionCuandoUsuarioNoEsCajero() {
        Usuario cliente = new Usuario();
        cliente.setId(2L);
        cliente.setUsername("cliente1");
        cliente.setRoles(Set.of(new Rol("CLIENTE")));

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(cliente);
        venta.setDetalles(List.of(detalle));

        when(usuarioService.hasRol(cliente, "CAJERO")).thenReturn(false);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> ventaService.save(venta)
        );

        assertEquals("Solo los cajeros pueden generar ventas", exception.getMessage());
        verify(usuarioService).hasRol(cliente, "CAJERO");
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void saveLanzaExcepcionCuandoVentaNoTieneDetalles() {
        Usuario cajero = new Usuario();
        cajero.setId(1L);
        cajero.setUsername("cajero1");
        cajero.setRoles(Set.of(new Rol("CAJERO")));

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(cajero);
        venta.setDetalles(List.of());

        when(usuarioService.hasRol(cajero, "CAJERO")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.save(venta)
        );

        assertEquals("La venta debe contener productos vendidos", exception.getMessage());
        verify(usuarioService).hasRol(cajero, "CAJERO");
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void findByUsuarioIdRetornaVentasDelUsuario() {
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaRepository.findByUsuarioId(10L)).thenReturn(List.of(venta));

        List<Venta> resultado = ventaService.findByUsuarioId(10L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(ventaRepository).findByUsuarioId(10L);
    }
}