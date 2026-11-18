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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Espejo de la tabla "venta". Al guardarla se guardan también sus líneas (cascade).
 */
@Entity
@Table(name = "venta")
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(name = "numero_factura", nullable = false, length = 30)
    private String numeroFactura;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "id_almacen", nullable = false)
    private Long idAlmacen;

    /** En la base de datos se llama id_usuario: es el vendedor que hizo la venta. */
    @Column(name = "id_usuario", nullable = false)
    private Long idVendedor;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_venta", nullable = false)
    private List<DetalleVentaEntity> detalles = new ArrayList<>();

    protected VentaEntity() {
    }

    public VentaEntity(Long id, String numeroFactura, Long idEmpresa, Long idAlmacen, Long idVendedor,
                       Long idCliente, LocalDateTime fecha, BigDecimal total, String estado,
                       List<DetalleVentaEntity> detalles) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.idEmpresa = idEmpresa;
        this.idAlmacen = idAlmacen;
        this.idVendedor = idVendedor;
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.detalles = new ArrayList<>(detalles);
    }

    public Long getId() { return id; }
    public String getNumeroFactura() { return numeroFactura; }
    public Long getIdEmpresa() { return idEmpresa; }
    public Long getIdAlmacen() { return idAlmacen; }
    public Long getIdVendedor() { return idVendedor; }
    public Long getIdCliente() { return idCliente; }
    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getTotal() { return total; }
    public String getEstado() { return estado; }
    public List<DetalleVentaEntity> getDetalles() { return detalles; }
}
