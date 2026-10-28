package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import com.kairos.Kairos_backend.domain.model.DetalleVenta;
import com.kairos.Kairos_backend.domain.model.EstadoVenta;
import com.kairos.Kairos_backend.domain.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Long id,
        String numeroFactura,
        LocalDateTime fecha,
        Long idAlmacen,
        Long idVendedor,
        Long idCliente,
        EstadoVenta estado,
        BigDecimal total,
        List<Linea> detalles
) {
    public record Linea(Long idProducto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {

        static Linea desde(DetalleVenta d) {
            return new Linea(d.getIdProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
        }
    }

    public static VentaResponse desde(Venta v) {
        return new VentaResponse(v.getId(), v.getNumeroFactura(), v.getFecha(), v.getIdAlmacen(),
                v.getIdVendedor(), v.getIdCliente(), v.getEstado(), v.getTotal(),
                v.getDetalles().stream().map(Linea::desde).toList());
    }
}
