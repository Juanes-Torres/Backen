package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Inventario;

import java.util.List;
import java.util.Optional;

public interface InventarioRepositoryPort {

    Inventario guardar(Inventario inventario);

    Optional<Inventario> buscarPorId(Long id);

    boolean existePorProductoYAlmacen(Long idProducto, Long idAlmacen);

    boolean existePorProducto(Long idProducto);

    /** Solo los almacenes de la empresa. Si idAlmacen es null, devuelve todos sus almacenes. */
    List<InventarioDetalle> listarDetalle(Long idEmpresa, Long idAlmacen);

    List<InventarioDetalle> listarBajoStock(Long idEmpresa);

    Optional<InventarioDetalle> buscarDetallePorId(Long id);
}
