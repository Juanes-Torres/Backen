package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre,

        @Size(max = 20, message = "Máximo 20 caracteres")
        String telefono
) {
}
