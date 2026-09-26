package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlmacenRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String ciudad,

        @Size(max = 150, message = "Máximo 150 caracteres")
        String direccion
) {
}
