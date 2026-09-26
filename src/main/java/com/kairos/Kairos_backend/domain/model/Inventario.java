package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

/**
 * Cuántas unidades de UN producto hay en UN almacén.
 * Aquí viven las reglas del stock.
 */
public class Inventario {

    private final Long id;
    private final Long idProducto;
    private final Long idAlmacen;
    private int cantidadDisponible;
    private int stockMinimo;

    public Inventario(Long id, Long idProducto, Long idAlmacen, int cantidadDisponible, int stockMinimo) {
        if (idProducto == null) {
            throw new ReglaNegocioException("El producto es obligatorio");
        }
        if (idAlmacen == null) {
            throw new ReglaNegocioException("El almacén es obligatorio");
        }
        if (cantidadDisponible < 0) {
            throw new ReglaNegocioException("La cantidad disponible no puede ser negativa");
        }
        if (stockMinimo < 0) {
            throw new ReglaNegocioException("El stock mínimo no puede ser negativo");
        }
        this.id = id;
        this.idProducto = idProducto;
        this.idAlmacen = idAlmacen;
        this.cantidadDisponible = cantidadDisponible;
        this.stockMinimo = stockMinimo;
    }

    public static Inventario nuevo(Long idProducto, Long idAlmacen, int cantidadInicial, int stockMinimo) {
        return new Inventario(null, idProducto, idAlmacen, cantidadInicial, stockMinimo);
    }

    /** Aplica una ENTRADA o una SALIDA de unidades. */
    public void ajustar(TipoMovimiento tipo, int cantidad) {
        if (tipo == null) {
            throw new ReglaNegocioException("El tipo de movimiento es obligatorio");
        }
        if (tipo == TipoMovimiento.ENTRADA) {
            registrarEntrada(cantidad);
        } else {
            registrarSalida(cantidad);
        }
    }

    public void registrarEntrada(int cantidad) {
        validarCantidadPositiva(cantidad);
        this.cantidadDisponible += cantidad;
    }

    public void registrarSalida(int cantidad) {
        validarCantidadPositiva(cantidad);
        if (cantidad > cantidadDisponible) {
            throw new ReglaNegocioException("Stock insuficiente: hay " + cantidadDisponible
                    + " unidades y se intentan sacar " + cantidad);
        }
        this.cantidadDisponible -= cantidad;
    }

    public void cambiarStockMinimo(int nuevoStockMinimo) {
        if (nuevoStockMinimo < 0) {
            throw new ReglaNegocioException("El stock mínimo no puede ser negativo");
        }
        this.stockMinimo = nuevoStockMinimo;
    }

    /** true si hay menos unidades que el mínimo: base de las alertas de stock (RF12). */
    public boolean estaBajoStock() {
        return cantidadDisponible < stockMinimo;
    }

    private void validarCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
    }

    public Long getId() { return id; }
    public Long getIdProducto() { return idProducto; }
    public Long getIdAlmacen() { return idAlmacen; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public int getStockMinimo() { return stockMinimo; }
}
