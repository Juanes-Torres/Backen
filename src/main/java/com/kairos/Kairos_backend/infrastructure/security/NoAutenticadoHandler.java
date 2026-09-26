package com.kairos.Kairos_backend.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 Unauthorized: no hay token, o el token no es válido o ya venció.
 */
@Component
public class NoAutenticadoHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        RespuestaErrorJson.escribir(response, 401, "Unauthorized",
                "Debe iniciar sesión: falta el token o no es válido");
    }
}
