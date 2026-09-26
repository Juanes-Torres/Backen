package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.EstadoAlmacen;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.AlmacenEntity;

public final class AlmacenMapper {

    private AlmacenMapper() {
    }

    public static AlmacenEntity toEntity(Almacen a) {
        return new AlmacenEntity(a.getId(), a.getNombre(), a.getCiudad(),
                a.getDireccion(), a.getEstado().name());
    }

    public static Almacen toDomain(AlmacenEntity e) {
        return new Almacen(e.getId(), e.getNombre(), e.getCiudad(),
                e.getDireccion(), EstadoAlmacen.valueOf(e.getEstado()));
    }
}
