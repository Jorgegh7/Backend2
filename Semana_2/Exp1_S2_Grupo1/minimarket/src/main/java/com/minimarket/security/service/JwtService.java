package com.minimarket.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Servicio encargado de la gestion completa de tokens JWT.
 * Responsabilidades:
 * - Crear (generar) tokens firmados
 * - Leer (deserializar) tokens para extraer datos internos
 * - Validar tokens (firma y expiracion)
 */
@Service
public class JwtService {

    // Llave secreta para firmar y validar tokens
    private final SecretKey secretKey;

    // Tiempo de expiracion del token en segundos
    private final long expirationSeconds;

    /**
     * Constructor que lee la configuracion desde application.properties
     */
    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds}") long expirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Genera un token JWT para el usuario autenticado.
     * Incluye: username (subject), fecha de creacion, expiracion y roles.
     */
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();

        // Extrae los roles del usuario y los convierte a List<String>
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())                          // quien es el dueño del token
                .issuedAt(Date.from(now))                                   // cuando se creo
                .expiration(Date.from(now.plusSeconds(expirationSeconds))) // cuando expira
                .claim("roles", roles)                                // roles como dato extra
                .signWith(secretKey)                                     // firma con la clave secreta
                .compact();                                             // genera el String JWT
    }

    /**
     * Extrae el username (subject) del token.
     */
    public String extractUserName(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae la lista de roles del token.
     * Valida que el claim "roles" sea una lista antes de retornarlo.
     */
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");

        if (roles instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    /**
     * Valida que el token sea legitimo:
     * - El username del token coincide con el del UserDetails
     * - El token no ha expirado
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Retorna el tiempo de expiracion configurado en segundos.
     */
    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    /**
     * Verifica si el token ya expiro comparando la fecha de expiracion
     * con la fecha actual.
     */
    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new java.util.Date());
    }

    /**
     * Metodo base que deserializa el token JWT.
     * Verifica la firma con la clave secreta y extrae el payload (Claims).
     * Todos los metodos extract usan este metodo internamente.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)      // verifica que la firma sea valida
                .build()
                .parseSignedClaims(token)   // parsea el token firmado
                .getPayload();              // extrae el contenido (claims)
    }
}