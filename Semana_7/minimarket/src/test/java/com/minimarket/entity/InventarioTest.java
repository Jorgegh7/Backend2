package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class InventarioTest {

    private Producto producto;
    private Inventario inventario;
    private Categoria categoria;

    @BeforeEach
    public void setup(){
        categoria = new Categoria();
        categoria.setNombre("Abarrotes");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setCategoria(categoria);
        producto.setStock(10);

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(5);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());
    }

    @Test
    public void testCrearInventario(){
        //Assert
        assertNotNull(inventario);
        assertEquals(1L, inventario.getId());
        assertNotNull(inventario.getProducto());
        assertEquals(5, inventario.getCantidad());
        assertEquals(1500, inventario.getProducto().getPrecio());
        assertEquals("Entrada", inventario.getTipoMovimiento());
        assertNotNull(inventario.getFechaMovimiento());
    }

    @Test
    public void testInventarioCamposNulos() {
        Inventario inventarioVacio = new Inventario();

        assertNull(inventarioVacio.getProducto());
        assertNull(inventarioVacio.getCantidad());
        assertNull(inventarioVacio.getTipoMovimiento());
        assertNull(inventarioVacio.getFechaMovimiento());
    }

}