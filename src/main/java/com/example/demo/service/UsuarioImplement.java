package com.example.demo.service;

import com.example.demo.DTO.LoginResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UsuarioImplement implements UsuarioService, UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy
    private JWTUtil jwtUtil;

    private final Argon2PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioImplement(Argon2PasswordEncoder passwordEncoder) {

        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Usuario> ListUser() {

        return usuarioRepository.findAllByOrderByIdAsc();
    }

    @Override
    public Usuario registrarUser(Usuario usuario) {
        // Cifrar la contraseña antes de guardarla
        String hashedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(hashedPassword);
        // Asignar correctamente el valor de isAdmin
        usuario.setEsAdministrador(usuario.isEsAdministrador());
        // Depuración para verificar el valor de isAdmin
        log.info("isAdmin recibido: {}", usuario.isEsAdministrador());
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario con ID {} registrado exitosamente.", usuarioGuardado.getId());
        return usuarioGuardado;
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuarioNuevo) {
        try {
            Usuario usuarioActual = buscarUsuarioPorId(usuarioNuevo.getId());
            usuarioActual.setNombre(usuarioNuevo.getNombre());
            usuarioActual.setApellido(usuarioNuevo.getApellido());
            usuarioActual.setEmail(usuarioNuevo.getEmail());
            usuarioActual.setTelefono(usuarioNuevo.getTelefono());
            if (usuarioNuevo.getPassword() != null) {
                usuarioActual.setPassword(passwordEncoder.encode(usuarioNuevo.getPassword()));
            }
            Usuario usuarioActualizado = usuarioRepository.save(usuarioActual);
            // Mensaje de éxito al actualizar
            log.info("Usuario con ID {} actualizado exitosamente.", usuarioActualizado.getId());
            return usuarioActualizado;
        }catch (Exception e) {
            log.error("Error al actualizar al usuario: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public LoginResponse login(String username, String password) {
        // Intentar encontrar al usuario por nombre
        Usuario usuario = usuarioRepository.findByNombre(username).orElse(null);

        // Depuración: Si el usuario no se encuentra, imprimir mensaje
        if (usuario == null) {
            System.out.println("Usuario no encontrado con el nombre: " + username); // Mensaje de depuración
            return new LoginResponse("Credenciales inválidas");  // Responder con un mensaje genérico
        }
        // Depuración: Si el usuario es encontrado, mostrar el nombre
        System.out.println("Usuario encontrado: " + usuario.getNombre()); // Mensaje de depuración

        // Verificar la contraseña con Argon2PasswordEncoder
        if (passwordEncoder.matches(password, usuario.getPassword())) {
            System.out.println("Contraseña correcta, generando token..."); // Mensaje de depuración
            // Generación del token JWT
            String token = jwtUtil.create(usuario.getId().toString(), usuario.getNombre());
            System.out.println("Token generado: " + token); // Mensaje de depuración
            return new LoginResponse(token);  // Retornar el token
        } else {
            System.out.println("Contraseña incorrecta"); // Mensaje de depuración
            return new LoginResponse("Contraseña incorrecta"); // Responder con mensaje de error
        }
    }

    @Override
    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Override
    public void eliminarUsuario(Long id) {
        try {
            Usuario usuario = buscarUsuarioPorId(id);
            usuarioRepository.delete(usuario);
            log.info("Usuario con ID {} eliminado exitosamente.", id);
        }catch (Exception e) {
            log.error("Error al eliminar usuario: {}", e.getMessage());
        }
    }

    // Implementación de UserDetailsService para la autenticación de Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombre(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Adaptación del usuario a UserDetails con rol de ADMIN si isAdmin es true
        return new org.springframework.security.core.userdetails.User(
                usuario.getNombre(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
        );
    }
}
