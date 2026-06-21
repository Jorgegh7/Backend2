package com.minimarket.security.service;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementacion de UserDetailsService de Spring Security.
 *
 * UserDetailsService es una interfaz de Spring Security que tiene
 * un solo metodo: loadUserByUsername(). Su proposito es decirle a
 * Spring Security DONDE y COMO buscar los usuarios.
 *
 * Spring Security no sabe donde estan los usuarios (pueden estar
 * en memoria, base de datos, LDAP, etc). Esta clase le dice:
 * "los usuarios estan en la base de datos, los busco con JPA
 * y te los devuelvo envueltos en CustomUserDetails".
 *
 * Flujo:
 * 1. El filtro JWT extrae el username del token
 * 2. Llama a loadUserByUsername() de esta clase
 * 3. Esta clase busca el Usuario en la BD con UsuarioRepository
 * 4. Si existe, lo envuelve en CustomUserDetails y lo devuelve
 * 5. Si no existe, lanza UsernameNotFoundException
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Busca un usuario por username en la base de datos.
     * Es el unico metodo de la interfaz UserDetailsService.
     *
     * @param username el nombre de usuario a buscar
     * @return UserDetails con los datos del usuario (username, password, roles)
     * @throws UsernameNotFoundException si el usuario no existe en la BD
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Lo envuelve en CustomUserDetails para que Spring Security pueda trabajar con el
        return new CustomUserDetails(usuario);
    }
}