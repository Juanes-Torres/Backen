package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

    @Test
    void entradaSumaUnidades() {
        Inventario inv = Inventario.nuevo(1L, 1L, 10, 5);
        inv.ajustar(TipoMovimiento.ENTRADA, 4);
        assertEquals(14, inv.getCantidadDisponible());
    }

    @Test
    void salidaRestaUnidades() {
        Inventario inv = Inventario.nuevo(1L, 1L, 10, 5);
        inv.ajustar(TipoMovimiento.SALIDA, 3);
        assertEquals(7, inv.getCantidadDisponible());
    }

    @Test
    void salidaMayorAlStock_lanzaError_yNoCambiaLaCantidad() {
        Inventario inv = Inventario.nuevo(1L, 1L, 2, 5);
        assertThrows(ReglaNegocioException.class, () -> inv.ajustar(TipoMovimiento.SALIDA, 3));
        assertEquals(2, inv.getCantidadDisponible());
    }

    @Test
    void cantidadCeroONegativa_lanzaError() {
        Inventario inv = Inventario.nuevo(1L, 1L, 10, 5);
        assertThrows(ReglaNegocioException.class, () -> inv.ajustar(TipoMovimiento.ENTRADA, 0));
        assertThrows(ReglaNegocioException.class, () -> inv.ajustar(TipoMovimiento.SALIDA, -1));
    }

    @Test
    void detectaBajoStock() {
        assertTrue(Inventario.nuevo(1L, 1L, 3, 5).estaBajoStock());
        assertFalse(Inventario.nuevo(1L, 1L, 5, 5).estaBajoStock());
    }
}
