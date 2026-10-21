package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Producto;

import java.math.BigDecimal;
import java.util.List;

/**
 * CASO DE USO: administrar y consultar el catálogo de productos.
 * La consulta es pública; crear, cambiar y borrar es solo dentro de la empresa del token.
 */
public interface GestionarProductosUseCase {

    List<Producto> buscar(Long idCategoria, String nombre);

    Producto obtener(Long id);

    Producto crear(Long idEmpresa, ProductoCommand command);

    Producto actualizar(Long idEmpresa, Long id, ProductoCommand command);

    void eliminar(Long idEmpresa, Long id);

    record ProductoCommand(
            String nombre,
            String descripcion,
            BigDecimal precio,
            String marca,
            Long idCategoria
    ) {
    }
}
