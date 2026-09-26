package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.UsuarioMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return UsuarioMapper.toDomain(jpaRepository.save(UsuarioMapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(UsuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email.trim().toLowerCase()).map(UsuarioMapper::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpaRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpaRepository.findAll().stream().map(UsuarioMapper::toDomain).toList();
    }
}
