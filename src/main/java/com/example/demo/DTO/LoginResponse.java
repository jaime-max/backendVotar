package com.example.demo.DTO;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String mensajeError;

    public LoginResponse(String token, String mensajeError) {

        this.token = token;
        this.mensajeError = mensajeError;
    }
}
