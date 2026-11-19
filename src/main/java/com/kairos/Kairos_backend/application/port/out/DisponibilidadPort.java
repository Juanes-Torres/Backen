package com.kairos.Kairos_backend.application.port.out;

import java.util.Collection;
import java.util.Set;

/**
 * PUERTO DE SALIDA: qué productos se pueden comprar ya (SCRUM-107).
 * Puerto pequeño y aparte del de inventario: el catálogo solo necesita esta pregunta.
 */
public interface DisponibilidadPort {

    /** De esos productos, los que tienen al menos una unidad en alguna sede ACTIVA. */
    Set<Long> disponibles(Collection<Long> idsProducto);
}
