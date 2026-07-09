package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.hateoas.ProductoModelAssembler;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones para gestionar Productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoModelAssembler assembler;

    @Operation(summary = "Listar Productos", description = "Obtiene una lista con todos los productos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido de forma correcta"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @GetMapping
    public CollectionModel<EntityModel<Producto>> listarProductos() {
        return assembler.toCollectionModel(productoService.findAll());
    }

    @Operation(summary = "Listar Producto", description = "Obtiene un producto por su ID")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(
            @Parameter(description = "ID Producto", example = "1")
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        return producto != null ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Registrar Producto", description = "Registra un nuevo producto en el sistema. Requiere rol ADMIN. El producto queda asociado al usuario administrador autenticado que lo creó.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"nombre\":\"Leche Entera 1L\",\"precio\":1200,\"stock\":50,\"categoria\":{\"id\":1}}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Producto>> guardarProducto(@RequestBody Producto producto, Authentication authentication) {
        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        Producto productoGuardado = productoService.guardarComoAdministrador(producto, usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(productoGuardado));
    }

    @Operation(summary = "Actualizar Producto", description = "Actualiza un producto accediendo a este por su ID. Requiere rol ADMIN.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Producto producto,
            Authentication authentication) {

        Producto productoExistente = productoService.findById(id);

        if (productoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        producto.setId(id);

        Producto productoActualizado = productoService.guardarComoAdministrador(producto, usuario);
        return ResponseEntity.ok(productoActualizado);
    }

    @Operation(summary = "Eliminar Producto", description = "Elimina un producto por su ID. Requiere rol ADMIN.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id, Authentication authentication) {
        Producto producto = productoService.findById(id);

        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = obtenerUsuarioAutenticado(authentication);
        productoService.eliminarComoAdministrador(id, usuario);

        return ResponseEntity.noContent().build();
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        String username = authentication.getName();

        return usuarioService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));
    }
}