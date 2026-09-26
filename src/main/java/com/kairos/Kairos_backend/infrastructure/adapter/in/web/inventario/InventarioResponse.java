package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;

public record InventarioResponse(
        Long id,
        Long idProducto,
        String producto,
        Long idAlmacen,
        String almacen,
        Integer cantidadDisponible,
        Integer stockMinimo,
        boolean bajoStock
) {
    public static InventarioResponse desde(InventarioDetalle d) {
        return new InventarioResponse(d.id(), d.idProducto(), d.producto(), d.idAlmacen(), d.almacen(),
                d.cantidadDisponible(), d.stockMinimo(), d.bajoStock());
    }
}
