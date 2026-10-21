package com.kairos.Kairos_backend.domain.model;

/**
 * Ciclo de vida de una empresa en la plataforma.
 * PENDIENTE → (aprobar) ACTIVA ⇄ (suspender / reactivar) SUSPENDIDA
 * PENDIENTE → (rechazar) RECHAZADA
 */
public enum EstadoEmpresa {
    PENDIENTE,
    ACTIVA,
    RECHAZADA,
    SUSPENDIDA
}
