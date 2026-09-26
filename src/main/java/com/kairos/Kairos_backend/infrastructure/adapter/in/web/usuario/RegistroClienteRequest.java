package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro PÚBLICO: no trae rol, porque siempre se crea un CLIENTE (RF01).
 */
public record RegistroClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres")
        String password,

        @Size(max = 20, message = "Máximo 20 caracteres")
        String telefono
) {
}
