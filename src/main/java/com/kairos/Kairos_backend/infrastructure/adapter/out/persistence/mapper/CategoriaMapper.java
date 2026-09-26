package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Categoria;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.CategoriaEntity;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaEntity toEntity(Categoria c) {
        return new CategoriaEntity(c.getId(), c.getNombre());
    }

    public static Categoria toDomain(CategoriaEntity e) {
        return new Categoria(e.getId(), e.getNombre());
    }
}
