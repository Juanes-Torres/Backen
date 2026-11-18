package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Espejo de la tabla "seguimiento_reclamo". La columna id_reclamo la llena ReclamoGarantiaEntity.
 */
@Entity
@Table(name = "seguimiento_reclamo")
public class SeguimientoReclamoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Long id;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "nota", length = 500)
    private String nota;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    protected SeguimientoReclamoEntity() {
    }

    public SeguimientoReclamoEntity(Long id, String estado, String nota, LocalDateTime fecha, Long idUsuario) {
        this.id = id;
        this.estado = estado;
        this.nota = nota;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
    }

    public Long getId() { return id; }
    public String getEstado() { return estado; }
    public String getNota() { return nota; }
    public LocalDateTime getFecha() { return fecha; }
    public Long getIdUsuario() { return idUsuario; }
}
