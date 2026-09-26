package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import com.kairos.Kairos_backend.domain.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solo el ADMINISTRADOR crea vendedores u otros administradores.
 */
public record CrearEmpleadoRequest(
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

        @NotNull(message = "El rol es obligatorio (VENDEDOR o ADMINISTRADOR)")
        Rol rol,

        @Size(max = 20, message = "Máximo 20 caracteres")
        String telefono,

        Long idAlmacen
) {
}
