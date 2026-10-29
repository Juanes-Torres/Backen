package com.kairos.Kairos_backend.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Primer filtro de CADA petición (antes que la seguridad):
 * - Le pone un id ("idPeticion") que sale en todas las líneas de log de esa petición
 *   y se devuelve en el encabezado X-Request-Id, para encontrarla después en los logs.
 * - Al terminar, escribe una línea con método, ruta, código HTTP y duración.
 * Los logs van a la consola (12 factores: XI); quién los guarda lo decide el ambiente.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegistroPeticionesFilter extends OncePerRequestFilter {

    public static final String ENCABEZADO = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RegistroPeticionesFilter.class);

    // Solo se acepta un id que venga de afuera si es corto y sin caracteres raros (evita ensuciar los logs)
    private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String idPeticion = idDe(request);
        MDC.put(ContextoLog.ID_PETICION, idPeticion);
        response.setHeader(ENCABEZADO, idPeticion);
        long inicio = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), ms);
            MDC.clear();   // el hilo atiende después otras peticiones: no deben heredar estos datos
        }
    }

    private static String idDe(HttpServletRequest request) {
        String recibido = request.getHeader(ENCABEZADO);
        return recibido != null && ID_VALIDO.matcher(recibido).matches()
                ? recibido
                : UUID.randomUUID().toString();
    }
}
