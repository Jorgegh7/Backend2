package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.InventarioRepository;
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
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @Test
    void findAllRetornaListaDeInventario() {
        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));

        List<Inventario> resultado = inventarioService.findAll();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(inventarioRepository).findAll();
    }

    @Test
    void findByIdRetornaInventarioCuandoExiste() {
        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        Inventario resultado = inventarioService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(inventarioRepository).findById(1L);
    }

    @Test
    void findByIdRetornaNullCuandoNoExiste() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Inventario resultado = inventarioService.findById(99L);

        assertNull(resultado);
        verify(inventarioRepository).findById(99L);
    }

    @Test
    void saveGuardaInventarioCorrectamente() {
        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.save(inventario);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void deleteByIdEliminaInventarioPorId() {
        inventarioService.deleteById(1L);

        verify(inventarioRepository).deleteById(1L);
    }

    @Test
    void findByProductoIdRetornaMovimientosDelProducto() {
        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioRepository.findByProductoId(10L)).thenReturn(List.of(inventario));

        List<Inventario> resultado = inventarioService.findByProductoId(10L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(inventarioRepository).findByProductoId(10L);
    }

    @Test
    void registrarMovimientoEntradaAumentaStockCuandoUsuarioEsAdmin() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoConStock(10);
        Inventario inventario = crearInventario(producto, 5, "ENTRADA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.registrarMovimientoComoAdministrador(inventario, admin);

        assertNotNull(resultado);
        assertEquals(15, producto.getStock());

        verify(usuarioService).hasRol(admin, "ADMIN");
        verify(productoRepository).save(producto);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void registrarMovimientoSalidaDescuentaStockCuandoUsuarioEsAdmin() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoConStock(10);
        Inventario inventario = crearInventario(producto, 4, "SALIDA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.registrarMovimientoComoAdministrador(inventario, admin);

        assertNotNull(resultado);
        assertEquals(6, producto.getStock());

        verify(productoRepository).save(producto);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void registrarMovimientoLanzaExcepcionCuandoUsuarioEsNull() {
        Inventario inventario = crearInventario(crearProductoConStock(10), 5, "ENTRADA");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(inventario, null)
        );

        assertEquals("El usuario no puede ser nulo", exception.getMessage());
        verifyNoInteractions(usuarioService);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoLanzaExcepcionCuandoUsuarioNoEsAdmin() {
        Usuario cliente = crearUsuarioConRol("CLIENTE");
        Inventario inventario = crearInventario(crearProductoConStock(10), 5, "ENTRADA");

        when(usuarioService.hasRol(cliente, "ADMIN")).thenReturn(false);

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(inventario, cliente)
        );

        assertEquals("Solo los administradores pueden modificar inventario", exception.getMessage());
        verify(usuarioService).hasRol(cliente, "ADMIN");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoLanzaExcepcionCuandoInventarioEsNull() {
        Usuario admin = crearUsuarioConRol("ADMIN");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(null, admin)
        );

        assertEquals("El inventario no puede ser nulo", exception.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoLanzaExcepcionCuandoNoTieneProducto() {
        Usuario admin = crearUsuarioConRol("ADMIN");

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(5);
        inventario.setTipoMovimiento("ENTRADA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(inventario, admin)
        );

        assertEquals("El inventario debe estar asociado a un producto", exception.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoLanzaExcepcionCuandoCantidadEsCero() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Inventario inventario = crearInventario(crearProductoConStock(10), 0, "ENTRADA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(inventario, admin)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoLanzaExcepcionCuandoTipoMovimientoEsInvalido() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Inventario inventario = crearInventario(crearProductoConStock(10), 5, "AJUSTE");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(inventario, admin)
        );

        assertEquals("El tipo de movimiento debe ser ENTRADA o SALIDA", exception.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void registrarMovimientoSalidaLanzaExcepcionCuandoNoHayStockSuficiente() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Producto producto = crearProductoConStock(3);
        Inventario inventario = crearInventario(producto, 5, "SALIDA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimientoComoAdministrador(inventario, admin)
        );

        assertEquals("No hay stock suficiente para realizar la salida", exception.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void actualizarComoAdministradorActualizaInventarioValido() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Inventario inventario = crearInventario(crearProductoConStock(10), 5, "ENTRADA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.actualizarComoAdministrador(inventario, admin);

        assertNotNull(resultado);
        verify(usuarioService).hasRol(admin, "ADMIN");
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void eliminarComoAdministradorEliminaRegistroExistente() {
        Usuario admin = crearUsuarioConRol("ADMIN");
        Inventario inventario = crearInventario(crearProductoConStock(10), 5, "ENTRADA");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        inventarioService.eliminarComoAdministrador(1L, admin);

        verify(usuarioService).hasRol(admin, "ADMIN");
        verify(inventarioRepository).findById(1L);
        verify(inventarioRepository).deleteById(1L);
    }

    @Test
    void eliminarComoAdministradorLanzaExcepcionCuandoRegistroNoExiste() {
        Usuario admin = crearUsuarioConRol("ADMIN");

        when(usuarioService.hasRol(admin, "ADMIN")).thenReturn(true);
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.eliminarComoAdministrador(99L, admin)
        );

        assertEquals("El registro de inventario no existe", exception.getMessage());
        verify(inventarioRepository, never()).deleteById(anyLong());
    }

    private Usuario crearUsuarioConRol(String nombreRol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(nombreRol.toLowerCase());
        usuario.setRoles(Set.of(new Rol(nombreRol)));
        return usuario;
    }

    private Producto crearProductoConStock(Integer stock) {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(stock);
        return producto;
    }

    private Inventario crearInventario(Producto producto, Integer cantidad, String tipoMovimiento) {
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setTipoMovimiento(tipoMovimiento);
        return inventario;
    }
}