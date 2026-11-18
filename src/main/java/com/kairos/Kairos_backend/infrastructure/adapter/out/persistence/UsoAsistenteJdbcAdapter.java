package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence;

import com.kairos.Kairos_backend.application.port.out.UsoAsistentePort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * ADAPTADOR de UsoAsistentePort: cuenta las preguntas por usuario y día en la tabla uso_asistente.
 * Usa SQL directo: sumar 1 es una sola instrucción atómica (INSERT ... ON CONFLICT), sin carreras
 * aunque haya varias copias del backend (12 factores: VIII).
 */
@Component
public class UsoAsistenteJdbcAdapter implements UsoAsistentePort {

    private final JdbcTemplate jdbc;

    public UsoAsistenteJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int preguntasDe(Long idUsuario, LocalDate fecha) {
        List<Integer> filas = jdbc.queryForList(
                "SELECT preguntas FROM uso_asistente WHERE id_usuario = ? AND fecha = ?",
                Integer.class, idUsuario, fecha);
        return filas.isEmpty() ? 0 : filas.get(0);
    }

    @Override
    public int registrarPregunta(Long idUsuario, LocalDate fecha) {
        Integer total = jdbc.queryForObject("""
                INSERT INTO uso_asistente (id_usuario, fecha, preguntas) VALUES (?, ?, 1)
                ON CONFLICT (id_usuario, fecha) DO UPDATE SET preguntas = uso_asistente.preguntas + 1
                RETURNING preguntas
                """, Integer.class, idUsuario, fecha);
        return total == null ? 0 : total;
    }
}
