package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VentaTest {

    @Test
    void gettersYSettersFuncionanCorrectamente() {
        Venta venta = new Venta();
        Date fecha = new Date();

        venta.setId(1L);
        venta.setFecha(fecha);

        assertEquals(1L, venta.getId());
        assertEquals(fecha, venta.getFecha());
    }

    @Test
    void ventaPermiteAsociarUsuario() {
        Venta venta = new Venta();
        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setUsername("cajero1");

        venta.setUsuario(usuario);

        assertNotNull(venta.getUsuario());
        assertEquals(1L, venta.getUsuario().getId());
        assertEquals("cajero1", venta.getUsuario().getUsername());
    }

    @Test
    void ventaPermiteAsociarDetalles() {
        Venta venta = new Venta();
        DetalleVenta detalle = new DetalleVenta();

        detalle.setId(1L);

        venta.setDetalles(List.of(detalle));

        assertNotNull(venta.getDetalles());
        assertEquals(1, venta.getDetalles().size());
        assertEquals(1L, venta.getDetalles().get(0).getId());
    }
}