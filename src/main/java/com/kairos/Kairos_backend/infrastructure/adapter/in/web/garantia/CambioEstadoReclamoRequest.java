package com.kairos.Kairos_backend.infrastructure.adapter.in.web.garantia;

import com.kairos.Kairos_backend.domain.model.EstadoReclamo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nuevo estado del reclamo y una nota opcional (por ejemplo, qué se reparó). */
public record CambioEstadoReclamoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoReclamo estado,

        @Size(max = 500, message = "Máximo 500 caracteres")
        String nota
) {
}
