package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.InventarioMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.InventarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InventarioPersistenceAdapter implements InventarioRepositoryPort {

    private final InventarioJpaRepository jpaRepository;

    public InventarioPersistenceAdapter(InventarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Inventario guardar(Inventario inventario) {
        return InventarioMapper.toDomain(jpaRepository.save(InventarioMapper.toEntity(inventario)));
    }

    @Override
    public Optional<Inventario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(InventarioMapper::toDomain);
    }

    @Override
    public boolean existePorProductoYAlmacen(Long idProducto, Long idAlmacen) {
        return jpaRepository.existsByIdProductoAndIdAlmacen(idProducto, idAlmacen);
    }

    @Override
    public boolean existePorProducto(Long idProducto) {
        return jpaRepository.existsByIdProducto(idProducto);
    }

    @Override
    public List<InventarioDetalle> listarDetalle(Long idAlmacen) {
        return idAlmacen == null
                ? jpaRepository.listarDetalle()
                : jpaRepository.listarDetallePorAlmacen(idAlmacen);
    }

    @Override
    public List<InventarioDetalle> listarBajoStock() {
        return jpaRepository.listarBajoStock();
    }

    @Override
    public Optional<InventarioDetalle> buscarDetallePorId(Long id) {
        return jpaRepository.buscarDetallePorId(id);
    }
}
