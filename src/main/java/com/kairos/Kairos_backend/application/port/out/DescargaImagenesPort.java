package com.kairos.Kairos_backend.application.port.out;

import java.util.Optional;

/**
 * PUERTO DE SALIDA: descargar una foto de internet (las que sugiere el catálogo de códigos de barras).
 */
public interface DescargaImagenesPort {

    /**
     * Vacío si no se pudo descargar: la dirección no es válida o apunta a la red interna,
     * el servidor no respondió, tardó demasiado o el archivo pesa más de lo permitido.
     * El contenido NO viene validado: eso lo hace el dominio (Imagen).
     */
    Optional<byte[]> descargar(String url);
}
