package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {

    boolean existsByNit(String nit);

    List<EmpresaEntity> findByEstadoOrderByFechaRegistroAsc(String estado);
}
