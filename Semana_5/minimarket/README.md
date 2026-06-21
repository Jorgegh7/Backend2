# MiniMarket Plus - Seguridad con JWT

Backend de MiniMarket Plus con autenticación y autorización implementada mediante JSON Web Tokens (JWT) y Spring Security.

## Tecnologías

- Java 17
- Spring Boot 3.4.1
- Spring Security
- JWT (jjwt 0.12.6)
- H2 Database (en memoria)
- Maven

## Estructura del Proyecto

```text
com.minimarket
├── controller
│   ├── AuthController              # Endpoints de registro y login
│   ├── CarritoController
│   ├── CategoriaController
│   ├── ProductoController
│   ├── UsuarioController
│   └── VentaController
├── entity
│   ├── Usuario                     # Entidad con username, password y roles
│   └── Rol                         # Entidad de roles (ADMIN, EMPLEADO, CLIENTE)
├── repository
│   ├── UsuarioRepository
│   └── RolRepository
└── security
    ├── config
    │   ├── SecurityConfig           # Configuración STATELESS con JWT
    │   └── JwtAuthenticationFilter  # Filtro que valida el token en cada petición
    ├── model
    │   ├── AuthRequest              # DTO para login
    │   ├── AuthResponse             # DTO de respuesta con token JWT
    │   ├── RegisterRequest          # DTO para registro con validaciones
    │   ├── CustomUserDetails        # Adaptador de Usuario para Spring Security
    │   ├── UsuarioResponse          # DTO de respuesta sin datos sensibles
    │   └── ApiMessageResponse       # DTO para mensajes genéricos
    └── service
        ├── JwtService               # Genera, valida y deserializa tokens JWT
        └── CustomUserDetailsService # Busca usuarios en BD para Spring Security
```

## Flujo de Autenticación JWT

```text
1. REGISTRO
   POST /api/auth/register
   → Recibe username, password y roles
   → Valida datos de entrada
   → Cifra password con BCrypt
   → Guarda usuario en BD

2. LOGIN
   POST /api/auth/login
   → Recibe username y password
   → Valida credenciales
   → Genera token JWT
   → Devuelve token al cliente

3. ACCESO A RECURSOS PROTEGIDOS
   GET /api/productos
   Header: Authorization: Bearer <token>
   → JwtAuthenticationFilter intercepta la petición
   → Extrae y valida el token
   → Si es válido, marca al usuario como autenticado
   → Spring Security autoriza o bloquea según el rol
```

## Configuración

### application.properties

```properties
# Base de datos H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.defer-datasource-initialization=true

# JWT
security.jwt.secret=minimarket-backend2-jwt-secret-key-super-segura-2026-demo
security.jwt.expiration-seconds=3600

# Logs del sistema
logging.file.name=logs/minimarket-security.log
```

### Dependencias JWT y seguridad

El proyecto utiliza Spring Security para autenticación y autorización, y JJWT para generar, firmar y validar tokens JWT.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
</dependency>
```

## Endpoints de Autenticación

### Registro de usuario

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
    "username": "admin",
    "password": "123456",
    "roles": [
        { "nombre": "ADMIN" }
    ]
}
```

Respuesta 200:

```json
{
    "message": "Usuario registrado exitosamente"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
    "username": "admin",
    "password": "123456"
}
```

Respuesta 200:

```json
{
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "type": "Bearer",
    "expiresIn": 3600,
    "username": "admin",
    "roles": ["ADMIN"]
}
```

### Acceso a endpoint protegido

```http
GET /api/productos
Authorization: Bearer <token>
```

Respuesta 200:

```json
[
    {
        "id": 1,
        "nombre": "Producto de ejemplo"
    }
]
```

## Autorización por Roles

Los endpoints están protegidos según el rol del usuario:

| Endpoint | Roles permitidos |
|---|---|
| `/api/auth/**` | Público, sin token |
| `/api/usuarios/**` | `ADMIN` |
| `/api/inventario/**` | `ADMIN`, `EMPLEADO` |
| `GET /api/productos/**` | `ADMIN`, `EMPLEADO`, `CLIENTE` |
| `POST /api/productos/**` | `ADMIN`, `EMPLEADO` |
| `PUT /api/productos/**` | `ADMIN`, `EMPLEADO` |
| `DELETE /api/productos/**` | `ADMIN` |
| `POST /api/ventas/**` | `ADMIN`, `EMPLEADO`, `CLIENTE` |
| `GET /api/ventas/**` | `ADMIN`, `EMPLEADO` |
| `/public/**` | Público, sin token |
| Cualquier otro endpoint | Requiere autenticación |

## Roles Disponibles

Los roles se cargan automáticamente al iniciar la aplicación mediante `data.sql`:

- `ADMIN`
- `EMPLEADO`
- `CLIENTE`

## Seguridad Implementada

- Contraseñas cifradas con `BCryptPasswordEncoder`.
- Sesiones `STATELESS`: el servidor no mantiene sesiones de usuario.
- CSRF deshabilitado porque la autenticación se realiza mediante JWT y no mediante cookies de sesión.
- Filtro JWT que intercepta solicitudes protegidas y valida el token antes de llegar al controller.
- Tokens firmados con clave secreta configurada en `application.properties`.
- Tokens con expiración de 1 hora.
- Validación de entrada en `RegisterRequest`, incluyendo restricción del campo `username` para reducir riesgo de XSS.
- Registro básico de eventos sospechosos, como tokens inválidos o expirados, almacenados en consola y en el archivo `logs/minimarket-security.log`.

## Pruebas de seguridad realizadas

Se realizaron pruebas con Postman para validar:

- Acceso sin token a endpoint protegido: respuesta `401 Unauthorized`.
- Acceso con rol insuficiente: respuesta `403 Forbidden`.
- Acceso con rol permitido: respuesta `200 OK`.
- Token JWT inválido: respuesta `401 Unauthorized` y registro del evento en consola y en el archivo `logs/minimarket-security.log`.
- Intento de SQL Injection en login: autenticación rechazada.
- Intento de XSS en registro: solicitud rechazada con `400 Bad Request`.

## Cómo ejecutar

```bash
git clone https://github.com/PedroBreit/DUOC-DBII002.git
cd DUOC-DBII002/Semana_3/Exp1_S3_Grupo1/minimarket
mvn spring-boot:run
```

La aplicación estará disponible en:

```text
http://localhost:8080
```

## Repositorio

```text
https://github.com/PedroBreit/DUOC-DBII002
```