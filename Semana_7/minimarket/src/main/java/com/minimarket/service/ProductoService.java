package com.minimarket.service;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;

import java.util.List;

public interface ProductoService {
    List<Producto> findAll();
    Producto findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
    List<Producto> findByCategoriaId(Long categoriaId);

    Producto guardarComoAdministrador(Producto producto, Usuario usuario);
    void eliminarComoAdministrador(Long id, Usuario usuario);
}