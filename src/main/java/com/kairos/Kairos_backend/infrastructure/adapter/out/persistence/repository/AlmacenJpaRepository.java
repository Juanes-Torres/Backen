package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.AlmacenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlmacenJpaRepository extends JpaRepository<AlmacenEntity, Long> {

    List<AlmacenEntity> findAllByOrderByNombreAsc();
}
