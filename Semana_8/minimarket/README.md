# MiniMarketPlus - Documentación de API con OpenAPI y HATEOAS

Backend desarrollado en **Java con Spring Boot** para la gestión de un minimarket. Esta versión del proyecto incorpora **documentación de API mediante OpenAPI/Swagger** y **navegabilidad hipermedia mediante HATEOAS (Hypermedia as the Engine of Application State)**, aplicando estándares OAS (OpenAPI Specification) sobre los controladores existentes.

---

## Descripción general

Este proyecto extiende la base de **MiniMarketPlus** (con autenticación, autorización por roles y pruebas unitarias) agregando dos capas adicionales:

* **OpenAPI/Swagger**: documentación interactiva y autogenerada de los endpoints REST.
* **HATEOAS**: enlaces dinámicos (`_links`) en las respuestas JSON que permiten al cliente navegar entre recursos relacionados y descubrir las acciones disponibles sobre cada recurso, sin necesidad de conocer las URLs de antemano.

---

## Tecnologías utilizadas

| Tecnología                              | Uso                                   |
| ---------------------------------------- | -------------------------------------- |
| **Java 17**                              | Lenguaje principal                     |
| **Spring Boot 3.4.1**                    | Framework backend                      |
| **Spring Web**                           | Construcción de API REST               |
| **Spring Data JPA**                      | Persistencia de datos                  |
| **Spring Security**                      | Autenticación y autorización           |
| **Spring HATEOAS**                       | Enlaces dinámicos en las respuestas    |
| **springdoc-openapi-starter-webmvc-ui**  | Documentación OpenAPI 3.0 / Swagger UI |
| **H2 Database**                          | Base de datos en memoria               |
| **Maven**                                | Gestión de dependencias                |

---

## Endpoints documentados

| Entidad    | Método | Ruta                    | Descripción                          | HATEOAS |
| ---------- | ------ | ------------------------ | ------------------------------------- | :-----: |
| Producto   | GET    | `/api/productos`         | Lista todos los productos             | ✅ CollectionModel |
| Producto   | POST   | `/api/productos`         | Registra un nuevo producto            | ✅ EntityModel |
| Carrito    | GET    | `/api/carrito`           | Lista los productos del carrito       | ✅ CollectionModel |
| Carrito    | POST   | `/api/carrito`           | Agrega un producto al carrito         | ✅ EntityModel |
| Inventario | GET    | `/api/inventario`        | Lista los movimientos de inventario   | ✅ CollectionModel |
| Inventario | POST   | `/api/inventario`        | Registra un nuevo movimiento          | ✅ EntityModel |
| Usuario    | GET    | `/api/usuarios`          | Lista los usuarios registrados        | ✅ CollectionModel |
| Usuario    | GET    | `/api/usuarios/{id}`     | Obtiene un usuario por su ID          | ✅ EntityModel |
| Usuario    | POST   | `/api/usuarios`          | Registra un nuevo usuario             | ✅ EntityModel |

Cada endpoint está anotado con `@Operation` y `@ApiResponses`, especificando resumen, descripción y códigos de respuesta (`200`, `201`, `400`, `403`, `404`) según el comportamiento real del controlador. Los métodos `POST` incluyen además ejemplos de request mediante `@ExampleObject`.

---

## Ejecución del proyecto

### Iniciar el proyecto

```bash
mvn spring-boot:run
```

La aplicación queda disponible en:

```text
http://localhost:8080
```

### Acceder a Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

El título, versión y descripción mostrados en Swagger UI se personalizan mediante la clase `OpenApiConfig` (`@Configuration` + bean `OpenAPI`), en lugar de usar el nombre genérico del proyecto.

### Especificación OpenAPI en formato JSON

```text
http://localhost:8080/v3/api-docs
```

Este archivo puede exportarse e importarse en Postman como colección para validar los endpoints y sus enlaces HATEOAS de forma externa a Swagger.

---

