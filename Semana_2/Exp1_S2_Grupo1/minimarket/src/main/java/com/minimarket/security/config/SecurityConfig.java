package com.minimarket.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de seguridad para JWT.
 *
 * Cambios respecto a la version con sesiones:
 * - Se quito formLogin (ya no hay formulario de login, se usa endpoint REST)
 * - Se quito logout (el cliente simplemente descarta el token)
 * - Se agrego sessionManagement STATELESS (JWT no usa sesiones del servidor)
 * - Se agrego el JwtAuthenticationFilter antes del filtro de Spring
 * - Se agregaron rutas publicas para /api/auth/** (register y login)
 */
@Configuration
public class SecurityConfig {

    // Filtro JWT que valida el token en cada peticion
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deshabilitado: JWT no usa cookies, no es vulnerable a CSRF
                .csrf(csrf -> csrf.disable())

                // Reglas de autorizacion de rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios/**").hasAuthority("GERENTE")
                        .requestMatchers("/api/inventario/**").hasAnyAuthority("GERENTE", "EMPLEADO")
                        .requestMatchers("/api/productos/**").hasAnyAuthority("GERENTE", "EMPLEADO", "CLIENTE")
                        .requestMatchers("/api/auth/**").permitAll()  // Login y register sin autenticacion
                        .requestMatchers("/public/**").permitAll()     // Rutas publicas
                        .requestMatchers("/h2-console/**").permitAll() // Consola H2 para desarrollo
                        .anyRequest().authenticated()                  // Tod0 lo demas requiere token
                )

                // STATELESS: el servidor no crea ni mantiene sesiones
                // Cada peticion se valida con el token JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Agrega el filtro JWT ANTES del filtro de autenticacion de Spring
                // Asi el token se valida antes de que Spring intente autenticar por su cuenta
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager: necesario para autenticar en el endpoint de login.
     * El AuthController lo usa para validar username y password.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * BCrypt para cifrar contraseñas antes de guardarlas en BD.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
