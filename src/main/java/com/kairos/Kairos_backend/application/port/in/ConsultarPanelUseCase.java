package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.application.port.out.ProductoVendido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * CASO DE USO: el panel de inicio (SCRUM-106). Números listos para tarjetas y una gráfica.
 * - Administrador: su empresa.  Vendedor: solo sus ventas y su sede.
 * - Superadministrador: cuántas empresas hay en cada estado.
 * Las secciones que no aplican a un rol llegan en nulo.
 */
public interface ConsultarPanelUseCase {

    Panel panel(Solicitante solicitante);

    record Panel(
            String rol,
            Ventas ventas,                         // nulo para el superadministrador
            Integer productosBajoStock,            // nulo para el superadministrador
            Integer reclamosAbiertos,              // nulo para el superadministrador
            Map<String, Long> empresasPorEstado    // solo el superadministrador
    ) {
    }

    record Ventas(
            Resumen hoy,
            Resumen ultimos7Dias,
            Resumen mes,
            List<VentasDelDia> porDia,               // los últimos 7 días, también los que no tuvieron ventas
            List<ProductoVendido> masVendidosDelMes  // máximo 5
    ) {
    }

    record Resumen(long cantidad, BigDecimal total) {
    }

    record VentasDelDia(LocalDate dia, long cantidad, BigDecimal total) {
    }
}
