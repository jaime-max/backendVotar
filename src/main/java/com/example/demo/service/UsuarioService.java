package com.example.demo.service;

import com.example.demo.DTO.LoginResponse;
import com.example.demo.entity.Usuario;

import java.util.List;

public interface UsuarioService {
    List<Usuario> ListUser();
    Usuario registrarUser(Usuario usuario);
    LoginResponse login(String username, String password);
    Usuario actualizarUsuario(Usuario usuario);
    Usuario buscarUsuarioPorId(Long id);
    void eliminarUsuario(Long id);
}
