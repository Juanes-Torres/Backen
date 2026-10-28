package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Venta (factura) hecha en una sede de una empresa (RF04).
 * El total siempre es la suma de sus líneas: nadie lo puede escribir a mano.
 */
public class Venta {

    private final Long id;
    private final String numeroFactura;
    private final Long idEmpresa;
    private final Long idAlmacen;
    private final Long idVendedor;
    private final Long idCliente;          // opcional: venta de mostrador sin cliente registrado
    private final LocalDateTime fecha;
    private final EstadoVenta estado;
    private final List<DetalleVenta> detalles;
    private final BigDecimal total;

    /** Reconstruye una venta que YA existe en la base de datos. */
    public Venta(Long id, String numeroFactura, Long idEmpresa, Long idAlmacen, Long idVendedor,
                 Long idCliente, LocalDateTime fecha, EstadoVenta estado, List<DetalleVenta> detalles) {
        if (numeroFactura == null || numeroFactura.isBlank()) {
            throw new ReglaNegocioException("El número de factura es obligatorio");
        }
        if (idEmpresa == null || idAlmacen == null || idVendedor == null) {
            throw new ReglaNegocioException("La venta debe tener empresa, almacén y vendedor");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ReglaNegocioException("La venta debe tener al menos un producto");
        }
        Set<Long> productos = new HashSet<>();
        for (DetalleVenta detalle : detalles) {
            if (!productos.add(detalle.getIdProducto())) {
                throw new ReglaNegocioException(
                        "El producto " + detalle.getIdProducto() + " está repetido; sume las cantidades en una sola línea");
            }
        }
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.idEmpresa = idEmpresa;
        this.idAlmacen = idAlmacen;
        this.idVendedor = idVendedor;
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.estado = estado == null ? EstadoVenta.COMPLETADA : estado;
        this.detalles = List.copyOf(detalles);
        this.total = this.detalles.stream().map(DetalleVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Una venta NUEVA queda COMPLETADA con la fecha de ahora. */
    public static Venta nueva(String numeroFactura, Long idEmpresa, Long idAlmacen, Long idVendedor,
                              Long idCliente, List<DetalleVenta> detalles) {
        return new Venta(null, numeroFactura, idEmpresa, idAlmacen, idVendedor, idCliente,
                LocalDateTime.now(), EstadoVenta.COMPLETADA, detalles);
    }

    /** Número consecutivo de factura dentro de la empresa: F-000001, F-000002... */
    public static String numeroFactura(long consecutivo) {
        return String.format("F-%06d", consecutivo);
    }

    public Long getId() { return id; }
    public String getNumeroFactura() { return numeroFactura; }
    public Long getIdEmpresa() { return idEmpresa; }
    public Long getIdAlmacen() { return idAlmacen; }
    public Long getIdVendedor() { return idVendedor; }
    public Long getIdCliente() { return idCliente; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoVenta getEstado() { return estado; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public BigDecimal getTotal() { return total; }
}
