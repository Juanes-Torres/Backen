package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {

    boolean existsByNit(String nit);

    Optional<EmpresaEntity> findByNit(String nit);

    List<EmpresaEntity> findByEstadoOrderByFechaRegistroAsc(String estado);

    List<EmpresaEntity> findAllByOrderByFechaRegistroAsc();
}
