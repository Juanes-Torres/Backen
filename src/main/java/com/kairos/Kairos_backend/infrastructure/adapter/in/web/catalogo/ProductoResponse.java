package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.domain.model.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String marca,
        Long idCategoria
) {
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getMarca(), p.getIdCategoria());
    }
}
