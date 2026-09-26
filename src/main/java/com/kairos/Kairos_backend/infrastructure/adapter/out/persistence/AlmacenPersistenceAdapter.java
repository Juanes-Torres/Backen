package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.AlmacenMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.AlmacenJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AlmacenPersistenceAdapter implements AlmacenRepositoryPort {

    private final AlmacenJpaRepository jpaRepository;

    public AlmacenPersistenceAdapter(AlmacenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Almacen guardar(Almacen almacen) {
        return AlmacenMapper.toDomain(jpaRepository.save(AlmacenMapper.toEntity(almacen)));
    }

    @Override
    public Optional<Almacen> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(AlmacenMapper::toDomain);
    }

    @Override
    public List<Almacen> listarTodos() {
        return jpaRepository.findAllByOrderByNombreAsc().stream().map(AlmacenMapper::toDomain).toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }
}
