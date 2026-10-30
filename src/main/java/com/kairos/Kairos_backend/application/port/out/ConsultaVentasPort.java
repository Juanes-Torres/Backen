package com.kairos.Kairos_backend.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE SALIDA: consultar ventas ya registradas (solo lectura).
 * Va aparte de VentaRepositoryPort: quien registra ventas no necesita estas consultas (SOLID: I).
 */
public interface ConsultaVentasPort {

    /** Ventas que cumplen el filtro, de la más reciente a la más antigua. */
    List<VentaResumen> listar(FiltroVentas filtro);

    Optional<VentaResumen> buscarResumen(Long idVenta);

    List<LineaVentaDetalle> lineasDe(Long idVenta);

    /**
     * Un campo nulo significa "sin ese filtro". El rango de fechas siempre llega lleno:
     * desde (incluido) hasta (excluido).
     */
    record FiltroVentas(Long idEmpresa, Long idAlmacen, Long idVendedor, Long idCliente,
                        LocalDateTime desde, LocalDateTime hasta) {
    }
}
