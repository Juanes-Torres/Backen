package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.application.port.out.CompraConGarantia;

import java.time.LocalDate;
import java.util.List;

/**
 * CASO DE USO: consultar la garantía de lo que compró un cliente, por su cédula o por
 * el número de factura (SCRUM-74). Solo muestra compras de la empresa del empleado.
 */
public interface ConsultarGarantiasUseCase {

    /** Se envía la cédula O la factura (una de las dos). */
    List<GarantiaDeProducto> consultar(Solicitante solicitante, String cedula, String factura);

    /** Un producto comprado con su garantía ya calculada. */
    record GarantiaDeProducto(CompraConGarantia compra, LocalDate fechaFin, boolean vigente) {
    }
}
