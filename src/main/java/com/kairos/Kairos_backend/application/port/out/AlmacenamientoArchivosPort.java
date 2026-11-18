package com.kairos.Kairos_backend.application.port.out;

import java.util.Optional;

/**
 * PUERTO DE SALIDA: guardar y leer archivos (hoy, fotos de productos; después, fotos de Créditos).
 * El caso de uso no sabe si por detrás hay una carpeta del servidor o un servicio en la nube:
 * cambiar de uno a otro es escribir otro adaptador.
 *
 * Las rutas son relativas y las arma la aplicación, por ejemplo "productos/15-a1b2c3.jpg".
 */
public interface AlmacenamientoArchivosPort {

    void guardar(String ruta, byte[] contenido);

    /** Vacío si el archivo no existe. */
    Optional<byte[]> leer(String ruta);

    /** No falla si el archivo ya no existe. */
    void borrar(String ruta);
}
