package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void crearProductoValido() {
        Producto p = Producto.nuevo("  Galaxy A55 ", "Celular", new BigDecimal("1599000"), "Samsung", 1L);
        assertEquals("Galaxy A55", p.getNombre());
        assertNull(p.getId());
    }

    @Test
    void precioNegativo_lanzaError() {
        assertThrows(ReglaNegocioException.class, () ->
                Producto.nuevo("Galaxy", null, new BigDecimal("-1"), null, 1L));
    }

    @Test
    void sinCategoria_lanzaError() {
        assertThrows(ReglaNegocioException.class, () ->
                Producto.nuevo("Galaxy", null, BigDecimal.TEN, null, null));
    }
}
