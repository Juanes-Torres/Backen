package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase.VentaConDetalle;
import com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle;

import java.math.BigDecimal;
import java.util.List;

/** Una factura completa: sus datos y cada producto con su nombre. */
public record VentaDetalleResponse(VentaResumenResponse venta, List<Linea> lineas) {

    public record Linea(Long idProducto, String producto, Integer cantidad, BigDecimal precioUnitario,
                        BigDecimal subtotal) {
        static Linea desde(LineaVentaDetalle l) {
            return new Linea(l.idProducto(), l.producto(), l.cantidad(), l.precioUnitario(), l.subtotal());
        }
    }

    public static VentaDetalleResponse desde(VentaConDetalle v) {
        return new VentaDetalleResponse(VentaResumenResponse.desde(v.venta()),
                v.lineas().stream().map(Linea::desde).toList());
    }
}
