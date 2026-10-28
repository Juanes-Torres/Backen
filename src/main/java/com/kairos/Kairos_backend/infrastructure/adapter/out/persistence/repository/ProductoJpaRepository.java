package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    /**
     * Catálogo público: une el producto con su empresa y deja solo las ACTIVAS.
     * Cada filtro se aplica solo si llega (idEmpresa / idCategoria nulos, nombre vacío = sin filtro).
     */
    @Query("""
            SELECT p FROM ProductoEntity p
            JOIN EmpresaEntity e ON e.id = p.idEmpresa
            WHERE e.estado = 'ACTIVA'
              AND (:idEmpresa IS NULL OR p.idEmpresa = :idEmpresa)
              AND (:idCategoria IS NULL OR p.idCategoria = :idCategoria)
              AND (:nombre = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
            ORDER BY p.nombre
            """)
    List<ProductoEntity> buscarEnCatalogo(@Param("idEmpresa") Long idEmpresa,
                                          @Param("idCategoria") Long idCategoria,
                                          @Param("nombre") String nombre);
}
