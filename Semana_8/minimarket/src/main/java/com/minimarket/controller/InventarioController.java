package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones para gestionar Inventarios")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    @Operation(summary = "Listar Movimientos Inventario", description = "Obtiene una lista con todos los Movimientos de Inventario")
    @ApiResponse(responseCode = "200", description = "Listado obtenido de forma correcta")
    @GetMapping
    public CollectionModel<EntityModel<Inventario>> listarMovimientosDeInventario() {
        List<EntityModel<Inventario>> inventarios = inventarioService.findAll()
                .stream().map(inventario -> {
                    EntityModel<Inventario> recurso = EntityModel.of(inventario);
                    recurso.add(linkTo(methodOn(InventarioController.class)
                            .obtenerMovimientoPorId(inventario.getId())).withSelfRel());
                    return recurso;
                })
                .collect(Collectors.toList());

        return CollectionModel.of(inventarios,
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel());
    }

    @Operation(summary = "Retorna Inventario por ID", description = "Obtiene un Inventario segun su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario obtenido de forma correcta"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Inventario> obtenerMovimientoPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(inventario) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Registrar Movimiento en Inventario", description = "Registra un nuevo Movimiento en el Inventario.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "201", description = "Movimiento Inventario creado correctamente",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"producto\":{\"id\":1},\"cantidad\":10,\"tipoMovimiento\":\"ENTRADA\"}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<Inventario>> registrarMovimiento(@RequestBody Inventario inventario) {
        Producto producto = productoService.findById(inventario.getProducto().getId());
        inventario.setProducto(producto);

        Inventario inventarioGuardado = inventarioService.save(inventario);

        EntityModel<Inventario> recurso = EntityModel.of(inventarioGuardado);
        recurso.add(linkTo(methodOn(InventarioController.class)
                .obtenerMovimientoPorId(inventarioGuardado.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(InventarioController.class)
                .listarMovimientosDeInventario()).withRel("lista-movimientos"));

        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @Operation(summary = "Actualizar Movimiento Inventario", description = "Actualiza un Movimiento Inventario accediendo a este por su ID.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Moviiento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movimiento Inventario no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Inventario> actualizarMovimiento(@PathVariable Long id, @RequestBody Inventario inventario) {
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            return ResponseEntity.ok(inventarioService.save(inventario));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar Movimiento Inventario", description = "Elimina un Movimiento Inventario por su ID.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "204", description = "Movimiento Inventario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Movimiento Inventario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
