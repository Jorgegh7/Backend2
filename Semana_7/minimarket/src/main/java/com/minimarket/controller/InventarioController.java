package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Usuario;
import com.minimarket.service.InventarioService;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Inventario> listarInventario() {
        return inventarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventario> obtenerInventarioPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        return inventario != null ? ResponseEntity.ok(inventario) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Inventario> registrarInventario(
            @RequestBody Inventario inventario,
            Authentication authentication) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        Inventario inventarioGuardado =
                inventarioService.registrarMovimientoComoAdministrador(inventario, usuario);

        return ResponseEntity.ok(inventarioGuardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventario> actualizarInventario(
            @PathVariable Long id,
            @RequestBody Inventario inventario,
            Authentication authentication) {

        Inventario inventarioExistente = inventarioService.findById(id);

        if (inventarioExistente == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        inventario.setId(id);

        Inventario inventarioActualizado =
                inventarioService.actualizarComoAdministrador(inventario, usuario);

        return ResponseEntity.ok(inventarioActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInventario(
            @PathVariable Long id,
            Authentication authentication) {

        Inventario inventario = inventarioService.findById(id);

        if (inventario == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        inventarioService.eliminarComoAdministrador(id, usuario);

        return ResponseEntity.noContent().build();
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        String username = authentication.getName();

        return usuarioService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
    }
}