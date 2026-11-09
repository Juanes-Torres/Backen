package com.kairos.Kairos_backend.infrastructure.adapter.in.web.garantia;

import com.kairos.Kairos_backend.domain.model.EstadoReclamo;
import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;
import com.kairos.Kairos_backend.domain.model.SeguimientoReclamo;

import java.time.LocalDateTime;
import java.util.List;

/** Un reclamo con su historial completo de estados. */
public record ReclamoResponse(
        Long id,
        Long idVenta,
        Long idProducto,
        Long idCliente,
        String motivo,
        EstadoReclamo estado,
        LocalDateTime fechaRegistro,
        Long idRegistradoPor,
        List<Paso> historial
) {
    public record Paso(EstadoReclamo estado, String nota, LocalDateTime fecha, Long idUsuario) {
        static Paso desde(SeguimientoReclamo s) {
            return new Paso(s.estado(), s.nota(), s.fecha(), s.idUsuario());
        }
    }

    public static ReclamoResponse desde(ReclamoGarantia r) {
        return new ReclamoResponse(r.getId(), r.getIdVenta(), r.getIdProducto(), r.getIdCliente(), r.getMotivo(),
                r.getEstado(), r.getFechaRegistro(), r.getIdRegistradoPor(),
                r.getHistorial().stream().map(Paso::desde).toList());
    }
}
