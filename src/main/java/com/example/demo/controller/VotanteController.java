package com.example.demo.controller;


import com.example.demo.entity.Votante;
import com.example.demo.repository.VotanteRepository;
import com.example.demo.service.VotanteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/votantes")
@CrossOrigin(origins = "http://localhost:5173")
public class VotanteController {
    @Autowired
    private VotanteService votanteService;

    @Autowired
    private VotanteRepository votanteRepository;

    @GetMapping
    public List<Votante> ListVotante() {
        return votanteService.getVotantes();
    }
    @DeleteMapping("/{id}")
    public void DeleteVotante(@PathVariable Long id) {
        votanteService.deleteVotante(id);
    }

    @GetMapping("/completa")
    public ResponseEntity<Map<String, String>> verificarVotacionCompleta() {
        Map<String, String> response = new HashMap<>();
        if (votanteService.todosHanVotado()) {
            response.put("status", "success");
            response.put("message", "Votación completa.");
            return ResponseEntity.ok(response); // HTTP 200 OK
        } else {
            response.put("status", "pending");
            response.put("message", "El proceso de votación no ha terminado.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response); // HTTP 202 ACCEPTED
        }
    }


    @PostMapping("/voto/validar")
    public ResponseEntity<Map<String, String>> validarVotante(@RequestBody Map<String, String> payload) {
        String cedulaDoc = payload.get("cedula");
        Map<String, String> response = new HashMap<>();

        // Verificar si el votante está autorizado
        if (!votanteService.verificarVotante(cedulaDoc)) {
            response.put("status", "error");
            response.put("message", "Credenciales Incorrectas.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // Verificar si el votante existe
        Votante votante = votanteRepository.findByCedula(cedulaDoc)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Votante no encontrado"));

        // Verificar si el votante ya ha votado
        if (votante.isVotado()) {
            response.put("status", "error");
            response.put("message", "Ya has votado, no puedes votar nuevamente.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Si pasa todas las validaciones
        response.put("status", "success");
        response.put("message", "El votante está autorizado para votar.");
        return ResponseEntity.ok(response);
    }


}
