package com.example.demo.service;

import com.example.demo.entity.Candidato;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CandidatoServices {
    List<Candidato> ListarCandidatos();
    Candidato registrarCandidato(Candidato candidato, MultipartFile file) throws IOException;
    Candidato buscarCandidatoPorId(Long id);
    Candidato actualizarCandidato(Candidato candidato, MultipartFile file) throws IOException;
    void votarPorCandidato(Long id, String cedulaDocument);
    Integer TotalVotos();
    void deleteCandidato(Long id);
}
