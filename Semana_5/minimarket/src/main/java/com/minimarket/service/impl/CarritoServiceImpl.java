package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.service.CarritoService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(id).orElse(null);
    }

    @Override
    public Carrito save(Carrito carrito) {
        Usuario usuario = usuarioService.findById(carrito.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        Producto producto = productoService.findById(carrito.getProducto().getId());
        if (producto == null) {
            throw new RuntimeException("El producto no existe");
        }

        if(carrito.getCantidad() <= 0){
            throw new RuntimeException("La cantidad seleccionada debe ser mayor a 0");
        } else if (carrito.getCantidad() > producto.getStock()) {
            throw new RuntimeException("La cantidad excede el stock del producto seleccionado");
        }

        return carritoRepository.save(carrito);
    }

    @Override
    public void deleteById(Long id) {
        carritoRepository.deleteById(id);
    }

    @Override
    public List<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }
}
