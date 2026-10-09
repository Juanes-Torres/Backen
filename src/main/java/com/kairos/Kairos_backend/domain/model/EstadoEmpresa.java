package com.kairos.Kairos_backend.domain.model;

/**
 * Ciclo de vida de una empresa en la plataforma.
 * PENDIENTE → (aprobar) ACTIVA → (suspender) SUSPENDIDA
 * PENDIENTE → (rechazar) RECHAZADA
 */
public enum EstadoEmpresa {
    PENDIENTE,
    ACTIVA,
    RECHAZADA,
    SUSPENDIDA
}
