package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.ProductoMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductoPersistenceAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository jpaRepository;

    public ProductoPersistenceAdapter(ProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Producto guardar(Producto producto) {
        return ProductoMapper.toDomain(jpaRepository.save(ProductoMapper.toEntity(producto)));
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(ProductoMapper::toDomain);
    }

    @Override
    public List<Producto> buscarEnCatalogo(Long idEmpresa, Long idCategoria, String nombre) {
        String texto = nombre == null ? "" : nombre.trim();   // "" = sin filtro por nombre
        return jpaRepository.buscarEnCatalogo(idEmpresa, idCategoria, texto).stream()
                .map(ProductoMapper::toDomain).toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }
}
