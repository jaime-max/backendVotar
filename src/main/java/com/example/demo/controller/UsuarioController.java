package com.example.demo.controller;

import com.example.demo.DTO.CambiarContrasena;
import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.LoginResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")

@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> ListUser() {

        return usuarioService.ListUser();
    }

    @PostMapping
    public ResponseEntity <Usuario> registrarUser(@RequestBody Usuario usuario) {
        try {
            Usuario usuarioGuardado = usuarioService.registrarUser(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
        }catch (Exception e) {
            log.error("Error al registrar usuario: {}",e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity <Usuario> editarUser(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            usuario.setId(id);
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(usuario);
            if (usuarioActualizado != null) {
                // Si el nombre de usuario cambió, indicar que se debe cerrar la sesión
                boolean nombreChanged = !usuario.getNombre().equals(usuarioActualizado.getNombre());
                if (nombreChanged) {
                    return ResponseEntity.status(HttpStatus.OK).body(usuarioActualizado);
                }
                return ResponseEntity.ok(usuarioActualizado);
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }catch (Exception e) {
            log.error("Error al actualizar usuario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }
    @DeleteMapping("/{id}")
    public void eliminarUser(@PathVariable Long id) {
            usuarioService.eliminarUsuario(id);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = usuarioService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cambiarContrasena")
    public ResponseEntity<Usuario> cambiarContrasena(@RequestBody CambiarContrasena cambioContrasenaDTO) {
        Usuario usuarioActualizado = usuarioService.cambiarContrasena(cambioContrasenaDTO);
        if (usuarioActualizado != null) {
            return ResponseEntity.ok(usuarioActualizado);
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }
    // En UsuarioController
    @GetMapping("/idPorNombre/{nombre}")
    public Long obtenerIdPorNombre(@PathVariable String nombre) {
        Usuario usuario = usuarioService.buscarUsuarioPorNombre(nombre);
        return usuario.getId(); // Retorna el ID del usuario
    }
}
