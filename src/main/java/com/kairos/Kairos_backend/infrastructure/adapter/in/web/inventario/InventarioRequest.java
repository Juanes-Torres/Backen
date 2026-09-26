package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventarioRequest(
        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull(message = "El almacén es obligatorio")
        Long idAlmacen,

        @NotNull(message = "La cantidad inicial es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        Integer cantidadInicial,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {
}
