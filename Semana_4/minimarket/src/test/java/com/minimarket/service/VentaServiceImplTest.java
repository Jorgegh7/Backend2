package com.minimarket.service;

import com.minimarket.entity.*;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta venta;
    private Producto producto;
    private DetalleVenta detalle;
    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    public void setUp() {
        Rol rol = new Rol("ADMIN");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        producto = new Producto();
        producto.setNombre("Arroz");
        producto.setStock(10);
        producto.setPrecio(1500.0);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("usuario_admin");
        usuario.setPassword("admin123");
        usuario.setRoles(roles);

        detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(5);
        detalle.setPrecio(1500.0);

        venta = new Venta();
        venta.setUsuario(usuario);
        venta.setDetalles(List.of(detalle));
    }

    @Test
    public void encontrarVentaPorId(){
        //Arrage
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        //Act
        Venta resultado = ventaService.findById(1L);

        //Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(ventaRepository).findById(1L);
    }

    @Test
    public void testUsuarioConRolAdminPuedeRegistrarVenta() {
        // Arrange
        when(usuarioService.hasRol(usuario, "ADMIN")).thenReturn(true);
        when(ventaRepository.save(venta)).thenReturn(venta);

        // Act
        Venta resultado = ventaService.save(venta);

        // Assert
        assertNotNull(resultado);
        verify(ventaRepository).save(venta);
    }

    @Test
    public void testCalcularTotalVenta() {
        // Act
        Double total = ventaService.calcularTotal(venta);

        // Assert
        assertEquals(7500.0, total);
    }

    @Test
    public void testGuardarVentaConStockSuficiente() {
        // Arrange
        when(usuarioService.hasRol(usuario, "ADMIN")).thenReturn(true);
        when(ventaRepository.save(venta)).thenReturn(venta);

        // Act
        Venta resultado = ventaService.save(venta);

        // Assert
        assertNotNull(resultado);
        verify(ventaRepository).save(venta);
    }

    @Test
    public void testGuardarVentaConStockInsuficiente() {
        // Arrange
        producto.setStock(0); // sobreescribes solo el stock
        when(usuarioService.hasRol(usuario, "ADMIN")).thenReturn(true);

        // Act Assert
        assertThrows(RuntimeException.class, () -> ventaService.save(venta));
        verify(ventaRepository, never()).save(venta);
    }

    @Test
    public void testGuardarVentaConProductoConPrecioNegativo() {
        // Arrange
        producto.setPrecio(-1500.0);
        when(usuarioService.hasRol(usuario, "ADMIN")).thenReturn(true);

        // Act  Assert
        assertThrows(RuntimeException.class, () -> ventaService.save(venta));
        verify(ventaRepository, never()).save(venta);
    }

    @Test
    public void testVentaVinculadaAUsuarioValido() {
        // Act
        Usuario usuarioDeVenta = venta.getUsuario();

        // Assert
        assertNotNull(usuarioDeVenta);
        assertNotNull(usuarioDeVenta.getUsername());
        assertNotNull(usuarioDeVenta.getPassword());
        assertNotNull(usuarioDeVenta.getRoles());
        assertFalse(usuarioDeVenta.getRoles().isEmpty());
    }




}
