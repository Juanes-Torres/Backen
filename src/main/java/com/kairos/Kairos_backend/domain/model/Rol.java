package com.kairos.Kairos_backend.domain.model;

/**
 * Los tres tipos de usuario definidos en el Documento KAIRÓS (Tabla 1),
 * más el SUPERADMIN: el dueño de la plataforma, que aprueba las empresas.
 */
public enum Rol {
    CLIENTE,
    VENDEDOR,
    ADMINISTRADOR,
    SUPERADMIN
}