## Autenticación

El proyecto utiliza **Spring Security con autenticación basada en sesión (formLogin)**. Para probar endpoints protegidos desde Swagger UI o Postman:

1. Iniciar sesión en `http://localhost:8080/login`.
2. Sin cerrar la sesión, navegar a Swagger UI o realizar las siguientes peticiones desde Postman (reutilizando la cookie de sesión).

**Usuario de prueba (cargado vía `data.sql`):**

| Campo    | Valor      |
| -------- | ---------- |
| Username | `admin`    |
| Password | `admin123` |
| Rol      | `ADMIN`    |

> **Nota:** al usar H2 en memoria, los datos se reinician en cada arranque y se recargan automáticamente desde `data.sql`.

---

## Implementación de HATEOAS

Se implementó HATEOAS sobre los endpoints de listado y registro de las cuatro entidades principales, utilizando `EntityModel` (recurso individual) y `CollectionModel` (colección de recursos), junto con enlaces de navegación (`self`, enlace a la colección) y enlaces de acción (`actualizar`, `eliminar`) que apuntan a las operaciones `PUT` y `DELETE` disponibles sobre cada recurso.

### Ejemplo de respuesta — `EntityModel` con enlaces de navegación y acción

Respuesta de `GET /api/productos/1`:

```json
{
  "id": 1,
  "nombre": "Leche Entera 1L",
  "precio": 1200,
  "stock": 50,
  "categoria": {
    "id": 1,
    "nombre": "Lácteos"
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "lista-productos": {
      "href": "http://localhost:8080/api/productos"
    },
    "actualizar": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "eliminar": {
      "href": "http://localhost:8080/api/productos/1"
    }
  }
}
```

Los enlaces `self`, `actualizar` y `eliminar` comparten la misma URL, ya que en REST el recurso es el mismo; lo que distingue la acción disponible es el método HTTP asociado a cada enlace (`GET`, `PUT`, `DELETE` respectivamente).

### Ejemplo de respuesta — `CollectionModel`

Respuesta de `GET /api/productos`:

```json
{
  "_embedded": {
    "productoList": [
      {
        "id": 1,
        "nombre": "Leche Entera 1L",
        "precio": 1200,
        "stock": 50,
        "categoria": { "id": 1, "nombre": "Lácteos" },
        "_links": {
          "self": { "href": "http://localhost:8080/api/productos/1" },
          "actualizar": { "href": "http://localhost:8080/api/productos/1" },
          "eliminar": { "href": "http://localhost:8080/api/productos/1" }
        }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/productos" }
  }
}
```

Cada elemento dentro de `_embedded` trae sus propios enlaces individuales, mientras que la colección completa trae su enlace `self` general, siguiendo el formato HAL (`application/hal+json`).

---

## Patrón `RepresentationModelAssembler`

Como mejora arquitectónica sobre la implementación manual de HATEOAS (construir los enlaces directamente dentro de cada método del controlador), se incorporó el patrón `RepresentationModelAssembler` de Spring HATEOAS, que centraliza la lógica de construcción de enlaces en una clase dedicada por entidad.

**Ejemplo — `ProductoModelAssembler`:**

```java
@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class)
                        .obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class)
                        .listarProductos()).withRel("lista-productos"),
                linkTo(methodOn(ProductoController.class)
                        .actualizarProducto(producto.getId(), producto, null)).withRel("actualizar"),
                linkTo(methodOn(ProductoController.class)
                        .eliminarProducto(producto.getId(), null)).withRel("eliminar"));
    }

    @Override
    public CollectionModel<EntityModel<Producto>> toCollectionModel(Iterable<? extends Producto> entities) {
        List<EntityModel<Producto>> productos = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
    }
}
```

Con el Assembler inyectado, el controlador se simplifica considerablemente:

