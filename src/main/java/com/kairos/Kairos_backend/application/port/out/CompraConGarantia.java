package com.kairos.Kairos_backend.application.port.out;

import java.time.LocalDateTime;

/**
 * Vista de LECTURA: un producto comprado en una factura, con lo necesario para calcular su garantía.
 * mesesGarantia es el plazo que tiene hoy el producto.
 */
public record CompraConGarantia(
        Long idVenta,
        String numeroFactura,
        LocalDateTime fechaCompra,
        Long idAlmacen,
        String almacen,
        Long idCliente,
        String cliente,
        String cedulaCliente,
        Long idProducto,
        String producto,
        Integer cantidad,
        Integer mesesGarantia
) {
}
