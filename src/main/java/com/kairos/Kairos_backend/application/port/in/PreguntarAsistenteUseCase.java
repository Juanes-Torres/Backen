package com.kairos.Kairos_backend.application.port.in;

import java.util.List;

/**
 * CASO DE USO: preguntarle al asistente virtual (SCRUM-77).
 * Responde con datos reales de KAIRÓS, solo los que el usuario puede ver.
 */
public interface PreguntarAsistenteUseCase {

    RespuestaAsistente preguntar(Solicitante solicitante, String pregunta);

    /** El texto de la respuesta y qué consultas hizo el asistente para llegar a ella. */
    record RespuestaAsistente(String respuesta, List<ConsultaHecha> consultas, int preguntasRestantesHoy) {
    }

    record ConsultaHecha(String herramienta, String parametros) {
    }
}
