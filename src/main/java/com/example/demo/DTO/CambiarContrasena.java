package com.example.demo.DTO;

import lombok.Data;

@Data
public class CambiarContrasena {
    private Long idUsuario;       // ID del usuario que quiere cambiar la contraseña
    private String contrasenaAntigua; // Contraseña actual del usuario
    private String nuevaContrasena;   // Nueva contraseña
    private String RepetirNuevaContrasena;
}
