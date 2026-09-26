package com.kairos.Kairos_backend.domain.exception;

/**
 * Se lanza cuando se intenta crear algo que ya existe
 * (un email ya registrado, una categoría con el mismo nombre...).
 * Respuesta HTTP: 409 Conflict.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
