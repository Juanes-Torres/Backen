package com.kairos.Kairos_backend.infrastructure.adapter.in.web.garantia;

import com.kairos.Kairos_backend.application.port.in.ConsultarGarantiasUseCase.GarantiaDeProducto;
import com.kairos.Kairos_backend.application.port.out.CompraConGarantia;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Un producto comprado y el estado de su garantía. */
public record GarantiaResponse(
        Long idVenta,
        String numeroFactura,
        LocalDateTime fechaCompra,
        String almacen,
        Long idCliente,
        String cliente,
        String cedulaCliente,
        Long idProducto,
        String producto,
        Integer cantidad,
        Integer mesesGarantia,
        LocalDate fechaFinGarantia,
        boolean vigente
) {
    public static GarantiaResponse desde(GarantiaDeProducto g) {
        CompraConGarantia c = g.compra();
        return new GarantiaResponse(c.idVenta(), c.numeroFactura(), c.fechaCompra(), c.almacen(), c.idCliente(),
                c.cliente(), c.cedulaCliente(), c.idProducto(), c.producto(), c.cantidad(), c.mesesGarantia(),
                g.fechaFin(), g.vigente());
    }
}
