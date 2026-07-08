# MiniMarketPlus - Documentación de API con OpenAPI y HATEOAS

Backend desarrollado en **Java con Spring Boot** para la gestión de un minimarket. Esta versión del proyecto incorpora **documentación de API mediante OpenAPI/Swagger** y **navegabilidad hipermedia mediante HATEOAS (Hypermedia as the Engine of Application State)**, aplicando estándares OAS (OpenAPI Specification) sobre los controladores existentes.

---

## Descripción general

Este proyecto extiende la base de **MiniMarketPlus** (con autenticación, autorización por roles y pruebas unitarias) agregando dos capas adicionales:

* **OpenAPI/Swagger**: documentación interactiva y autogenerada de los endpoints REST.
* **HATEOAS**: enlaces dinámicos (`_links`) en las respuestas JSON que permiten al cliente navegar entre recursos relacionados sin necesidad de conocer las URLs de antemano.

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

## Ejemplo de respuesta con HATEOAS

Respuesta de `GET /api/usuarios/1`:

```json
{
  "id": 1,
  "username": "admin",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/usuarios/1"
    },
    "lista-usuarios": {
      "href": "http://localhost:8080/api/usuarios"
    }
  }
}
```

Cada recurso individual incluye un enlace `self` hacia sí mismo, y un enlace relacionado hacia la colección completa, permitiendo al cliente navegar la API sin necesidad de construir URLs manualmente.

---

## Validación externa

La documentación fue validada en dos herramientas:

* **Swagger UI**: prueba interactiva directa de cada endpoint, confirmando que los códigos de respuesta y los enlaces `_links`/`_embedded` se generan correctamente.
* **Postman**: la especificación OpenAPI (`/v3/api-docs`) fue importada como colección, confirmando que el comportamiento documentado coincide con las respuestas reales de la API fuera del entorno de Swagger.

---

## Reflexión técnica

La incorporación de OpenAPI y HATEOAS aporta a la calidad del backend en tres aspectos concretos: primero, formaliza el contrato de cada endpoint, exponiendo inconsistencias entre el código y la documentación (como discrepancias entre el código HTTP documentado y el realmente retornado) que de otra forma pasarían desapercibidas. Segundo, mejora la navegabilidad de la API: los enlaces `_links` permiten que un cliente descubra las acciones disponibles sobre un recurso sin depender de documentación externa o de hardcodear rutas, acercando la API al nivel de madurez REST de Richardson. Tercero, favorece la mantenibilidad, ya que la documentación se genera automáticamente desde las anotaciones del código, evitando la desincronización típica de documentación mantenida manualmente.

---

## Conclusión

Esta iteración de **MiniMarketPlus** demuestra la integración de OpenAPI/Swagger para documentación estandarizada y Spring HATEOAS para navegabilidad hipermedia, sobre una base ya validada con pruebas unitarias y autenticación por roles. La combinación de ambas herramientas, validada tanto en Swagger UI como en Postman, permite exponer una API REST autodescriptiva y consistente con los estándares OAS.
