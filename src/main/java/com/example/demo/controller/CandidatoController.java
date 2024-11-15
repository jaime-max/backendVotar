package com.example.demo.controller;

import com.example.demo.entity.Candidato;
import com.example.demo.service.CandidatoServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/candidatos")
@CrossOrigin(origins = "http://localhost:5173")
public class CandidatoController {

    @Autowired
    private CandidatoServices candidatoServices;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Candidato> obtenerCandidato() {

        return candidatoServices.ListarCandidatos();
    }
    @GetMapping("/{id}")
    public Candidato obtenerCandidatoPorId(@PathVariable Long id) {
        return candidatoServices.buscarCandidatoPorId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Candidato guardarCandidato(@RequestParam("candidato")String candidatoJson,
                                      @RequestParam("file") MultipartFile file) throws IOException {
        // Convierte el JSON en un objeto Candidato
        Candidato candidato = new ObjectMapper().readValue(candidatoJson, Candidato.class);
        if(file.isEmpty()) {
            log.error("El archivo no puede ser nulo");
            throw  new IllegalArgumentException("La foto es obligatoria para el registro");
        }
        return candidatoServices.registrarCandidato(candidato,file);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Candidato editarCandidato(@PathVariable Long id,
                                     @RequestParam("candidato") String candidatoJson,
                                     @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        // Convierte el JSON en un objeto Candidato
        Candidato candidato = new ObjectMapper().readValue(candidatoJson, Candidato.class);
        candidato.setId(id);
        return candidatoServices.actualizarCandidato(candidato,file);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminateCandidate(@PathVariable Long id) {
        candidatoServices.deleteCandidato(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/total-votos")
    public Integer obtenerTotalVotos(){
        return candidatoServices.TotalVotos();
    }

    @PostMapping("/votar/{id}")
    public Candidato votarCandidato(@PathVariable Long id){
        return candidatoServices.votarPorCandidato(id);
    }

}
