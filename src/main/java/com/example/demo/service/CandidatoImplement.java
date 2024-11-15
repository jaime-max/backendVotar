package com.example.demo.service;

import com.example.demo.entity.Candidato;
import com.example.demo.repository.CandidatoRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CandidatoImplement implements CandidatoServices {

    @Autowired
    private CandidatoRepository candidatoRepository;

    private final String uploadDir = "uploads/";

    @Override
    public List<Candidato> ListarCandidatos() {

        return candidatoRepository.findAllByOrderByIdAsc();
    }

    @Override
    public Candidato registrarCandidato(Candidato candidato, MultipartFile file) throws IOException {
        String Filename = savePhoto(file);
        candidato.setFoto(Filename);
        return candidatoRepository.save(candidato);
    }

    @Override
    public Candidato buscarCandidatoPorId(Long id) {
        return candidatoRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidato no encontrado"));

    }

    @Override
    public Candidato actualizarCandidato(Candidato candidatoEdit, MultipartFile file) throws IOException {
        try {
            //Busca candidato existente por ID
            Candidato candidatoExistente = buscarCandidatoPorId(candidatoEdit.getId());
            //Actualiza los campos del candidato
            candidatoExistente.setNombre(candidatoEdit.getNombre());
            candidatoExistente.setApellido(candidatoEdit.getApellido());
            candidatoExistente.setCurso(candidatoEdit.getCurso());
            //Si se proporciona una foto la actualiza
            if(file != null && !file.isEmpty()) {
                String Filename = savePhoto(file);
                candidatoExistente.setFoto(Filename);
            }
            //Guarda y devuelve el candidato actualizado
            Candidato candidatoActualizado = candidatoRepository.save(candidatoExistente);
            // Mensaje de éxito al actualizar
            log.info("Candidato con ID {} actualizado exitosamente.", candidatoActualizado.getId());
            return candidatoActualizado;
        }catch (IOException e) {
            log.error("Error al guardar la foto del candidato: {}", e.getMessage());
            throw new IOException("No se pudo actualizar el candidato. Error al guardar la foto.", e);
        }
    }

    @Override
    public Candidato votarPorCandidato(Long id) {
        Candidato candidato = buscarCandidatoPorId(id);
        candidato.setVotos(candidato.getVotos() + 1);
        return candidatoRepository.save(candidato);
    }

    @Override
    public Integer TotalVotos() {
        List<Candidato> candidate = candidatoRepository.findAll();
        return candidate.stream().mapToInt(Candidato :: getVotos).sum();
    }

    @Override
    public void deleteCandidato(Long id) {
        try {
            buscarCandidatoPorId(id);
            candidatoRepository.deleteById(id);
            log.info("Candidato {} eliminado", id);
        }catch (Exception e) {
            log.error("Error al eliminar candidato", e);
        }

    }

    public String savePhoto(MultipartFile file) throws IOException {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;// Devuelve el nombre del archivo o su ruta para almacenarla en la base de datos
        }catch (IOException e) {
            log.error("Error al guardar el archivo: {}", e.getMessage());
            throw new IOException("No se pudo guardar la foto, por favor intenta nuevamente.", e);
        }
    }
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.error("Error al crear el directorio de cargas: {}", e.getMessage());// Maneja el error apropiadamente
        }
    }


}
