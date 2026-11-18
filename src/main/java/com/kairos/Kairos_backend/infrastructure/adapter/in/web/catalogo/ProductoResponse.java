package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.domain.model.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String marca,
        Long idCategoria,
        Long idEmpresa,
        int mesesGarantia,
        String imagenUrl          // nulo si el producto no tiene foto
) {
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getMarca(), p.getIdCategoria(), p.getIdEmpresa(), p.getMesesGarantia(),
                imagenUrl(p));
    }

    /**
     * Dirección para mostrar la foto, relativa al backend: /api/productos/15/imagen?v=1a2b3c
     * El "v" cambia cada vez que cambia la foto: así el navegador no muestra la anterior guardada.
     */
    private static String imagenUrl(Producto p) {
        return p.tieneImagen()
                ? "/api/productos/" + p.getId() + "/imagen?v=" + Integer.toHexString(p.getImagen().hashCode())
                : null;
    }
}
