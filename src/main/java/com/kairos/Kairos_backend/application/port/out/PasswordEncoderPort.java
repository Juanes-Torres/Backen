package com.kairos.Kairos_backend.application.port.out;

/**
 * PUERTO DE SALIDA: cifrar y comparar contraseñas.
 * La aplicación no sabe que por detrás se usa BCrypt.
 */
public interface PasswordEncoderPort {

    String cifrar(String passwordPlano);

    boolean coincide(String passwordPlano, String passwordHash);
}
