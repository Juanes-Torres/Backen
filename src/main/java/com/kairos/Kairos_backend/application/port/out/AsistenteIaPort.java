package com.kairos.Kairos_backend.application.port.out;

import java.util.List;
import java.util.Map;

/**
 * PUERTO DE SALIDA: un modelo de IA que responde preguntas usando "herramientas" (tool use).
 * El caso de uso no sabe si por detrás hay Claude, otro proveedor o nada (adaptador apagado).
 *
 * La IA NO ve la base de datos: solo puede pedir que se ejecuten las herramientas que se le
 * ofrecen, y el {@link EjecutorHerramientas} las ejecuta con los permisos del usuario.
 */
public interface AsistenteIaPort {

    /**
     * Envía la pregunta, deja que la IA pida herramientas (el ejecutor las corre) y devuelve
     * el texto final. Si la IA no está disponible, lanza ServicioExternoNoDisponibleException.
     */
    String responder(String instrucciones, String pregunta, List<HerramientaIa> herramientas,
                     EjecutorHerramientas ejecutor);

    /** Una herramienta que la IA puede pedir: nombre, para qué sirve y sus parámetros. */
    record HerramientaIa(String nombre, String descripcion, List<ParametroIa> parametros) {
    }

    /** tipo: "string" o "integer". */
    record ParametroIa(String nombre, String tipo, String descripcion, boolean obligatorio) {
    }

    /** Ejecuta la herramienta que pidió la IA y devuelve el resultado como texto. */
    @FunctionalInterface
    interface EjecutorHerramientas {
        String ejecutar(String nombre, Map<String, Object> entrada);
    }
}
