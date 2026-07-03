package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carritos", description = "Operaciones para gestionar Carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Operation(summary = "Listar Carritos", description = "Obtiene una lista con todos los Carritos")
    @ApiResponse(responseCode = "200", description = "Listado obtenido de forma correcta")
    @GetMapping
    public List<Carrito> listarCarrito() {
        return carritoService.findAll();
    }

    @Operation(summary = "Retorna Carrito por ID", description = "Obtiene un carrito segun su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido de forma correcta"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Carrito> obtenerCarritoPorId(
            @Parameter(description = "ID Carrito", example = "1")
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        return (carrito != null) ? ResponseEntity.ok(carrito) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Registrar Carrito", description = "Registra un nuevo Carrito en el sistema")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "201", description = "Carrito creado correctamente",
                    content = @Content(
                            examples = @ExampleObject(value = "{\"usuario\":{\"id\":1},\"producto\":{\"id\":1},\"cantidad\":3}"))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Carrito> agregarProductoAlCarrito(@RequestBody Carrito carrito) {
        Carrito carritoGuardado = carritoService.save(carrito);
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoGuardado);
    }

    @Operation(summary = "Actualizar Carrito", description = "Actualiza un Carrito accediendo a este por su ID.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Carrito actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Carrito> actualizarCarrito(
            @Parameter(description = "ID Carrito", example = "1")
            @PathVariable Long id, @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            carrito.setId(id);
            return ResponseEntity.ok(carritoService.save(carrito));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar Carrito", description = "Elimina un Carrito por su ID.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "204", description = "Carrito eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
