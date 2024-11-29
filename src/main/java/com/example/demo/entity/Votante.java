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
    private boolean votado= false; // Agrega este campo para saber si el votante ya ha votado

}
