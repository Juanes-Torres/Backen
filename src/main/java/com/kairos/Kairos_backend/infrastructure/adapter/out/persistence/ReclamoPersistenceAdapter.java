package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.ReclamoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;
import com.kairos.Kairos_backend.domain.model.SeguimientoReclamo;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.ReclamoGarantiaEntity;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper.ReclamoMapper;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository.ReclamoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReclamoPersistenceAdapter implements ReclamoRepositoryPort {

    private final ReclamoJpaRepository jpaRepository;

    public ReclamoPersistenceAdapter(ReclamoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReclamoGarantia guardar(ReclamoGarantia reclamo) {
        if (reclamo.getId() == null) {
            return ReclamoMapper.toDomain(jpaRepository.save(ReclamoMapper.toEntity(reclamo)));
        }
        // Reclamo existente: solo se agregan los pasos nuevos del historial (los anteriores no cambian)
        ReclamoGarantiaEntity entidad = jpaRepository.findById(reclamo.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un reclamo con id " + reclamo.getId()));
        List<SeguimientoReclamo> historial = reclamo.getHistorial();
        for (SeguimientoReclamo paso : historial.subList(entidad.getHistorial().size(), historial.size())) {
            entidad.avanzar(paso.estado().name(), ReclamoMapper.toEntity(paso));
        }
        return ReclamoMapper.toDomain(jpaRepository.save(entidad));
    }

    @Override
    public Optional<ReclamoGarantia> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(ReclamoMapper::toDomain);
    }

    @Override
    public List<ReclamoGarantia> listarPorEmpresa(Long idEmpresa) {
        return jpaRepository.findByIdEmpresaOrderByFechaRegistroDescIdDesc(idEmpresa).stream()
                .map(ReclamoMapper::toDomain).toList();
    }

    @Override
    public List<ReclamoGarantia> listarPorCliente(Long idCliente) {
        return jpaRepository.findByIdClienteOrderByFechaRegistroDescIdDesc(idCliente).stream()
                .map(ReclamoMapper::toDomain).toList();
    }
}
