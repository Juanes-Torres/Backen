package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.InventarioEntity;

public final class InventarioMapper {

    private InventarioMapper() {
    }

    public static InventarioEntity toEntity(Inventario i) {
        return new InventarioEntity(i.getId(), i.getIdProducto(), i.getIdAlmacen(),
                i.getCantidadDisponible(), i.getStockMinimo());
    }

    public static Inventario toDomain(InventarioEntity e) {
        return new Inventario(e.getId(), e.getIdProducto(), e.getIdAlmacen(),
                e.getCantidadDisponible(), e.getStockMinimo());
    }
}
