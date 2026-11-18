package com.kairos.Kairos_backend.application.port.out;

import java.time.LocalDate;

/**
 * PUERTO DE SALIDA: cuántas preguntas le hizo un usuario al asistente en un día (límite diario).
 */
public interface UsoAsistentePort {

    /** Cuántas preguntas lleva el usuario en esa fecha. */
    int preguntasDe(Long idUsuario, LocalDate fecha);

    /** Suma una pregunta al usuario en esa fecha y devuelve cuántas lleva ese día (incluida esta). */
    int registrarPregunta(Long idUsuario, LocalDate fecha);
}
