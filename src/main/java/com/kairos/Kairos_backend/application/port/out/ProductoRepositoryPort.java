package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(Long id);

    /**
     * Catálogo público: SOLO productos de empresas ACTIVAS.
     * Filtros opcionales: si llegan en null, no se filtra por ese campo.
     */
    List<Producto> buscarEnCatalogo(Long idEmpresa, Long idCategoria, String nombre);

    boolean existePorId(Long id);

    void eliminar(Long id);
}
