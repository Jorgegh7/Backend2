package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones para gestionar Usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Listar Usuarios", description = "Obtiene una lista con todos los Usuarios")
    @ApiResponse(responseCode = "200", description = "Listado obtenido de forma correcta")
    @GetMapping
    public CollectionModel<EntityModel<Usuario>> listarUsuarios() {
        List<EntityModel<Usuario>> usuarios = usuarioService.findAll().stream()
                .map(usuario -> {
                    EntityModel<Usuario> recurso = EntityModel.of(usuario);
                    recurso.add(linkTo(methodOn(UsuarioController.class)
                            .obtenerUsuarioPorId(usuario.getId())).withSelfRel());
                    return recurso;
                }).collect(Collectors.toList());
        return CollectionModel.of(usuarios,
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());
    }

    @Operation(summary = "Retorna Usuario por ID", description = "Obtiene un Usuario segun su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario obtenido de forma correcta"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();

        EntityModel<Usuario> recurso = EntityModel.of(usuario);

        recurso.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id))
                .withSelfRel());
        recurso.add(linkTo(methodOn(UsuarioController.class).listarUsuarios())
                .withRel("lista-usuarios"));
        recurso.add(linkTo(methodOn(UsuarioController.class).actualizarUsuario(id, usuario))
                .withRel("actualizar"));
        recurso.add(linkTo(methodOn(UsuarioController.class).eliminarUsuario(id))
                .withRel("eliminar"));

        return ResponseEntity.ok(recurso);
    }

    @Operation(summary = "Registrar Usuario", description = "Registra un nuevo Usuario en el sistema.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"username\":\"carlos.perez\",\"password\":\"cajero123\",\"roles\":[{\"id\":1}]}"
                            ))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);

        EntityModel<Usuario> recurso = EntityModel.of(usuarioGuardado);
        recurso.add(linkTo(methodOn(UsuarioController.class)
                .obtenerUsuarioPorId(usuarioGuardado.getId())).withSelfRel());
        recurso.add(linkTo(methodOn(UsuarioController.class)
                .listarUsuarios()).withRel("lista-usuarios"));
        recurso.add(linkTo(methodOn(UsuarioController.class)
                .actualizarUsuario(usuarioGuardado.getId(), usuarioGuardado)).withRel("actualizar"));
        recurso.add(linkTo(methodOn(UsuarioController.class)
                .eliminarUsuario(usuarioGuardado.getId())).withRel("eliminar"));

        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @Operation(summary = "Actualizar Usuario", description = "Actualiza un Usuario accediendo a este por su ID.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuario.setId(id);
            return ResponseEntity.ok(usuarioService.save(usuario));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar Usuario", description = "Elimina un Usuario por su ID.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) { // Verifica si el usuario existe
            usuarioService.deleteById(id); // Elimina al usuario
            return ResponseEntity.noContent().build(); // Respuesta 204 (sin contenido)
        }
        return ResponseEntity.notFound().build(); // Respuesta 404 (no encontrado)
    }
}
