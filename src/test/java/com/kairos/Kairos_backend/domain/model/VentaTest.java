package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VentaTest {

    private static Venta venta(List<DetalleVenta> detalles) {
        return Venta.nueva("F-000001", 1L, 10L, 5L, null, detalles);
    }

    @Test
    void elTotalEsLaSumaDeLasLineas() {
        Venta v = venta(List.of(
                new DetalleVenta(1L, 2, new BigDecimal("1500000")),     // 3.000.000
                new DetalleVenta(2L, 3, new BigDecimal("50000.50"))));  //   150.001,50

        assertEquals(new BigDecimal("3150001.50"), v.getTotal());
        assertEquals(EstadoVenta.COMPLETADA, v.getEstado());
    }

    @Test
    void unaVentaSinProductos_lanzaError() {
        assertThrows(ReglaNegocioException.class, () -> venta(List.of()));
    }

    @Test
    void unProductoRepetido_lanzaError() {
        assertThrows(ReglaNegocioException.class, () -> venta(List.of(
                new DetalleVenta(1L, 1, BigDecimal.TEN),
                new DetalleVenta(1L, 2, BigDecimal.TEN))));
    }

    @Test
    void unaLineaConCantidadCero_lanzaError() {
        assertThrows(ReglaNegocioException.class, () -> new DetalleVenta(1L, 0, BigDecimal.TEN));
    }

    @Test
    void elNumeroDeFacturaEsConsecutivoConCeros() {
        assertEquals("F-000042", Venta.numeroFactura(42));
    }
}
