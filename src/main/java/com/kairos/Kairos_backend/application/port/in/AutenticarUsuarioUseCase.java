package com.kairos.Kairos_backend.application.port.in;

public interface AutenticarUsuarioUseCase {

    String autenticar(String email, String password);
}