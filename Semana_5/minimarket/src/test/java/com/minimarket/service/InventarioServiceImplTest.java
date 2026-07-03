package com.minimarket.service;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    Producto producto;
    Inventario inventario;

    @BeforeEach
    public void setup(){
        Categoria categoria = new Categoria();
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
    public void guardarInventarioTipoMovimientoEntradaTest() {
        // Arrange
        when(productoService.findById(producto.getId())).thenReturn(producto);
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        // Act
        Inventario respuesta = inventarioService.save(inventario);

        // Assert
        assertNotNull(respuesta);
        assertEquals(15, producto.getStock()); // 10 inicial + 5 de entrada
        verify(productoService).save(producto);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    public void guardarInventarioTipoMovimientoSalidaConStockSuficienteTest(){
        //Arrange
        inventario.setTipoMovimiento("Salida");
        when(productoService.findById(producto.getId())).thenReturn(producto);
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        //Act
        Inventario respuesta = inventarioService.save(inventario);

        //Assert
        assertNotNull(respuesta);
        assertEquals("Salida", respuesta.getTipoMovimiento());
        assertTrue(producto.getStock() >= 0);
        assertEquals(5,  producto.getStock()); // Stock inicial producto - Cantidad
        verify(productoService).save(producto);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    public void guardarInventarioTipoMovimientoSalidaConStockInsuficienteTest(){
        //Arrange
        inventario.setTipoMovimiento("Salida");
        inventario.setCantidad(11);
        when(productoService.findById(producto.getId())).thenReturn(producto);

        //Assert
        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
        verify(productoService, never()).save(producto);
    }

    @Test
    public void guardarInventarioTipoMovimientoErroneoTest(){
        //Arrange
        inventario.setTipoMovimiento("Sin movimiento");
        when(productoService.findById(producto.getId())).thenReturn(producto);

        //Assert
        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
        verify(productoService, never()).save(producto);
    }

    @Test
    public void guardarInventarioCantidadMovmientoIgualACeroTest(){
        //Arrange
        inventario.setCantidad(0);
        when(productoService.findById(producto.getId())).thenReturn(producto);

        //Assert
        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
        verify(productoService, never()).save(producto);
    }

    @Test
    public void guardarInventarioConProductoNoEncontradoTest(){
        when(productoService.findById(producto.getId())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
        verify(productoService, never()).save(producto);
    }

    @Test
    public void encontrarInventarioPorIdTest(){
        //Arrange
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        //Act
        Inventario respuesta = inventarioService.findById(1L);

        //Assert
        assertNotNull(respuesta);
        verify(inventarioRepository).findById(1L);
    }

    @Test
    public void guardarInventarioConTipoMovimientoNuloTest(){
        //Arrange
        inventario.setTipoMovimiento(null);

        //Act & Assert
        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
    }

    @Test
    public void guardarInventarioConTipoMovimientoVacioTest(){
        //Arrange
        inventario.setTipoMovimiento("");

        //Act & Assert
        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
    }

    @Test
    public void guardarInventarioConCantidadNulaTest(){
        //Arrange
        inventario.setCantidad(null);

        //Act & Assert
        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(inventario);
    }


}
