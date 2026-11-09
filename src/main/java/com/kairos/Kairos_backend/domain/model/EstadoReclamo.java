package com.kairos.Kairos_backend.domain.model;

import java.util.Set;

/**
 * Estados de un reclamo de garantía y a cuáles puede pasar cada uno (SCRUM-75):
 * RECIBIDO → EN_REVISION o RECHAZADO
 * EN_REVISION → REPARADO, CAMBIADO o RECHAZADO
 * REPARADO, CAMBIADO o RECHAZADO → ENTREGADO (el producto vuelve al cliente)
 * ENTREGADO es el final.
 */
public enum EstadoReclamo {
    RECIBIDO,
    EN_REVISION,
    REPARADO,
    CAMBIADO,
    RECHAZADO,
    ENTREGADO;

    public boolean puedePasarA(EstadoReclamo siguiente) {
        return siguientes().contains(siguiente);
    }

    private Set<EstadoReclamo> siguientes() {
        return switch (this) {
            case RECIBIDO -> Set.of(EN_REVISION, RECHAZADO);
            case EN_REVISION -> Set.of(REPARADO, CAMBIADO, RECHAZADO);
            case REPARADO, CAMBIADO, RECHAZADO -> Set.of(ENTREGADO);
            case ENTREGADO -> Set.of();
        };
    }
}
