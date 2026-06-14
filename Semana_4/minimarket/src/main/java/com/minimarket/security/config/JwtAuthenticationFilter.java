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
 * Filtro encargado de interceptar cada peticion HTTP para validar el token JWT.
 *
 * El filtro revisa el header Authorization. Si la peticion contiene un token
 * con formato Bearer, extrae el JWT, obtiene el username, carga los datos del
 * usuario desde la base de datos y valida que el token sea correcto.
 *
 * Si el token es valido, registra la autenticacion en el SecurityContext para
 * que Spring Security pueda aplicar las reglas de autorizacion por rol.
 *
 * Si la peticion no contiene token, continua el flujo sin autenticar. En ese
 * caso, Spring Security permitira o rechazara el acceso segun la configuracion
 * del endpoint solicitado.
 *
 * Se inyecta UserDetailsService en lugar de una implementacion concreta para
 * mantener bajo acoplamiento. Spring utiliza automaticamente
 * CustomUserDetailsService, ya que es la clase del proyecto que implementa
 * esta interfaz.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/auth/");
    }

    /**
     * Metodo principal del filtro. Se ejecuta una vez por cada peticion HTTP.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Busca el header Authorization en la peticion.
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Si no existe o no comienza con "Bearer ", continua sin autenticar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrae el token quitando el prefijo "Bearer ".
            String token = authHeader.substring(7);

            // Extrae el username desde el token JWT.
            String username = jwtService.extractUserName(token);

            // Si existe username y aun no hay autenticacion en el contexto,
            // se intenta autenticar al usuario para esta peticion.
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Carga los datos del usuario desde la base de datos.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                /*
                 * Valida que el token corresponda al usuario y que no este expirado.
                 * La firma del token se verifica al parsear sus claims en JwtService.
                 */
                if (jwtService.isTokenValid(token, userDetails)) {

                    // Crea la autenticacion con el usuario y sus roles.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Agrega detalles de la peticion, como IP y datos asociados al request.
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Registra la autenticacion en el contexto de seguridad.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (JwtException | IllegalArgumentException ex) {
            // Registra eventos sospechosos asociados a tokens invalidos o expirados.
            log.warn("Token JWT invalido o expirado. Ruta: {} | IP: {} | Error: {}",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"token jwt invalido o expirado\"}");
            return;
        }

        // Continua con el siguiente filtro de la cadena.
        filterChain.doFilter(request, response);
    }
}