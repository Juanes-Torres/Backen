package com.kairos.Kairos_backend.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vista de LECTURA de una venta: trae los nombres de la sede, del vendedor y del cliente
 * para que el frontend pueda mostrarlos sin hacer más consultas.
 * cliente e idCliente son nulos en una venta de mostrador.
 */
public record VentaResumen(
        Long id,
        String numeroFactura,
        LocalDateTime fecha,
        Long idEmpresa,
        Long idAlmacen,
        String almacen,
        Long idVendedor,
        String vendedor,
        Long idCliente,
        String cliente,
        String estado,
        BigDecimal total
) {
}
