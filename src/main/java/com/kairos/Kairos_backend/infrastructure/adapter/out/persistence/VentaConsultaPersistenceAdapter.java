package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.ConsultaVentasPort;
import com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.VentaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR de ConsultaVentasPort: consultas de solo lectura sobre las ventas en PostgreSQL.
 */
@Component
public class VentaConsultaPersistenceAdapter implements ConsultaVentasPort {

    private final VentaJpaRepository jpaRepository;

    public VentaConsultaPersistenceAdapter(VentaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<VentaResumen> listar(FiltroVentas filtro) {
        return jpaRepository.listarResumen(filtro.idEmpresa(), filtro.idAlmacen(), filtro.idVendedor(),
                filtro.idCliente(), filtro.desde(), filtro.hasta());
    }

    @Override
    public Optional<VentaResumen> buscarResumen(Long idVenta) {
        return jpaRepository.buscarResumen(idVenta);
    }

    @Override
    public List<LineaVentaDetalle> lineasDe(Long idVenta) {
        return jpaRepository.lineasDe(idVenta);
    }
}
