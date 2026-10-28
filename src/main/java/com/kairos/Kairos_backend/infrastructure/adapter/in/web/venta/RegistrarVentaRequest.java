package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Datos para registrar una venta. El precio NO llega aquí: lo pone el sistema desde el producto.
 */
public record RegistrarVentaRequest(
        /** Solo para el administrador; el vendedor vende en su sede. */
        Long idAlmacen,

        @Email(message = "El email del cliente no tiene un formato válido")
        String emailCliente,

        @NotEmpty(message = "La venta debe tener al menos un producto")
        List<@Valid @NotNull(message = "El producto no puede ir vacío") ItemRequest> items
) {

    public record ItemRequest(
            @NotNull(message = "El idProducto es obligatorio")
            Long idProducto,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser al menos 1")
            Integer cantidad
    ) {
    }
}
