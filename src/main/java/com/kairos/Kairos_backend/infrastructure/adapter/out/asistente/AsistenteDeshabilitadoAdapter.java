package com.kairos.Kairos_backend.infrastructure.adapter.out.asistente;

import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Se usa cuando el asistente está apagado (kairos.asistente.enabled=false, el valor por defecto):
 * la app funciona igual y la API responde 503 con un mensaje claro. Mismo patrón que el correo.
 */
@Component
@ConditionalOnProperty(name = "kairos.asistente.enabled", havingValue = "false", matchIfMissing = true)
public class AsistenteDeshabilitadoAdapter implements AsistenteIaPort {

    @Override
    public String responder(String instrucciones, String pregunta, List<HerramientaIa> herramientas,
                            EjecutorHerramientas ejecutor) {
        throw new ServicioExternoNoDisponibleException("El asistente virtual está apagado en este ambiente");
    }
}
