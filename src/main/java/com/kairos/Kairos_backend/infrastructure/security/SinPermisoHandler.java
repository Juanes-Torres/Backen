package com.kairos.Kairos_backend.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 403 Forbidden: el token es válido, pero el rol no tiene permiso.
 */
@Component
public class SinPermisoHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        RespuestaErrorJson.escribir(response, 403, "Forbidden",
                "No tiene permisos para realizar esta acción");
    }
}
