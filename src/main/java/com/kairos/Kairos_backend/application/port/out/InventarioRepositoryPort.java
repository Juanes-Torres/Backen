package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Inventario;

import java.util.List;
import java.util.Optional;

public interface InventarioRepositoryPort {

    Inventario guardar(Inventario inventario);

    Optional<Inventario> buscarPorId(Long id);

    boolean existePorProductoYAlmacen(Long idProducto, Long idAlmacen);

    boolean existePorProducto(Long idProducto);

    /** Si idAlmacen es null, devuelve el inventario de todos los almacenes. */
    List<InventarioDetalle> listarDetalle(Long idAlmacen);

    List<InventarioDetalle> listarBajoStock();

    Optional<InventarioDetalle> buscarDetallePorId(Long id);
}
