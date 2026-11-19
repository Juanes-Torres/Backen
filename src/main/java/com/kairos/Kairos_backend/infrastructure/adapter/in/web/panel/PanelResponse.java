package com.kairos.Kairos_backend.infrastructure.adapter.in.web.panel;

import com.kairos.Kairos_backend.application.port.in.ConsultarPanelUseCase.Panel;
import com.kairos.Kairos_backend.application.port.in.ConsultarPanelUseCase.Resumen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Panel de inicio (GET /api/panel). Las secciones que no aplican al rol llegan en nulo. */
public record PanelResponse(
        String rol,
        Ventas ventas,
        Integer productosBajoStock,
        Integer reclamosAbiertos,
        Map<String, Long> empresasPorEstado
) {
    public record Ventas(Periodo hoy, Periodo ultimos7Dias, Periodo mes, List<Dia> porDia,
                         List<MasVendido> masVendidosDelMes) {
    }

    public record Periodo(long cantidad, BigDecimal total) {
        static Periodo desde(Resumen r) {
            return new Periodo(r.cantidad(), r.total());
        }
    }

    public record Dia(LocalDate dia, long cantidad, BigDecimal total) {
    }

    public record MasVendido(String producto, long unidades, BigDecimal total) {
    }

    public static PanelResponse desde(Panel p) {
        var v = p.ventas();
        Ventas ventas = v == null ? null : new Ventas(Periodo.desde(v.hoy()), Periodo.desde(v.ultimos7Dias()),
                Periodo.desde(v.mes()),
                v.porDia().stream().map(d -> new Dia(d.dia(), d.cantidad(), d.total())).toList(),
                v.masVendidosDelMes().stream().map(m -> new MasVendido(m.producto(), m.unidades(), m.total())).toList());
        return new PanelResponse(p.rol(), ventas, p.productosBajoStock(), p.reclamosAbiertos(), p.empresasPorEstado());
    }
}
