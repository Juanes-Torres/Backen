package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle;
import com.kairos.Kairos_backend.application.port.out.ProductoVendido;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;

import java.time.LocalDate;
import java.util.List;

/**
 * CASO DE USO: consultar ventas (RF04, SCRUM-48).
 * - El administrador ve las de su empresa y puede filtrar por sede y por vendedor.
 * - El vendedor solo ve las suyas.
 * - El cliente ve sus compras en cualquier empresa.
 */
public interface ConsultarVentasUseCase {

    List<VentaResumen> listar(Solicitante solicitante, Filtro filtro);

    /**
     * Los productos más vendidos entre dos días (incluidos), con las mismas reglas de quién ve qué
     * ventas que listar(): el administrador, su empresa; el vendedor, solo sus ventas.
     */
    List<ProductoVendido> masVendidos(Solicitante solicitante, LocalDate desde, LocalDate hasta, int limite);

    /** Una factura con sus productos. La ven los empleados de esa empresa o el cliente que compró. */
    VentaConDetalle buscar(Solicitante solicitante, Long idVenta);

    /** Todos los campos son opcionales. Las fechas incluyen el día completo. */
    record Filtro(Long idAlmacen, Long idVendedor, LocalDate desde, LocalDate hasta) {
    }

    record VentaConDetalle(VentaResumen venta, List<LineaVentaDetalle> lineas) {
    }
}
