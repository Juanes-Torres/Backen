package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.CategoriaRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Categoria;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.CategoriaMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.CategoriaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository jpaRepository;

    public CategoriaPersistenceAdapter(CategoriaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        return CategoriaMapper.toDomain(jpaRepository.save(CategoriaMapper.toEntity(categoria)));
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(CategoriaMapper::toDomain);
    }

    @Override
    public List<Categoria> listarTodas() {
        return jpaRepository.findAllByOrderByNombreAsc().stream().map(CategoriaMapper::toDomain).toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return jpaRepository.existsByNombreIgnoreCase(nombre);
    }
}
