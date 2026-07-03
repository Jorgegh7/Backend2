package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void gettersYSettersFuncionanCorrectamente() {
        Producto producto = new Producto();

        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(20);

        assertEquals(1L, producto.getId());
        assertEquals("Arroz", producto.getNombre());
        assertEquals(1500.0, producto.getPrecio());
        assertEquals(20, producto.getStock());
    }

    @Test
    void productoPermiteAsociarCategoria() {
        Producto producto = new Producto();
        Categoria categoria = new Categoria();

        categoria.setId(1L);
        categoria.setNombre("Abarrotes");

        producto.setCategoria(categoria);

        assertNotNull(producto.getCategoria());
        assertEquals(1L, producto.getCategoria().getId());
        assertEquals("Abarrotes", producto.getCategoria().getNombre());
    }
}