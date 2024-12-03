package com.example.demo.service;



import com.example.demo.entity.Votante;
import com.example.demo.repository.VotanteRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class VotantesServiceImplement implements VotanteService {
    @Autowired
    private VotanteRepository votanteRepository;

    @Override
    public void cargarVotantesExcel(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }

        if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls")) {
            throw new IllegalArgumentException("Solo se permiten archivos Excel (.xlsx o .xls).");
        }

        Set<String> cedulasEnArchivo = new HashSet<>(); // Para almacenar cédulas ya procesadas en el archivo

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Tomar la primera hoja

            // Iterar sobre las filas del archivo Excel
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar la cabecera

                Cell cedulaCell = row.getCell(0); // Primera columna: Cédula
                Cell nombreCell = row.getCell(1); // Segunda columna: Nombre

                if (cedulaCell != null && nombreCell != null) {
                    // Usar DataFormatter para obtener el valor como String
                    String cedula = new DataFormatter().formatCellValue(cedulaCell).trim();
                    String nombre = new DataFormatter().formatCellValue(nombreCell).trim();

                    if (!cedula.isEmpty() && !nombre.isEmpty()) {
                        // Verificar si la cédula tiene exactamente 10 dígitos
                        if (!cedula.matches("\\d{10}")) {
                            throw new IllegalArgumentException("La cédula " + cedula + " no es válida. Debe tener 10 dígitos.");
                        }

                        // Verificar si la cédula ya está en el archivo para evitar duplicados en el Excel
                        if (cedulasEnArchivo.contains(cedula)) {
                            throw new IllegalArgumentException("El archivo contiene cédulas duplicadas. Por favor, verifique el archivo.");
                        }
                        cedulasEnArchivo.add(cedula);

                        // Verificar si la cédula ya existe en la base de datos
                        if (votanteRepository.findByCedula(cedula).isPresent()) {
                            throw new IllegalArgumentException("La cédula " + cedula + " ya existe en la base de datos.");
                        }

                        // Guardar el votante si no hay duplicados ni cédulas inválidas
                        Votante votante = new Votante();
                        votante.setCedula(cedula);
                        votante.setNombre(nombre);
                        votante.setVotado(false);
                        votanteRepository.save(votante);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Error al procesar el archivo Excel.", e);
        }
    }


    @Override
    public boolean verificarVotante(String cedula) {
        Optional<Votante> votante = votanteRepository.findByCedula(cedula);
        return votante.isPresent() && !votante.get().isDescartado();
    }

    @Override
    public List<Votante> getVotantes() {
        return votanteRepository.findAllByOrderByIdAsc();
    }

    @Override
    public void deleteVotante(Long id) {
        try {
            Votante votante = buscarVotantePorId(id);
            votanteRepository.delete(votante);
            log.info("Votante con ID {} eliminado exitosamente.", id);
        }catch (Exception e) {
            log.error("Error al eliminar Votante: {}", e.getMessage());
        }
    }

    @Override
    public Votante buscarVotantePorId(Long id) {
        return votanteRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Votante no encontrado "));
    }

    @Override
    public boolean todosHanVotado() {
        long totalVotantes = votanteRepository.countByDescartadoFalse(); // Solo los no descartados
        long votantesQueHanVotado = votanteRepository.countByVotadoTrue(); // Votantes que han votado
        return totalVotantes == votantesQueHanVotado; // Si los totales coinciden, todos han votado
    }

    @Override
    public void descartarVotante(Long id) {
        Votante votante = buscarVotantePorId(id);
        if(votante.isVotado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede descartar un votante que ya ha votado.");
        }
        votante.setDescartado(true);
        votanteRepository.save(votante);
    }

    @Override
    public List<Votante> getVotanteNoDescartado() {
        return votanteRepository.findByDescartadoFalseOrderByIdAsc();
    }
}

