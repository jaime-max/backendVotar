package com.example.demo.service;

import com.example.demo.entity.Usuario;
import com.example.demo.entity.Votante;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VotanteService {
    void cargarVotantesExcel(MultipartFile file) throws IOException;

    boolean verificarVotante(String cedula);
    List<Votante> getVotantes();
    void deleteVotante(Long id);
    Votante buscarVotantePorId(Long id);
    boolean todosHanVotado();
}
