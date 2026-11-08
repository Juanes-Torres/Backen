package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regla de vigencia de la garantía: fecha de compra + meses (SCRUM-74).
 */
class GarantiaTest {

    private static final LocalDate COMPRA = LocalDate.of(2026, 1, 31);

    @Test
    void terminaAlSumarLosMesesALaFechaDeCompra() {
        assertEquals(LocalDate.of(2027, 1, 31), new Garantia(COMPRA, 12).fechaFin());
        // 31 de enero + 1 mes = último día de febrero
        assertEquals(LocalDate.of(2026, 2, 28), new Garantia(COMPRA, 1).fechaFin());
    }

    @Test
    void estaVigenteHastaElUltimoDiaIncluido() {
        Garantia garantia = new Garantia(COMPRA, 12);

        assertTrue(garantia.vigenteEl(COMPRA));
        assertTrue(garantia.vigenteEl(LocalDate.of(2027, 1, 31)));
        assertFalse(garantia.vigenteEl(LocalDate.of(2027, 2, 1)));
    }

    @Test
    void unProductoConCeroMesesNoTieneGarantia() {
        assertFalse(new Garantia(COMPRA, 0).vigenteEl(COMPRA));
    }

    @Test
    void necesitaFechaDeCompraYMesesNoNegativos() {
        assertThrows(ReglaNegocioException.class, () -> new Garantia(null, 12));
        assertThrows(ReglaNegocioException.class, () -> new Garantia(COMPRA, -1));
    }

    @Test
    void elProductoTiene12MesesPorDefecto_yNoAceptaMesesNegativos() {
        Producto sinPlazo = Producto.nuevo("Galaxy", null, BigDecimal.TEN, null, 1L, 1L, null);
        assertEquals(Producto.MESES_GARANTIA_POR_DEFECTO, sinPlazo.getMesesGarantia());
        assertEquals(LocalDate.of(2027, 1, 31), sinPlazo.garantiaDesde(COMPRA).fechaFin());

        assertEquals(24, Producto.nuevo("TV", null, BigDecimal.TEN, null, 1L, 1L, 24).getMesesGarantia());
        assertThrows(ReglaNegocioException.class,
                () -> Producto.nuevo("TV", null, BigDecimal.TEN, null, 1L, 1L, -3));
    }

    @Test
    void laCedulaEsOpcional_yAlLlenarseSoloLlevaDigitos() {
        Usuario cliente = Usuario.nuevo("Ana", "ana@correo.com", "hash", Rol.CLIENTE, null, null);
        cliente.asignarCedula(null);
        assertEquals(null, cliente.getCedula());

        cliente.asignarCedula(" 1020304050 ");
        assertEquals("1020304050", cliente.getCedula());

        assertThrows(ReglaNegocioException.class, () -> cliente.asignarCedula("10.203.040"));
        assertThrows(ReglaNegocioException.class, () -> cliente.asignarCedula("123"));
    }
}
