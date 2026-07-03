package com.minimarket.service;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;


    private Producto producto;

    private Categoria categoria;

    @BeforeEach
    public void setup(){
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Alimentos");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setCategoria(categoria);
        producto.setStock(5);
    }

    @Test
    public void guardarProductoConPrecioNegativo(){
        //Arrange
        producto.setPrecio(-1000.0);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productoService.save(producto));
        verify(productoRepository, never()).save(producto);

    }

    @Test
    public void guardarProductoConStockNegativo(){
        //Arrange
        producto.setStock(-10);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productoService.save(producto));
        verify(productoRepository, never()).save(producto);
    }


}
