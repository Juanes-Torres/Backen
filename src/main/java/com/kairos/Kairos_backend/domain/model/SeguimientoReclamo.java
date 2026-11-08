package com.kairos.Kairos_backend.domain.model;

import java.time.LocalDateTime;

/** Un paso del historial del reclamo: a qué estado pasó, cuándo, quién lo hizo y una nota opcional. */
public record SeguimientoReclamo(EstadoReclamo estado, String nota, LocalDateTime fecha, Long idUsuario) {
}
