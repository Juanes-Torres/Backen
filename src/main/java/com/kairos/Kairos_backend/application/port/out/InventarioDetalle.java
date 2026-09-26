package com.kairos.Kairos_backend.application.port.out;

/**
 * Vista de LECTURA del inventario: incluye los nombres del producto y del almacén
 * para que el frontend pueda mostrarlos sin hacer más consultas.
 */
public record InventarioDetalle(
        Long id,
        Long idProducto,
        String producto,
        Long idAlmacen,
        String almacen,
        Integer cantidadDisponible,
        Integer stockMinimo
) {
    public boolean bajoStock() {
        return cantidadDisponible < stockMinimo;
    }
}
