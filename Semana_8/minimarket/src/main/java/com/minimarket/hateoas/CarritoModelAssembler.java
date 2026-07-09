package com.minimarket.hateoas;

import com.minimarket.controller.CarritoController;
import com.minimarket.entity.Carrito;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, EntityModel<Carrito>> {

    @Override
    public EntityModel<Carrito> toModel(Carrito carrito) {
        return EntityModel.of(carrito,
                linkTo(methodOn(CarritoController.class)
                        .obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class)
                        .listarCarrito()).withRel("lista-carritos"),
                linkTo(methodOn(CarritoController.class)
                        .actualizarCarrito(carrito.getId(), carrito)).withRel("actualizar"),
                linkTo(methodOn(CarritoController.class)
                        .eliminarProductoDelCarrito(carrito.getId())).withRel("eliminar"));
    }

    @Override
    public CollectionModel<EntityModel<Carrito>> toCollectionModel(Iterable<? extends Carrito> entities) {
        List<EntityModel<Carrito>> carritos = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(carritos,
                linkTo(methodOn(CarritoController.class).listarCarrito()).withSelfRel());
    }
}