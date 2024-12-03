package com.example.demo.service;

import com.example.demo.DTO.CambiarContrasena;
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
    private EmailService emailService;

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

            // Verificar si el nombre de usuario ha cambiado
            boolean nombreChanged = !usuarioActual.getNombre().equals(usuarioNuevo.getNombre());

            usuarioActual.setNombre(usuarioNuevo.getNombre());
            usuarioActual.setApellido(usuarioNuevo.getApellido());
            usuarioActual.setEmail(usuarioNuevo.getEmail());
            usuarioActual.setTelefono(usuarioNuevo.getTelefono());

            if (usuarioNuevo.getPassword() != null) {
                usuarioActual.setPassword(passwordEncoder.encode(usuarioNuevo.getPassword()));
            }

            Usuario usuarioActualizado = usuarioRepository.save(usuarioActual);

            // Si el nombre de usuario cambió, indicamos que se debe cerrar la sesión
            if (nombreChanged) {
                log.info("Nombre de usuario cambiado. La sesión debe cerrarse.");
                // Aquí podríamos enviar algún tipo de señal al frontend para invalidar el token
            }
            log.info("Usuario con ID {} actualizado exitosamente.", usuarioActualizado.getId());
            return usuarioActualizado;
        }catch (Exception e) {
            log.error("Error al actualizar al usuario: {}", e.getMessage());
            return null;
        }
    }


    @Override
    public LoginResponse login(String username, String password) {
        // Verificar si el nombre de usuario es válido
        if (username == null || username.isEmpty()) {
            return new LoginResponse(null, "Usuario incorrecto");
        }
        log.info("Iniciando sesión para el usuario: {}", username);
        // Busca al usuario en la base de datos por su nombre de usuario.
        Usuario usuario = usuarioRepository.findByNombre(username).orElse(null);

        // Caso 1: El usuario no existe en la base de datos.
        if (usuario == null) {
            // Verificar si también la contraseña es incorrecta para este caso.
            if (password == null || password.isEmpty()) {
                return new LoginResponse(null, "Usuario y contraseña incorrectos");
            }
            return new LoginResponse(null, "Usuario incorrecto");
        }

        // Caso 2: El usuario existe, pero la contraseña es incorrecta.
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return new LoginResponse(null, "Contraseña incorrecta");
        }

        // Caso 3: Usuario y contraseña correctos.
        String token = jwtUtil.create(usuario.getId().toString(), usuario.getNombre());
        return new LoginResponse(token, null); // Retorna el token generado y ningún mensaje de error.
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


    @Override
    public Usuario cambiarContrasena(CambiarContrasena cambioContrasenaDTO) {
        try {
            // Buscar el usuario por el ID
            Usuario usuario = usuarioRepository.findById(cambioContrasenaDTO.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Verificar si la contraseña antigua es correcta
            if (!passwordEncoder.matches(cambioContrasenaDTO.getContrasenaAntigua(), usuario.getPassword())) {
                throw new IllegalArgumentException("La contraseña antigua no es correcta.");
            }
            // Validar que la nueva contraseña y la repetición coincidan
            if (!cambioContrasenaDTO.getNuevaContrasena().equals(cambioContrasenaDTO.getRepetirNuevaContrasena())) {
                throw new IllegalArgumentException("La nueva contraseña y la repetición no coinciden.");
            }

            // Establecer la nueva contraseña
            usuario.setPassword(passwordEncoder.encode(cambioContrasenaDTO.getNuevaContrasena()));

            // Guardar al usuario con la nueva contraseña
            Usuario usuarioActualizado = usuarioRepository.save(usuario);

            // Enviar correo de confirmación
            String subject = "Cambio de contraseña exitoso";
            String text = "Hola " + usuario.getNombre() + ",\n\nTu contraseña ha sido cambiada exitosamente.";
            emailService.enviarCorreo(usuario.getEmail(), subject, text);

            return usuarioActualizado;
        } catch (Exception e) {
            // Manejo de errores si la contraseña antigua no coincide o el usuario no se encuentra
            return null;
        }
    }

    @Override
    public Usuario buscarUsuarioPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }


    // Implementación de UserDetailsService para la autenticación de Spring Security
    @Override
    public UserDetails loadUserByUsername(String nombre) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombre(nombre)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Adaptación del usuario a UserDetails con rol de ADMIN si isAdmin es true
        return new org.springframework.security.core.userdetails.User(
                usuario.getNombre(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
        );
    }
}
