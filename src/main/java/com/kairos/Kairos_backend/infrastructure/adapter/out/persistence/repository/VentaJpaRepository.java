package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, Long> {

    long countByIdEmpresa(Long idEmpresa);
}
