package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.ProductoEntity;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoEntity toEntity(Producto p) {
        return new ProductoEntity(p.getId(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getMarca(), p.getIdCategoria());
    }

    public static Producto toDomain(ProductoEntity e) {
        return new Producto(e.getId(), e.getNombre(), e.getDescripcion(),
                e.getPrecio(), e.getMarca(), e.getIdCategoria());
    }
}
