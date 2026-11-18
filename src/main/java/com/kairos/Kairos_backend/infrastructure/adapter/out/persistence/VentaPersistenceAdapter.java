package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.VentaRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Venta;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.VentaMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.VentaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VentaPersistenceAdapter implements VentaRepositoryPort {

    private final VentaJpaRepository jpaRepository;

    public VentaPersistenceAdapter(VentaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Venta guardar(Venta venta) {
        return VentaMapper.toDomain(jpaRepository.save(VentaMapper.toEntity(venta)));
    }

    @Override
    public Optional<Venta> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(VentaMapper::toDomain);
    }

    @Override
    public long contarPorEmpresa(Long idEmpresa) {
        return jpaRepository.countByIdEmpresa(idEmpresa);
    }
}