```java
@Autowired
private ProductoModelAssembler assembler;

@GetMapping
public CollectionModel<EntityModel<Producto>> listarProductos() {
    return assembler.toCollectionModel(productoService.findAll());
}

@PostMapping
public ResponseEntity<EntityModel<Producto>> guardarProducto(@RequestBody Producto producto, Authentication authentication) {
    Usuario usuario = obtenerUsuarioAutenticado(authentication);
    Producto productoGuardado = productoService.guardarComoAdministrador(producto, usuario);
    return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(productoGuardado));
}
```

Este patrón se implementó como prueba de concepto en las entidades **Producto**, **Usuario** y **Carrito** (`ProductoModelAssembler`, `UsuarioModelAssembler`, `CarritoModelAssembler`), demostrando el mecanismo de forma funcional. Se recomienda extenderlo a **Inventario** en una futura iteración del proyecto, siguiendo el mismo patrón ya validado.

---

## Validación externa

La documentación fue validada en dos herramientas:

* **Swagger UI**: prueba interactiva directa de cada endpoint, confirmando que los códigos de respuesta y los enlaces `_links`/`_embedded` se generan correctamente.
* **Postman**: la especificación OpenAPI (`/v3/api-docs`) fue importada como colección, confirmando que el comportamiento documentado coincide con las respuestas reales de la API fuera del entorno de Swagger.

---

## Reflexión técnica

La incorporación de OpenAPI y HATEOAS aporta a la calidad del backend en tres aspectos concretos: primero, formaliza el contrato de cada endpoint, exponiendo inconsistencias entre el código y la documentación (como discrepancias entre el código HTTP documentado y el realmente retornado) que de otra forma pasarían desapercibidas. Segundo, mejora la navegabilidad de la API: los enlaces `_links` permiten que un cliente descubra las acciones disponibles sobre un recurso —incluyendo operaciones de modificación y eliminación— sin depender de documentación externa o de hardcodear rutas, acercando la API al nivel de madurez REST de Richardson. Tercero, favorece la mantenibilidad, ya que la documentación se genera automáticamente desde las anotaciones del código, y la construcción de enlaces mediante `RepresentationModelAssembler` centraliza esa lógica evitando su repetición en cada método del controlador.

Esta característica se validó también fuera de Swagger UI, importando la especificación OpenAPI directamente en Postman: al generar la colección desde el spec, cada endpoint conservó su documentación, y al ejecutar las peticiones, las respuestas mostraron los mismos enlaces `_links`/`_embedded` observados en Swagger UI, confirmando que la implementación de HATEOAS no depende de una herramienta específica sino que es parte del contrato real de la API.

**Estrategias para mantener la documentación actualizada:**

1. **Generación automática, nunca manual.** El spec debe generarse desde las anotaciones presentes directamente en el código (`@Operation`, `@Schema`) mediante springdoc-openapi, no escribirse manualmente.
2. **Versionado explícito de la API.** Se recomienda adoptar versionado por URI (por ejemplo, `/api/v1/productos`), ya que es el enfoque más visible en Swagger UI en comparación con el versionado por header o query param. Así, cuando un endpoint deba cambiar de forma incompatible, se introduce una nueva versión (`/api/v2/...`) sin romper a los clientes que dependen de la anterior.
3. **Contract testing en el pipeline CI/CD.** Como trabajo futuro, se propone agregar un paso en GitHub Actions que valide que las respuestas reales del backend coincidan con lo prometido en el spec, detectando automáticamente inconsistencias como las corregidas durante este desarrollo.

---

## Conclusión

Esta iteración de **MiniMarketPlus** demuestra la integración de OpenAPI/Swagger para documentación estandarizada y Spring HATEOAS para navegabilidad hipermedia —incluyendo enlaces de acción y el patrón `RepresentationModelAssembler`— sobre una base ya validada con pruebas unitarias y autenticación por roles. La combinación de ambas herramientas, validada tanto en Swagger UI como en Postman, permite exponer una API REST autodescriptiva y consistente con los estándares OAS.
