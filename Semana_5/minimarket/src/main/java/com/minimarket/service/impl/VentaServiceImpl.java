package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    private UsuarioService usuarioService;

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        if (!usuarioService.hasRol(venta.getUsuario(), "ADMIN")) {
            throw new RuntimeException("El usuario no tiene permisos para registrar ventas");
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            validarStock(detalle);
            validarPrecio(detalle);
        }
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    public Double calcularTotal(Venta venta) {
        return venta.getDetalles().stream()
                .mapToDouble(d -> d.getPrecio() * d.getCantidad())
                .sum();
    }

    private void validarStock(DetalleVenta detalle) {
        Producto producto = detalle.getProducto();
        if (producto.getStock() < detalle.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para el producto: "
                    + producto.getNombre());
        }
    }

    private void validarPrecio(DetalleVenta detalle) {
        if (detalle.getProducto().getPrecio() < 0) {
            throw new RuntimeException("El precio no puede ser negativo para el producto: "
                    + detalle.getProducto().getNombre());
        }
    }


}
