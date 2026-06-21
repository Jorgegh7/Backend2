package com.minimarket.entity;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CarritoTest {

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    public void setUp(){
        Set<Rol> roles = Set.of(new Rol("USER"));

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("user");
        usuario.setPassword("usuario123");
        usuario.setRoles(roles);

        Categoria categoria = new Categoria();
        categoria.setNombre("Abarrotes");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setCategoria(categoria);
        producto.setStock(10);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(5);
    }

    @Test
    public void crearCarritoTest(){
        assertNotNull(carrito);
        assertEquals(1L, carrito.getId());
        assertEquals(5, carrito.getCantidad());
        assertNotNull(carrito.getUsuario());
        assertNotNull(carrito.getProducto());
        assertEquals("user", carrito.getUsuario().getUsername());
    }

    @Test
    public void crearCarritoCamposNullTest(){
        carrito = new Carrito();

        assertNull(carrito.getId());
        assertNull(carrito.getProducto());
        assertNull(carrito.getUsuario());
        assertNull(carrito.getCantidad());
    }



}
