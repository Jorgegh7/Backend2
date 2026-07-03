package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    @Override
    public Producto guardarComoAdministrador(Producto producto, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (!usuarioService.hasRol(usuario, "ADMIN")) {
            throw new SecurityException("Solo los administradores pueden modificar productos");
        }

        validarProducto(producto);

        return productoRepository.save(producto);
    }

    @Override
    public void eliminarComoAdministrador(Long id, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (!usuarioService.hasRol(usuario, "ADMIN")) {
            throw new SecurityException("Solo los administradores pueden eliminar productos");
        }

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            throw new IllegalArgumentException("El producto no existe");
        }

        productoRepository.deleteById(id);
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El producto debe tener nombre");
        }

        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor a cero");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock del producto no puede ser negativo");
        }

        if (producto.getCategoria() == null) {
            throw new IllegalArgumentException("El producto debe tener categoria");
        }
    }
}
