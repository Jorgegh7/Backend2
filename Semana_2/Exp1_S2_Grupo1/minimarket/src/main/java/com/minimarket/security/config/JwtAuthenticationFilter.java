package com.minimarket.security.config;

import com.minimarket.security.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta cada peticion HTTP para validar el token JWT.
 * Extiende OncePerRequestFilter para garantizar que se ejecuta
 * una sola vez por peticion.
 *
 * Funciona como un guardia en la puerta de un edificio:
 * - Alguien llega -> el guardia le pide su credencial (header Authorization)
 * - Si no trae credencial -> lo deja pasar pero sin acceso a areas restringidas
 * - Si trae credencial -> la revisa
 * - Si la credencial es valida -> lo marca como "autorizado" y lo deja entrar
 * - Si la credencial es falsa -> lo rechaza con error 401
 *
 * Se inyecta UserDetailsService (interfaz) en lugar de CustomUserDetailsService
 * (implementacion) por buena practica. Es el mismo patron que se usa en los
 * controllers con InscripcionService en vez de InscripcionServiceImpl.
 * El filtro solo necesita el metodo loadUserByUsername(), no le importa
 * de donde vienen los usuarios. Spring inyecta automaticamente
 * CustomUserDetailsService porque es la unica clase que implementa
 * esa interfaz en el proyecto.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Se usa la interfaz UserDetailsService por flexibilidad.
    // Spring inyecta CustomUserDetailsService automaticamente.
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Metodo principal del filtro. Se ejecuta en cada peticion.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Busca el header Authorization en la peticion
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. Si no existe o no empieza con "Bearer ", deja pasar sin autenticar
        //    La peticion sigue pero sin usuario autenticado en el SecurityContext
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extrae el token quitando "Bearer " del inicio (7 caracteres)
            String token = authHeader.substring(7);

            // 4. Usa JwtService para extraer el username del token
            String username = jwtService.extractUserName(token);

            // 5. Si hay username Y no hay autenticacion previa en el contexto
            //    (evita autenticar dos veces en la misma peticion)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Busca el usuario en la BD a traves de UserDetailsService
                //    Internamente llama a CustomUserDetailsService.loadUserByUsername()
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 7. Valida que el token sea legitimo (firma + expiracion + username)
                if (jwtService.isTokenValid(token, userDetails)) {

                    // 8. Crea el objeto de autenticacion con el usuario y sus roles
                    //    credentials es null porque ya se valido con JWT, no con password
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Agrega detalles de la peticion (IP, session, etc)-Informacion adicional
                    //El usuario ya está auntenticado con el paso 8.
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Marca al usuario como autenticado en el SecurityContext
                    //    A partir de aqui Spring Security sabe quien es el usuario
                    //    y que permisos tiene para el resto de la peticion
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            // Si el token es invalido, expirado o corrupto, devuelve error 401
        } catch (JwtException | IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"token jwt invalido\"}");
            return;
        }

        // Continua con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}