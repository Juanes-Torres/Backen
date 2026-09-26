package com.kairos.Kairos_backend.infrastructure.adapter.in.web;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato estándar de TODOS los errores de la API.
 */
public record ErrorResponse(
        int status,
        String error,
        String mensaje,
        Map<String, String> campos,
        LocalDateTime fecha
) {
    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(status, error, mensaje, null, LocalDateTime.now());
    }
}
