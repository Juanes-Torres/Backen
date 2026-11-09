package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.EstadoReclamo;
import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;
import com.kairos.Kairos_backend.domain.model.SeguimientoReclamo;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.ReclamoGarantiaEntity;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.SeguimientoReclamoEntity;

/** Convierte entre el reclamo del dominio y sus entidades JPA. */
public final class ReclamoMapper {

    private ReclamoMapper() {
    }

    public static ReclamoGarantiaEntity toEntity(ReclamoGarantia r) {
        return new ReclamoGarantiaEntity(r.getId(), r.getIdEmpresa(), r.getIdVenta(), r.getIdProducto(),
                r.getIdCliente(), r.getMotivo(), r.getEstado().name(), r.getFechaRegistro(), r.getIdRegistradoPor(),
                r.getHistorial().stream().map(ReclamoMapper::toEntity).toList());
    }

    public static SeguimientoReclamoEntity toEntity(SeguimientoReclamo s) {
        return new SeguimientoReclamoEntity(null, s.estado().name(), s.nota(), s.fecha(), s.idUsuario());
    }

    public static ReclamoGarantia toDomain(ReclamoGarantiaEntity e) {
        return new ReclamoGarantia(e.getId(), e.getIdEmpresa(), e.getIdVenta(), e.getIdProducto(), e.getIdCliente(),
                e.getMotivo(), EstadoReclamo.valueOf(e.getEstado()), e.getFechaRegistro(), e.getIdRegistradoPor(),
                e.getHistorial().stream()
                        .map(s -> new SeguimientoReclamo(EstadoReclamo.valueOf(s.getEstado()), s.getNota(), s.getFecha(),
                                s.getIdUsuario()))
                        .toList());
    }
}
