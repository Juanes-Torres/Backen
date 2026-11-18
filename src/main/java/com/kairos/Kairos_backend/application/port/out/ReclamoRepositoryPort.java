package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE SALIDA: guardar y buscar reclamos de garantía con su historial.
 */
public interface ReclamoRepositoryPort {

    ReclamoGarantia guardar(ReclamoGarantia reclamo);

    Optional<ReclamoGarantia> buscarPorId(Long id);

    /** Reclamos de la empresa, del más reciente al más antiguo. */
    List<ReclamoGarantia> listarPorEmpresa(Long idEmpresa);

    /** Reclamos del cliente en todas las empresas, del más reciente al más antiguo. */
    List<ReclamoGarantia> listarPorCliente(Long idCliente);
}
