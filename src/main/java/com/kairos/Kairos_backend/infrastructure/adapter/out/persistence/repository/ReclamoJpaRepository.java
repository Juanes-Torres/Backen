package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.ReclamoGarantiaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamoJpaRepository extends JpaRepository<ReclamoGarantiaEntity, Long> {

    List<ReclamoGarantiaEntity> findByIdEmpresaOrderByFechaRegistroDescIdDesc(Long idEmpresa);

    List<ReclamoGarantiaEntity> findByIdClienteOrderByFechaRegistroDescIdDesc(Long idCliente);
}
