package com.minimarket.controller;

import com.minimarket.security.model.ApiMessageResponse;
import com.minimarket.security.model.AuthRequest;
import com.minimarket.security.model.AuthResponse;
import com.minimarket.security.model.RegisterRequest;
import com.minimarket.security.service.JwtService;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller de autenticacion.
 * Maneja el registro de nuevos usuarios y el login.
 * Las rutas /api/auth/** estan configuradas como permitAll()
 * en SecurityConfig, asi que no requieren token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Endpoint de registro de nuevos usuarios.
     * Recibe username, password y roles.
     * Valida los datos recibidos mediante @Valid.
     * Cifra la contraseña con BCrypt antes de guardar en BD.
     *
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        // Verifica que el username no exista
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new ApiMessageResponse("El username ya existe"));
        }

        // Crea el nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); // Cifra la contraseña

        // Busca los roles en la BD y los asigna
        Set<Rol> roles = new HashSet<>();
        for (Rol rol : request.getRoles()) {
            Rol rolDb = rolRepository.findByNombre(rol.getNombre())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rol.getNombre()));
            roles.add(rolDb);
        }
        usuario.setRoles(roles);

        // Guarda el usuario en la BD
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new ApiMessageResponse("Usuario registrado exitosamente"));
    }

    /**
     * Endpoint de login.
     * Recibe username y password, valida las credenciales
     * y devuelve un token JWT.
     *
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Autentica con username y password usando AuthenticationManager
            // Internamente usa CustomUserDetailsService para buscar el usuario
            // y PasswordEncoder para comparar la contraseña
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Si llega aqui, las credenciales son correctas
            // Extrae el UserDetails del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Genera el token JWT con los datos del usuario
            String token = jwtService.generateToken(userDetails);

            // Extrae los roles como lista de Strings para la respuesta
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(auth -> auth.getAuthority())
                    .toList();

            // Arma la respuesta con el token y datos del usuario
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setType("Bearer");
            response.setExpiresIn(jwtService.getExpirationSeconds());
            response.setUsername(userDetails.getUsername());
            response.setRoles(roles);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(401)
                    .body(new ApiMessageResponse("Credenciales invalidas"));
        }
    }
}