package com.kairos.Kairos_backend.domain.exception;

/**
 * Se lanza cuando el login falla (email o contraseña incorrectos, o usuario inactivo).
 * Respuesta HTTP: 401 Unauthorized.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
