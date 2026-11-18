package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre,

        @Size(max = 255, message = "Máximo 255 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Máximo 10 enteros y 2 decimales")
        BigDecimal precio,

        @Size(max = 100, message = "Máximo 100 caracteres")
        String marca,

        @NotNull(message = "La categoría es obligatoria")
        Long idCategoria,

        /** Opcional: al crear, si no se envía son 12 meses; al editar, si no se envía se conserva. */
        @Min(value = 0, message = "Los meses de garantía no pueden ser negativos")
        @Max(value = 120, message = "Máximo 120 meses de garantía")
        Integer mesesGarantia
) {
}
