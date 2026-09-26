package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Producto;

import java.math.BigDecimal;
import java.util.List;

/**
 * CASO DE USO: administrar y consultar el catálogo de productos.
 */
public interface GestionarProductosUseCase {

    List<Producto> buscar(Long idCategoria, String nombre);

    Producto obtener(Long id);

    Producto crear(ProductoCommand command);

    Producto actualizar(Long id, ProductoCommand command);

    void eliminar(Long id);

    record ProductoCommand(
            String nombre,
            String descripcion,
            BigDecimal precio,
            String marca,
            Long idCategoria
    ) {
    }
}
