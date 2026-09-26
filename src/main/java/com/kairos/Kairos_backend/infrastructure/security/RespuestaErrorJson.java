package com.kairos.Kairos_backend.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Escribe un error en formato JSON (mismo formato que ErrorResponse)
 * para los errores de seguridad, que ocurren ANTES de llegar al controller.
 */
final class RespuestaErrorJson {

    private RespuestaErrorJson() {
    }

    static void escribir(HttpServletResponse response, int status, String error, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"status":%d,"error":"%s","mensaje":"%s","campos":null,"fecha":"%s"}"""
                .formatted(status, error, mensaje, LocalDateTime.now()));
    }
}
