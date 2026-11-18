package com.kairos.Kairos_backend.domain.exception;

/**
 * Se lanza cuando un servicio externo (una API de terceros) está caído, lento o
 * en su límite de uso. Lo demás de KAIRÓS sigue funcionando.
 * Respuesta HTTP: 503 Service Unavailable.
 */
public class ServicioExternoNoDisponibleException extends RuntimeException {

    public ServicioExternoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
