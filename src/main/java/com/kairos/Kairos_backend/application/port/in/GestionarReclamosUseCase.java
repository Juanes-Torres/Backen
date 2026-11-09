package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.EstadoReclamo;
import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;

import java.util.List;

/**
 * CASO DE USO: reclamos de garantía (SCRUM-75).
 * - Los empleados registran reclamos de su empresa y cambian su estado.
 * - El cliente ve el estado de sus reclamos.
 */
public interface GestionarReclamosUseCase {

    /** Solo si el producto está en esa factura y su garantía sigue vigente. */
    ReclamoGarantia registrar(Solicitante solicitante, RegistrarReclamoCommand command);

    ReclamoGarantia cambiarEstado(Solicitante solicitante, Long idReclamo, EstadoReclamo nuevo, String nota);

    /** Empleado: los de su empresa. Cliente: los suyos. */
    List<ReclamoGarantia> listar(Solicitante solicitante);

    ReclamoGarantia buscar(Solicitante solicitante, Long idReclamo);

    record RegistrarReclamoCommand(Long idVenta, Long idProducto, String motivo) {
    }
}
