package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;

import java.util.List;

/**
 * CASO DE USO: el superadministrador revisa las empresas y decide si pueden operar.
 */
public interface GestionarEmpresasUseCase {

    /** Si el estado es nulo, devuelve todas. */
    List<Empresa> listar(EstadoEmpresa estado);

    Empresa buscarPorId(Long id);

    Empresa aprobar(Long id);

    Empresa rechazar(Long id);

    Empresa suspender(Long id);

    Empresa reactivar(Long id);
}
