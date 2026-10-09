package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE SALIDA: qué necesita la aplicación para guardar y buscar empresas.
 */
public interface EmpresaRepositoryPort {

    Empresa guardar(Empresa empresa);

    Optional<Empresa> buscarPorId(Long id);

    boolean existePorNit(String nit);

    List<Empresa> listarPorEstado(EstadoEmpresa estado);
}
