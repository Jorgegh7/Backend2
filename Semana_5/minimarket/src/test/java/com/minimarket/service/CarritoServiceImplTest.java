package com.minimarket.service;

import com.minimarket.entity.*;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.service.impl.CarritoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
public class CarritoServiceImplTest {

    @Mock
    CarritoRepository carritoRepository;

    @Mock
    ProductoService productoService;

    @Mock
    UsuarioService usuarioService;

    @InjectMocks
    CarritoServiceImpl carritoService;

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
    public void guardarCarritoCorrectoTest(){
        //Arrange
        when(usuarioService.findById(carrito.getUsuario().getId())).thenReturn(Optional.ofNullable(usuario));
        when(productoService.findById(carrito.getProducto().getId())).thenReturn(producto);
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        //Act
        Carrito carritoRespuesta = carritoService.save(carrito);

        //Assert
        assertNotNull(carritoRespuesta);
        assertNotNull(carritoRespuesta.getUsuario());
        assertNotNull(carritoRespuesta.getProducto());
        assertEquals(5, carritoRespuesta.getCantidad());
        verify(carritoRepository).save(carrito);
        verify(usuarioService).findById(carrito.getUsuario().getId());
    }

    @Test
    public void guardarCarritoConStockInsuficienteTest(){
        //Arrange
        carrito.setCantidad(11);
        when(usuarioService.findById(carrito.getUsuario().getId())).thenReturn(Optional.ofNullable(usuario));
        when(productoService.findById(carrito.getProducto().getId())).thenReturn(producto);

        //Act & Assert
        assertThrows(RuntimeException.class,() -> carritoService.save(carrito));
        verify(carritoRepository, never()).save(carrito);
    }

    @Test
    public void guardarCarritoConCantidadCeroTest(){
        //Arrange
        carrito.setCantidad(0);
        when(usuarioService.findById(carrito.getUsuario().getId())).thenReturn(Optional.ofNullable(usuario));
        when(productoService.findById(carrito.getProducto().getId())).thenReturn(producto);

        //Act & Assert
        assertThrows(RuntimeException.class,() -> carritoService.save(carrito));
        verify(carritoRepository, never()).save(carrito);
    }

    @Test
    public void guardarCarritoConProductoInexistenteTest(){
       //Arrange
        when(usuarioService.findById(carrito.getUsuario().getId())).thenReturn(Optional.ofNullable(usuario));
        when(productoService.findById(carrito.getProducto().getId())).thenReturn(null);

        //Act & Assert
        assertThrows(RuntimeException.class,() -> carritoService.save(carrito));
        verify(carritoRepository, never()).save(carrito);
    }

    @Test
    public void guardarCarritoConUsuarioInexistenteTest(){
        when(usuarioService.findById(carrito.getUsuario().getId())).thenReturn(null);

        //Act & Assert
        assertThrows(RuntimeException.class,() -> carritoService.save(carrito));
        verify(carritoRepository, never()).save(carrito);
    }

    @Test
    public void encontrarCarritoPorIdTest(){
        //Arrange
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        //Act
        Carrito respueta = carritoService.findById(1L);

        //Assert
        assertNotNull(respueta);
        verify(carritoRepository).findById(1L);
    }

    @Test
    public void encontrarCarritosPorUsuarioIdTest(){
        //Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(List.of(carrito));

        //Act
        List<Carrito> respuestaListaCarritos = carritoService.findByUsuarioId(1L);

        //Assert
        assertEquals(1, respuestaListaCarritos.size());
        assertNotNull(respuestaListaCarritos);
        assertEquals("user", respuestaListaCarritos.get(0).getUsuario().getUsername());
        verify(carritoRepository).findByUsuarioId(1L);
    }

}
