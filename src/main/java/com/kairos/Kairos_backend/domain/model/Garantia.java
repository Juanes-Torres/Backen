package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

/**
 * Garantía de un producto comprado: dura desde la fecha de compra hasta
 * fecha de compra + meses de garantía (SCRUM-74). Un producto con 0 meses no tiene garantía.
 */
public record Garantia(LocalDate fechaCompra, int meses) {

    public Garantia {
        if (fechaCompra == null) {
            throw new ReglaNegocioException("La garantía necesita la fecha de compra");
        }
        if (meses < 0) {
            throw new ReglaNegocioException("Los meses de garantía no pueden ser negativos");
        }
    }

    /** Último día en que el producto está cubierto. */
    public LocalDate fechaFin() {
        return fechaCompra.plusMonths(meses);
    }

    /** Vigente si tiene meses de garantía y el día consultado no pasa de la fecha de fin. */
    public boolean vigenteEl(LocalDate dia) {
        return meses > 0 && !dia.isBefore(fechaCompra) && !dia.isAfter(fechaFin());
    }
}
