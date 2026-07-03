package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    public Inventario save(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Override
    public Inventario registrarMovimientoComoAdministrador(Inventario inventario, Usuario usuario) {
        validarUsuarioAdministrador(usuario);
        validarInventario(inventario);
        actualizarStockProducto(inventario);

        return inventarioRepository.save(inventario);
    }

    @Override
    public Inventario actualizarComoAdministrador(Inventario inventario, Usuario usuario) {
        validarUsuarioAdministrador(usuario);
        validarInventario(inventario);

        return inventarioRepository.save(inventario);
    }

    @Override
    public void eliminarComoAdministrador(Long id, Usuario usuario) {
        validarUsuarioAdministrador(usuario);

        Inventario inventario = inventarioRepository.findById(id).orElse(null);

        if (inventario == null) {
            throw new IllegalArgumentException("El registro de inventario no existe");
        }

        inventarioRepository.deleteById(id);
    }

    private void validarUsuarioAdministrador(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (!usuarioService.hasRol(usuario, "ADMIN")) {
            throw new SecurityException("Solo los administradores pueden modificar inventario");
        }
    }

    private void validarInventario(Inventario inventario) {
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario no puede ser nulo");
        }

        if (inventario.getProducto() == null) {
            throw new IllegalArgumentException("El inventario debe estar asociado a un producto");
        }

        if (inventario.getCantidad() == null || inventario.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        if (inventario.getTipoMovimiento() == null || inventario.getTipoMovimiento().isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }

        String tipoMovimiento = inventario.getTipoMovimiento().toUpperCase();

        if (!tipoMovimiento.equals("ENTRADA") && !tipoMovimiento.equals("SALIDA")) {
            throw new IllegalArgumentException("El tipo de movimiento debe ser ENTRADA o SALIDA");
        }
    }

    private void actualizarStockProducto(Inventario inventario) {
        Producto producto = inventario.getProducto();
        Integer stockActual = producto.getStock() == null ? 0 : producto.getStock();
        Integer cantidad = inventario.getCantidad();
        String tipoMovimiento = inventario.getTipoMovimiento().toUpperCase();

        if (tipoMovimiento.equals("ENTRADA")) {
            producto.setStock(stockActual + cantidad);
        }

        if (tipoMovimiento.equals("SALIDA")) {
            if (stockActual < cantidad) {
                throw new IllegalArgumentException("No hay stock suficiente para realizar la salida");
            }

            producto.setStock(stockActual - cantidad);
        }

        productoRepository.save(producto);
    }
}