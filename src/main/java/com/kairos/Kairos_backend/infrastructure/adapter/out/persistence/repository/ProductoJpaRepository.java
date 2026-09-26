package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data genera el SQL a partir del NOMBRE de cada método.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findAllByOrderByNombreAsc();

    List<ProductoEntity> findByIdCategoriaOrderByNombreAsc(Long idCategoria);

    List<ProductoEntity> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    List<ProductoEntity> findByIdCategoriaAndNombreContainingIgnoreCaseOrderByNombreAsc(Long idCategoria, String nombre);
}
