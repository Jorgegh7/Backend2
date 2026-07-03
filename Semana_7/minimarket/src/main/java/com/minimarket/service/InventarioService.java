package com.minimarket.service;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Usuario;

import java.util.List;

public interface InventarioService {
    List<Inventario> findAll();
    Inventario findById(Long id);
    Inventario save(Inventario inventario);
    void deleteById(Long id);
    List<Inventario> findByProductoId(Long productoId);

    Inventario registrarMovimientoComoAdministrador(Inventario inventario, Usuario usuario);
    Inventario actualizarComoAdministrador(Inventario inventario, Usuario usuario);
    void eliminarComoAdministrador(Long id, Usuario usuario);
}