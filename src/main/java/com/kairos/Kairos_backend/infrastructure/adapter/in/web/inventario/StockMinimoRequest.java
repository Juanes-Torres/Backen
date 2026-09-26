package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockMinimoRequest(
        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {
}
