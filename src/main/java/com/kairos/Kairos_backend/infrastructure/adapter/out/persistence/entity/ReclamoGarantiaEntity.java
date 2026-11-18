package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Espejo de la tabla "reclamo_garantia". Al guardarlo se guarda también su historial (cascade).
 */
@Entity
@Table(name = "reclamo_garantia")
public class ReclamoGarantiaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclamo")
    private Long id;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "id_registrado_por", nullable = false)
    private Long idRegistradoPor;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reclamo", nullable = false)
    @OrderBy("fecha ASC, id ASC")
    private List<SeguimientoReclamoEntity> historial = new ArrayList<>();

    protected ReclamoGarantiaEntity() {
    }

    public ReclamoGarantiaEntity(Long id, Long idEmpresa, Long idVenta, Long idProducto, Long idCliente, String motivo,
                                 String estado, LocalDateTime fechaRegistro, Long idRegistradoPor,
                                 List<SeguimientoReclamoEntity> historial) {
        this.id = id;
        this.idEmpresa = idEmpresa;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.idCliente = idCliente;
        this.motivo = motivo;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.idRegistradoPor = idRegistradoPor;
        this.historial = new ArrayList<>(historial);
    }

    /** Al cambiar de estado: se actualiza el estado y se AGREGA el paso nuevo, sin reescribir el historial. */
    public void avanzar(String nuevoEstado, SeguimientoReclamoEntity paso) {
        this.estado = nuevoEstado;
        this.historial.add(paso);
    }

    public Long getId() { return id; }
    public Long getIdEmpresa() { return idEmpresa; }
    public Long getIdVenta() { return idVenta; }
    public Long getIdProducto() { return idProducto; }
    public Long getIdCliente() { return idCliente; }
    public String getMotivo() { return motivo; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public Long getIdRegistradoPor() { return idRegistradoPor; }
    public List<SeguimientoReclamoEntity> getHistorial() { return historial; }
}
