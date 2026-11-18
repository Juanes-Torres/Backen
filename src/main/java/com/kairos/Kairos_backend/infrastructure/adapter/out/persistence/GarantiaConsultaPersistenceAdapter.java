package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.CompraConGarantia;
import com.kairos.Kairos_backend.application.port.out.ConsultaGarantiasPort;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.VentaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ADAPTADOR de ConsultaGarantiasPort: busca en PostgreSQL los productos comprados y su plazo de garantía.
 */
@Component
public class GarantiaConsultaPersistenceAdapter implements ConsultaGarantiasPort {

    private final VentaJpaRepository jpaRepository;

    public GarantiaConsultaPersistenceAdapter(VentaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CompraConGarantia> comprasPorCedula(Long idEmpresa, String cedula) {
        return jpaRepository.comprasPorCedula(idEmpresa, cedula);
    }

    @Override
    public List<CompraConGarantia> comprasPorFactura(Long idEmpresa, String numeroFactura) {
        return jpaRepository.comprasPorFactura(idEmpresa, numeroFactura);
    }
}
