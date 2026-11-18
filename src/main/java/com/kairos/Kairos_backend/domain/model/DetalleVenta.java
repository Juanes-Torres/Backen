package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;

/**
 * Una línea de la factura: qué producto, cuántas unidades y a qué precio.
 * El precio se COPIA del producto al vender: si después cambia, la factura no cambia.
 */
public class DetalleVenta {

    private final Long idProducto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleVenta(Long idProducto, int cantidad, BigDecimal precioUnitario) {
        if (idProducto == null) {
            throw new ReglaNegocioException("El producto es obligatorio");
        }
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Toma el precio actual del producto. */
    public static DetalleVenta de(Producto producto, int cantidad) {
        return new DetalleVenta(producto.getId(), cantidad, producto.getPrecio());
    }

    public Long getIdProducto() { return idProducto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
