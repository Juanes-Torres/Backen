package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro PÚBLICO de una empresa junto con su primer administrador.
 */
public record RegistroEmpresaRequest(
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        @Size(max = 150, message = "Máximo 150 caracteres")
        String nombre,

        @NotBlank(message = "El NIT es obligatorio")
        @Size(max = 20, message = "Máximo 20 caracteres")
        String nit,

        @NotBlank(message = "El email de la empresa es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String email,

        @Size(max = 20, message = "Máximo 20 caracteres")
        String telefono,

        @NotNull(message = "Los datos del administrador son obligatorios")
        @Valid
        Administrador administrador
) {

    public record Administrador(
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
}
