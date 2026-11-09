package com.kairos.Kairos_backend.infrastructure.adapter.in.web.garantia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para registrar un reclamo de garantía. */
public record ReclamoRequest(
        @NotNull(message = "El idVenta es obligatorio")
        Long idVenta,

        @NotNull(message = "El idProducto es obligatorio")
        Long idProducto,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 500, message = "Máximo 500 caracteres")
        String motivo
) {
}
