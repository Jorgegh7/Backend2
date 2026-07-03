package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.ProductoRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    void findAllRetornaListaDeProductos() {
        Producto producto = new Producto();
        producto.setId(1L);

        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.findAll();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(productoRepository).findAll();
    }

    @Test
    void findByIdRetornaProductoCuandoExiste() {
        Producto producto = new Producto();
        producto.setId(1L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoRepository).findById(1L);
    }

    @Test
    void findByIdRetornaNullCuandoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Producto resultado = productoService.findById(99L);

        assertNull(resultado);
        verify(productoRepository).findById(99L);
    }

    @Test
    void saveGuardaProductoCorrectamente() {
        Producto producto = new Producto();
        producto.setId(1L);

        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.save(producto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoRepository).save(producto);
    }

    @Test
    void deleteByIdEliminaProductoPorId() {
        productoService.deleteById(1L);

        verify(productoRepository).deleteById(1L);
    }

    @Test
    void findByCategoriaIdRetornaProductosDeCategoria() {
        Producto producto = new Producto();
        producto.setId(1L);

        when(productoRepository.findByCategoriaId(10L)).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.findByCategoriaId(10L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(productoRepository).findByCategoriaId(10L);
    }

    @Test
    void guardarComoAdministradorGuardaProductoValidoCuandoUsuarioEsAdmin() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoValido();

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.guardarComoAdministrador(producto, admin);

        assertNotNull(resultado);
        assertEquals("Arroz", resultado.getNombre());
        assertEquals(1500.0, resultado.getPrecio());
        assertEquals(20, resultado.getStock());

        verify(usuarioService).hasRol(admin, "ADMIN");
        verify(productoRepository).save(producto);
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoUsuarioEsNull() {
        Producto producto = crearProductoValido();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.guardarComoAdministrador(producto, null)
        );

        assertEquals("El usuario no puede ser nulo", exception.getMessage());
        verifyNoInteractions(usuarioService);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoUsuarioNoEsAdmin() {
        Usuario cliente = crearUsuarioConRol("CLIENTE");
        Producto producto = crearProductoValido();

        when(usuarioService.hasRol(cliente, "ADMIN")).thenReturn(false);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> productoService.guardarComoAdministrador(producto, cliente)
        );

        assertEquals("Solo los administradores pueden modificar productos", exception.getMessage());
        verify(usuarioService).hasRol(cliente, "ADMIN");
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoProductoEsNull() {
        Usuario admin = crearUsuarioConRol("ADMIN");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.guardarComoAdministrador(null, admin)
        );

        assertEquals("El producto no puede ser nulo", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoProductoNoTieneNombre() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoValido();
        producto.setNombre("");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.guardarComoAdministrador(producto, admin)
        );

        assertEquals("El producto debe tener nombre", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoPrecioEsInvalido() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoValido();
        producto.setPrecio(0.0);

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.guardarComoAdministrador(producto, admin)
        );

        assertEquals("El precio del producto debe ser mayor a cero", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoStockEsNegativo() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoValido();
        producto.setStock(-1);

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.guardarComoAdministrador(producto, admin)
        );

        assertEquals("El stock del producto no puede ser negativo", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void guardarComoAdministradorLanzaExcepcionCuandoCategoriaEsNull() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoValido();
        producto.setCategoria(null);

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.guardarComoAdministrador(producto, admin)
        );

        assertEquals("El producto debe tener categoria", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarComoAdministradorEliminaProductoExistenteCuandoUsuarioEsAdmin() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoValido();
        producto.setId(1L);

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminarComoAdministrador(1L, admin);

        verify(usuarioService).hasRol(admin, "ADMIN");
        verify(productoRepository).findById(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void eliminarComoAdministradorLanzaExcepcionCuandoUsuarioEsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.eliminarComoAdministrador(1L, null)
        );

        assertEquals("El usuario no puede ser nulo", exception.getMessage());
        verifyNoInteractions(usuarioService);
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarComoAdministradorLanzaExcepcionCuandoUsuarioNoEsAdmin() {
        Usuario cliente = crearUsuarioConRol("CLIENTE");

        when(usuarioService.hasRol(cliente, "ADMIN")).thenReturn(false);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> productoService.eliminarComoAdministrador(1L, cliente)
        );

        assertEquals("Solo los administradores pueden eliminar productos", exception.getMessage());
        verify(usuarioService).hasRol(cliente, "ADMIN");
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarComoAdministradorLanzaExcepcionCuandoProductoNoExiste() {
        Usuario admin = crearUsuarioConRol("ADMIN");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.eliminarComoAdministrador(99L, admin)
        );

        assertEquals("El producto no existe", exception.getMessage());
        verify(productoRepository, never()).deleteById(anyLong());
    }

    private Usuario crearUsuarioConRol(String nombreRol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(nombreRol.toLowerCase());
        usuario.setRoles(Set.of(new Rol(nombreRol)));
        return usuario;
    }

    private Producto crearProductoValido() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(20);
        producto.setCategoria(categoria);

        return producto;
    }
}