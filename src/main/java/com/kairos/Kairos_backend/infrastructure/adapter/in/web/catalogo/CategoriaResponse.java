package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.domain.model.Categoria;

public record CategoriaResponse(Long id, String nombre) {

    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre());
    }
}
