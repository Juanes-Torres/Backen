package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

/**
 * Código de barras de un producto (EAN-8, UPC-A, EAN-13 o GTIN-14): solo dígitos, de 8 a 14.
 * Se valida aquí, antes de gastar una consulta de la API externa.
 */
public record CodigoBarras(String valor) {

    public CodigoBarras {
        valor = valor == null ? null : valor.trim();
        if (valor == null || !valor.matches("\\d{8,14}")) {
            throw new ReglaNegocioException("El código de barras debe tener entre 8 y 14 dígitos (solo números)");
        }
    }
}
