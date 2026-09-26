package com.kairos.Kairos_backend.domain.exception;

/**
 * Se lanza cuando se busca algo que no existe (usuario, producto, almacén...).
 * Respuesta HTTP: 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
