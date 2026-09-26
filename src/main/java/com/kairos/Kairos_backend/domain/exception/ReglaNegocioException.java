package com.kairos.Kairos_backend.domain.exception;

/**
 * Se lanza cuando algo viola una regla del negocio de KAIRÓS.
 * Ejemplo: registrar un vendedor sin almacén, o sacar más unidades de las que hay.
 * Respuesta HTTP: 400 Bad Request.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
