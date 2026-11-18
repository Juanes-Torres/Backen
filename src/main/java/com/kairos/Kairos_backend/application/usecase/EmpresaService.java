package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarEmpresasActivasUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarEmpresasUseCase;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class EmpresaService implements GestionarEmpresasUseCase, ConsultarEmpresasActivasUseCase {

    private final EmpresaRepositoryPort empresaRepository;

    public EmpresaService(EmpresaRepositoryPort empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Override
    public List<Empresa> listar(EstadoEmpresa estado) {
        return estado == null ? empresaRepository.listarTodas() : empresaRepository.listarPorEstado(estado);
    }

    @Override
    public List<Empresa> listarActivas() {
        return empresaRepository.listarPorEstado(EstadoEmpresa.ACTIVA);
    }

    @Override
    public Empresa buscarPorId(Long id) {
        return empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una empresa con id " + id));
    }

    // Las reglas de qué cambio de estado se permite viven en el dominio (Empresa)

    @Override
    public Empresa aprobar(Long id) {
        return cambiarEstado(id, Empresa::aprobar);
    }

    @Override
    public Empresa rechazar(Long id) {
        return cambiarEstado(id, Empresa::rechazar);
    }

    @Override
    public Empresa suspender(Long id) {
        return cambiarEstado(id, Empresa::suspender);
    }

    @Override
    public Empresa reactivar(Long id) {
        return cambiarEstado(id, Empresa::reactivar);
    }

    private Empresa cambiarEstado(Long id, Consumer<Empresa> cambio) {
        Empresa empresa = buscarPorId(id);
        cambio.accept(empresa);
        return empresaRepository.guardar(empresa);
    }
}
