package com.kairos.Kairos_backend.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CASO DE USO: todo lo que va impreso en una factura (quién vende, dónde, a quién y qué),
 * incluida la fecha hasta la que cubre la garantía de cada producto.
 * La ven las mismas personas que el detalle de la venta: los empleados de esa empresa o el cliente que compró.
 */
public interface ConsultarFacturaUseCase {

    Factura factura(Solicitante solicitante, Long idVenta);

    record Factura(
            String numero,
            LocalDateTime fecha,
            String estado,
            Emisor empresa,
            Sede sede,
            String vendedor,
            Cliente cliente,            // nulo en una venta de mostrador
            List<Linea> lineas,
            BigDecimal total
    ) {
    }

    record Emisor(String nombre, String nit, String email, String telefono) {
    }

    record Sede(String nombre, String ciudad, String direccion) {
    }

    record Cliente(String nombre, String cedula) {
    }

    /** garantiaHasta es nulo si el producto no tiene garantía (0 meses). */
    record Linea(String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal,
                 int mesesGarantia, LocalDate garantiaHasta) {
    }
}
