package com.minimarket.security.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // JWT no usa cookies de sesion, por eso se deshabilita CSRF.
            .csrf(csrf -> csrf.disable())

            // JWT trabaja sin sesiones del lado del servidor.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Manejo claro de errores:
            // 401 cuando no hay autenticacion valida.
            // 403 cuando el usuario esta autenticado, pero no tiene permisos.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )

            .authorizeHttpRequests(auth -> auth

                // Endpoints publicos de autenticacion.
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()

                // Consola H2 solo para desarrollo/pruebas.
                .requestMatchers("/h2-console/**").permitAll()

                // Usuarios: solo administrador.
                .requestMatchers("/api/usuarios", "/api/usuarios/**")
                    .hasAuthority("ADMIN")

                // Productos:
                // Consultar productos: cliente, empleado y administrador.
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**")
                    .hasAnyAuthority("ADMIN", "EMPLEADO", "CLIENTE")

                // Crear productos: empleado y administrador.
                .requestMatchers(HttpMethod.POST, "/api/productos", "/api/productos/**")
                    .hasAnyAuthority("ADMIN", "EMPLEADO")

                // Editar productos: empleado y administrador.
                .requestMatchers(HttpMethod.PUT, "/api/productos", "/api/productos/**")
                    .hasAnyAuthority("ADMIN", "EMPLEADO")

                // Eliminar productos: solo administrador.
                .requestMatchers(HttpMethod.DELETE, "/api/productos", "/api/productos/**")
                    .hasAuthority("ADMIN")

                // Inventario: empleado y administrador.
                .requestMatchers("/api/inventario", "/api/inventario/**")
                    .hasAnyAuthority("ADMIN", "EMPLEADO")

                // Ventas:
                // Registrar venta: cliente, empleado y administrador.
                .requestMatchers(HttpMethod.POST, "/api/ventas", "/api/ventas/**")
                    .hasAnyAuthority("ADMIN", "EMPLEADO", "CLIENTE")

                // Consultar ventas: empleado y administrador.
                .requestMatchers(HttpMethod.GET, "/api/ventas", "/api/ventas/**")
                    .hasAnyAuthority("ADMIN", "EMPLEADO")

                // Cualquier otra ruta requiere autenticacion.
                .anyRequest().authenticated()
            )

            // Filtro JWT antes del filtro estandar de autenticacion de Spring.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // Permite usar H2 Console en navegador durante desarrollo.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            // Respuesta cuando no hay token o la autenticacion no es valida.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"No autenticado. Debe enviar un token JWT valido.\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Respuesta cuando el usuario esta autenticado, pero no tiene permisos.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Acceso denegado. No tiene permisos para este recurso.\"}");
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}