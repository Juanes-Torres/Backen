package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<CategoriaEntity> findAllByOrderByNombreAsc();
}
