package com.kairos.Kairos_backend.application.port.out;

import java.math.BigDecimal;

/**
 * Vista de LECTURA de una línea de la factura, con el nombre del producto.
 */
public record LineaVentaDetalle(
        Long idProducto,
        String producto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
