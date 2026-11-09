package com.kairos.Kairos_backend.infrastructure.adapter.in.web.asistente;

import com.kairos.Kairos_backend.application.port.in.PreguntarAsistenteUseCase;
import com.kairos.Kairos_backend.application.port.in.PreguntarAsistenteUseCase.ConsultaHecha;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADAPTADOR DE ENTRADA (REST) del asistente virtual (SCRUM-77).
 * Traduce HTTP/JSON <-> caso de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/asistente")
public class AsistenteController {

    private final PreguntarAsistenteUseCase asistente;

    public AsistenteController(PreguntarAsistenteUseCase asistente) {
        this.asistente = asistente;
    }

    public record PreguntaRequest(
            @NotBlank(message = "La pregunta es obligatoria")
            @Size(max = 1000, message = "Máximo 1000 caracteres")
            String pregunta
    ) {
    }

    public record RespuestaResponse(String respuesta, List<ConsultaHecha> consultas, int preguntasRestantesHoy) {
    }

    /** Pregunta al asistente; responde el texto y qué consultas hizo para llegar a él. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public RespuestaResponse preguntar(@Valid @RequestBody PreguntaRequest request,
                                       @AuthenticationPrincipal UsuarioAutenticado yo) {
        var r = asistente.preguntar(yo.comoSolicitante(), request.pregunta());
        return new RespuestaResponse(r.respuesta(), r.consultas(), r.preguntasRestantesHoy());
    }
}
