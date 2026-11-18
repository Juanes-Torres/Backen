package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Venta;

import java.util.Optional;

/**
 * PUERTO DE SALIDA: guardar y buscar ventas (las listas por sede y vendedor llegan en SCRUM-48).
 */
public interface VentaRepositoryPort {

    /** Guarda la venta con todas sus líneas. */
    Venta guardar(Venta venta);

    Optional<Venta> buscarPorId(Long id);

    /** Cuántas ventas tiene la empresa: con esto se calcula el siguiente número de factura. */
    long contarPorEmpresa(Long idEmpresa);
}
