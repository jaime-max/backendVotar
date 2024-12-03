package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Votante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String cedula;
    private String nombre;
    @Column(nullable = false)
    private boolean votado= false; // si el votante ya ha votado
    @Column(nullable = false)
    private boolean descartado = false; // Si el votante fue descartado

}
