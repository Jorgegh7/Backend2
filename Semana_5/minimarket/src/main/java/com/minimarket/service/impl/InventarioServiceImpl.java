package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoService productoService;

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

        Producto producto = inventario.getProducto();
        Integer cantidad = inventario.getCantidad();
        String tipoMovimiento = inventario.getTipoMovimiento();

        if (inventario.getTipoMovimiento() == null || inventario.getTipoMovimiento().isBlank()) {
            throw new RuntimeException("El tipo de movimiento no puede ser nulo ni vacío");
        }
        if (inventario.getCantidad() == null) {
            throw new RuntimeException("La cantidad no puede ser nula");
        }

        if(productoService.findById(producto.getId()) == null){
            throw new RuntimeException("El producto no ha sido encontrado");
        }
        if(cantidad <= 0 ){
            throw new RuntimeException("La cantidad del movimiento debe ser mayor a 0");
        }
        if(tipoMovimiento.equalsIgnoreCase("Entrada")){
            producto.setStock(producto.getStock() + cantidad);
        } else if (tipoMovimiento.equalsIgnoreCase("Salida")) {
            if(producto.getStock() < cantidad) {
                throw new RuntimeException("La cantidad de salina no puede ser mayor al Stock actual");
            }
            producto.setStock(producto.getStock() - cantidad);
        }
        else{
            throw new RuntimeException("Tipo de movimiento inválido: " + tipoMovimiento);
        }
        productoService.save(producto);
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
}
