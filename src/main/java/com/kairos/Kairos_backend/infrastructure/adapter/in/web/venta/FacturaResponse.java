package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import com.kairos.Kairos_backend.application.port.in.ConsultarFacturaUseCase.Factura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Lo que va impreso en una factura (GET /api/ventas/{id}/factura). */
public record FacturaResponse(
        String numero,
        LocalDateTime fecha,
        String estado,
        Empresa empresa,
        Sede sede,
        String vendedor,
        Cliente cliente,
        List<Linea> lineas,
        BigDecimal total
) {
    public record Empresa(String nombre, String nit, String email, String telefono) {
    }

    public record Sede(String nombre, String ciudad, String direccion) {
    }

    public record Cliente(String nombre, String cedula) {
    }

    public record Linea(String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal,
                        int mesesGarantia, LocalDate garantiaHasta) {
    }

    public static FacturaResponse desde(Factura f) {
        var e = f.empresa();
        var s = f.sede();
        var c = f.cliente();
        return new FacturaResponse(f.numero(), f.fecha(), f.estado(),
                new Empresa(e.nombre(), e.nit(), e.email(), e.telefono()),
                new Sede(s.nombre(), s.ciudad(), s.direccion()),
                f.vendedor(),
                c == null ? null : new Cliente(c.nombre(), c.cedula()),
                f.lineas().stream().map(l -> new Linea(l.producto(), l.cantidad(), l.precioUnitario(),
                        l.subtotal(), l.mesesGarantia(), l.garantiaHasta())).toList(),
                f.total());
    }
}
