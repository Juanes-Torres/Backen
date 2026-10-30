package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import com.kairos.Kairos_backend.application.port.out.VentaResumen;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una fila de la lista de ventas, con los nombres de la sede, el vendedor y el cliente. */
public record VentaResumenResponse(
        Long id,
        String numeroFactura,
        LocalDateTime fecha,
        Long idAlmacen,
        String almacen,
        Long idVendedor,
        String vendedor,
        Long idCliente,
        String cliente,
        String estado,
        BigDecimal total
) {
    public static VentaResumenResponse desde(VentaResumen v) {
        return new VentaResumenResponse(v.id(), v.numeroFactura(), v.fecha(), v.idAlmacen(), v.almacen(),
                v.idVendedor(), v.vendedor(), v.idCliente(), v.cliente(), v.estado(), v.total());
    }
}
