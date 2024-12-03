package com.example.demo.repository;


import com.example.demo.entity.Votante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotanteRepository extends JpaRepository<Votante, Long> {
    List<Votante> findAllByOrderByIdAsc();
    Optional<Votante> findByCedula(String cedula);
    // Método para contar el total de votantes
    long count();

    // Contar cuántos votantes han votado
    long countByVotadoTrue(); // Método para contar los votantes que han votado

    // Método para contar solo los votantes que no están descartados
    long countByDescartadoFalse();

    // Método para obtener todos los votantes no descartados
    List<Votante> findByDescartadoFalseOrderByIdAsc();
}
