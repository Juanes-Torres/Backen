package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.EmpresaMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository jpaRepository;

    public EmpresaPersistenceAdapter(EmpresaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Empresa guardar(Empresa empresa) {
        return EmpresaMapper.toDomain(jpaRepository.save(EmpresaMapper.toEntity(empresa)));
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(EmpresaMapper::toDomain);
    }

    @Override
    public boolean existePorNit(String nit) {
        return jpaRepository.existsByNit(nit == null ? null : nit.trim());
    }

    @Override
    public List<Empresa> listarPorEstado(EstadoEmpresa estado) {
        return jpaRepository.findByEstadoOrderByFechaRegistroAsc(estado.name()).stream()
                .map(EmpresaMapper::toDomain).toList();
    }
}
