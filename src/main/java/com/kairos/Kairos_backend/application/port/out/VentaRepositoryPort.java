package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Venta;

/**
 * PUERTO DE SALIDA: guardar ventas (las consultas llegan en SCRUM-48).
 */
public interface VentaRepositoryPort {

    /** Guarda la venta con todas sus líneas. */
    Venta guardar(Venta venta);

    /** Cuántas ventas tiene la empresa: con esto se calcula el siguiente número de factura. */
    long contarPorEmpresa(Long idEmpresa);
}
