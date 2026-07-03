# MiniMarketPlus - Desarrollo Backend II

Backend desarrollado en **Java con Spring Boot** para la gestión de un minimarket.
El sistema implementa **autenticación**, **autorización por roles** y **pruebas unitarias** para validar reglas de negocio asociadas a productos, inventario, ventas y usuarios.

---

## Descripción general

**MiniMarketPlus** es una aplicación backend orientada a la administración de:

* Productos.
* Movimientos de inventario.
* Ventas.
* Usuarios y roles.

El proyecto incorpora seguridad mediante **Spring Security**, permitiendo restringir operaciones según el rol del usuario autenticado.

---

## Roles del sistema

| Rol         | Permisos principales                          |
| ----------- | --------------------------------------------- |
| **CLIENTE** | Consultar productos disponibles.              |
| **CAJERO**  | Generar y consultar ventas.                   |
| **ADMIN**   | Administrar productos, inventario y usuarios. |

---

## Tecnologías utilizadas

| Tecnología               | Uso                           |
| ------------------------ | ----------------------------- |
| **Java 17**              | Lenguaje principal            |
| **Spring Boot 3.4.1**    | Framework backend             |
| **Spring Web**           | Construcción de API REST      |
| **Spring Data JPA**      | Persistencia de datos         |
| **Spring Security**      | Autenticación y autorización  |
| **H2 Database**          | Base de datos en memoria      |
| **Maven**                | Gestión de dependencias       |
| **JUnit 5**              | Pruebas unitarias             |
| **Mockito**              | Simulación de dependencias    |
| **Spring Security Test** | Pruebas con roles y seguridad |
| **JaCoCo**               | Reporte de cobertura          |

---

## Estructura del proyecto

```text
minimarket/
├── src/
│   ├── main/
│   │   ├── java/com/minimarket/
│   │   │   ├── controller/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/minimarket/
│           ├── controller/
│           ├── entity/
│           └── service/
├── pom.xml
└── README.md
```

---

## Reglas de negocio implementadas

### Producto

* Los productos pueden ser consultados por usuarios autenticados.
* Solo usuarios con rol **ADMIN** pueden crear, actualizar o eliminar productos.
* El producto debe tener nombre.
* El precio debe ser mayor a cero.
* El stock no puede ser negativo.
* El producto debe estar asociado a una categoría.
* No se permite eliminar un producto inexistente.

### Inventario

* Solo usuarios con rol **ADMIN** pueden administrar inventario.
* Un movimiento de inventario debe estar asociado a un producto.
* La cantidad debe ser mayor a cero.
* El tipo de movimiento debe ser **ENTRADA** o **SALIDA**.
* Una **ENTRADA** aumenta el stock del producto.
* Una **SALIDA** descuenta stock del producto.
* No se permite una salida si no existe stock suficiente.
* No se permite eliminar un registro de inventario inexistente.

### Venta

* Solo usuarios con rol **CAJERO** pueden generar ventas.
* Una venta debe tener un usuario asociado.
* Una venta debe contener detalles de productos vendidos.
* No se permite registrar ventas nulas o sin productos.

### Usuario

* Los usuarios pueden tener uno o más roles.
* Se valida la existencia de roles mediante el servicio de usuarios.
* La validación de roles no distingue entre mayúsculas y minúsculas.

---

## Seguridad y autorización

La seguridad se configura mediante Spring Security en la clase `SecurityConfig`.

### Reglas principales de acceso

```text
GET    /api/productos/**    -> usuario autenticado
POST   /api/productos/**    -> ADMIN
PUT    /api/productos/**    -> ADMIN
DELETE /api/productos/**    -> ADMIN

/api/inventario/**          -> ADMIN
/api/ventas/**              -> CAJERO
/api/usuarios/**            -> ADMIN
```

Estas reglas aseguran que cada operación crítica sea ejecutada únicamente por usuarios con el rol correspondiente.

---

## Pruebas implementadas

El proyecto incluye pruebas unitarias y pruebas de capa web usando:

* JUnit 5.
* Mockito.
* MockMvc.
* Spring Security Test.

### Pruebas de controladores

| Clase de prueba            | Objetivo                                                                        |
| -------------------------- | ------------------------------------------------------------------------------- |
| `ProductoControllerTest`   | Validar consulta, creación, actualización y eliminación de productos según rol. |
| `InventarioControllerTest` | Validar acceso y gestión de inventario solo para administradores.               |
| `VentaControllerTest`      | Validar generación y consulta de ventas por cajeros.                            |
| `UsuarioControllerTest`    | Validar administración de usuarios solo por administradores.                    |

Estas pruebas verifican respuestas HTTP como:

```text
200 OK
204 No Content
403 Forbidden
404 Not Found
```

### Pruebas de servicios

| Clase de prueba             | Validaciones principales                                                               |
| --------------------------- | -------------------------------------------------------------------------------------- |
| `ProductoServiceImplTest`   | Producto válido, usuario administrador, precio, stock, categoría y eliminación segura. |
| `InventarioServiceImplTest` | Entradas, salidas, stock suficiente, cantidad válida y permisos de administrador.      |
| `VentaServiceImplTest`      | Venta con cajero, usuario asociado y detalles de productos.                            |
| `UsuarioServiceImplTest`    | Búsqueda de usuarios y validación de roles.                                            |

### Pruebas de entidades

Se agregaron pruebas para validar:

* Getters y setters.
* Relaciones básicas entre entidades.
* Asociación entre usuarios y roles.
* Asociación entre productos, inventario y ventas.

---

## Ejecución del proyecto

### Compilar el proyecto

```bash
mvn clean compile
```

### Ejecutar las pruebas

```bash
mvn test
```

### Ejecutar pruebas desde cero y generar cobertura

```bash
mvn clean test
```

---

## Reporte de cobertura

El proyecto utiliza **JaCoCo** para generar reportes de cobertura.

Después de ejecutar:

```bash
mvn clean test
```

el reporte HTML se genera en:

```text
target/site/jacoco/index.html
```

También se generan reportes en formato XML y CSV dentro de:

```text
target/site/jacoco/
```

---

## Mejoras realizadas

Durante el desarrollo se reforzó la lógica de negocio en la capa de servicios.
Inicialmente algunos métodos solo delegaban operaciones directamente al repositorio. Posteriormente se incorporaron validaciones para asegurar que las operaciones críticas respetaran las reglas del dominio.

### Principales mejoras

* Validación de rol **ADMIN** para modificar productos.
* Validación de datos obligatorios en productos.
* Validación de rol **ADMIN** para modificar inventario.
* Actualización automática de stock en movimientos de entrada y salida.
* Control de stock insuficiente.
* Validación de rol **CAJERO** para generar ventas.
* Validación de ventas con usuario y detalles.
* Pruebas unitarias para escenarios exitosos y fallidos.

---

## Conclusión

**MiniMarketPlus** implementa una solución backend con autenticación, autorización por roles y validaciones de negocio en las capas de controlador y servicio.

Las pruebas unitarias permiten verificar el comportamiento esperado del sistema, detectar errores y respaldar la calidad del software desarrollado.

La combinación de **Spring Security**, **MockMvc**, **Mockito** y **JaCoCo** permite demostrar que las funcionalidades principales fueron validadas correctamente.
