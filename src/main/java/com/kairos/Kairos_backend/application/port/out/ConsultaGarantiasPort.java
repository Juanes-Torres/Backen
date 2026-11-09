package com.kairos.Kairos_backend.application.port.out;

import java.util.List;

/**
 * PUERTO DE SALIDA: productos comprados en una empresa, para consultar su garantía (solo lectura).
 */
public interface ConsultaGarantiasPort {

    /** Compras del cliente con esa cédula en la empresa, de la más reciente a la más antigua. */
    List<CompraConGarantia> comprasPorCedula(Long idEmpresa, String cedula);

    /** Productos de la factura con ese número en la empresa. */
    List<CompraConGarantia> comprasPorFactura(Long idEmpresa, String numeroFactura);
}
