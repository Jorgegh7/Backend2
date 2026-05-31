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

```
com.minimarket
├── controller
│   ├── AuthController            # Endpoints de registro y login
│   ├── CarritoController
│   ├── CategoriaController
│   ├── ProductoController
│   ├── UsuarioController
│   └── VentaController
├── entity
│   ├── Usuario                   # Entidad con username, password y roles
│   └── Rol                       # Entidad de roles (ADMIN, EMPLEADO, CLIENTE, GERENTE)
├── repository
│   ├── UsuarioRepository
│   └── RolRepository
└── security
    ├── config
    │   ├── SecurityConfig         # Configuración STATELESS con JWT
    │   └── JwtAuthenticationFilter # Filtro que valida el token en cada petición
    ├── model
    │   ├── AuthRequest            # DTO para login (username, password)
    │   ├── AuthResponse           # DTO de respuesta con token JWT
    │   ├── RegisterRequest        # DTO para registro de usuarios
    │   ├── CustomUserDetails      # Adaptador de Usuario para Spring Security
    │   ├── UsuarioResponse        # DTO de respuesta sin datos sensibles
    │   └── ApiMessageResponse     # DTO para mensajes genéricos
    └── service
        ├── JwtService             # Genera, valida y deserializa tokens JWT
        └── CustomUserDetailsService # Busca usuarios en BD para Spring Security
```

## Flujo de Autenticación JWT

```
1. REGISTRO
   POST /api/auth/register
   → Recibe username, password, roles
   → Cifra password con BCrypt
   → Guarda usuario en BD

2. LOGIN
   POST /api/auth/login
   → Recibe username, password
   → Valida credenciales
   → Genera token JWT
   → Devuelve token al cliente

3. ACCESO A RECURSOS PROTEGIDOS
   GET /api/productos (con header Authorization: Bearer <token>)
   → JwtAuthenticationFilter intercepta la petición
   → Extrae y valida el token
   → Si es válido, marca al usuario como autenticado
   → El endpoint responde normalmente
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
security.jwt.secret=miClaveSecretaSuperLargaYSegura1234567890abcdef
security.jwt.expiration-seconds=3600
```

### Dependencias JWT (pom.xml)

```xml
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

```
POST /api/auth/register
Content-Type: application/json

{
    "username": "admin",
    "password": "123456",
    "roles": [
        { "nombre": "GERENTE" }
    ]
}

Respuesta 200:
{
    "message": "Usuario registrado exitosamente"
}
```

### Login

```
POST /api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "123456"
}

Respuesta 200:
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 3600,
    "username": "admin",
    "roles": ["GERENTE"]
}
```

### Acceso a endpoint protegido

```
GET /api/productos
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Respuesta 200:
[ ... lista de productos ... ]
```

## Autorización por Roles

Los endpoints están protegidos según el rol del usuario:

| Endpoint | Roles permitidos |
|---|---|
| `/api/auth/**` | Público (sin token) |
| `/api/usuarios/**` | GERENTE |
| `/api/inventario/**` | GERENTE, EMPLEADO |
| `/api/productos/**` | GERENTE, EMPLEADO, CLIENTE |
| `/public/**` | Público (sin token) |
| Cualquier otro | Requiere autenticación |

## Roles Disponibles

Los roles se cargan automáticamente al iniciar la aplicación mediante `data.sql`:

- ADMIN
- EMPLEADO
- CLIENTE
- GERENTE

## Seguridad Implementada

- **Contraseñas cifradas** con BCryptPasswordEncoder
- **Sesiones STATELESS**: el servidor no mantiene sesiones, cada petición se valida con el token
- **CSRF deshabilitado**: no es necesario con JWT porque no se usan cookies de sesión
- **Filtro JWT**: intercepta cada petición y valida el token antes de llegar al controller
- **Tokens firmados** con HMAC-SHA256 usando clave secreta configurada en properties
- **Tokens con expiración**: 1 hora por defecto, configurable

## Cómo ejecutar

```bash
git clone <url-del-repositorio>
cd minimarket
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`
