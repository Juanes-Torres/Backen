package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.EmpresaEntity;

public final class EmpresaMapper {

    private EmpresaMapper() {
    }

    public static EmpresaEntity toEntity(Empresa e) {
        return new EmpresaEntity(e.getId(), e.getNombre(), e.getNit(), e.getEmail(),
                e.getTelefono(), e.getEstado().name(), e.getFechaRegistro());
    }

    public static Empresa toDomain(EmpresaEntity e) {
        return new Empresa(e.getId(), e.getNombre(), e.getNit(), e.getEmail(),
                e.getTelefono(), EstadoEmpresa.valueOf(e.getEstado()), e.getFechaRegistro());
    }
}
