package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas del reclamo de garantía: estados permitidos e historial (SCRUM-75).
 */
class ReclamoGarantiaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 8, 10, 0);

    private ReclamoGarantia nuevo() {
        return ReclamoGarantia.nuevo(1L, 5L, 100L, 30L, "  No enciende  ", 7L, AHORA);
    }

    @Test
    void empiezaRecibido_yEsePrimerPasoQuedaEnElHistorial() {
        ReclamoGarantia reclamo = nuevo();

        assertEquals(EstadoReclamo.RECIBIDO, reclamo.getEstado());
        assertEquals("No enciende", reclamo.getMotivo());
        assertEquals(1, reclamo.getHistorial().size());
        assertEquals(7L, reclamo.getHistorial().get(0).idUsuario());
    }

    @Test
    void recorreElCaminoCompleto_yCadaPasoGuardaFechaQuienYNota() {
        ReclamoGarantia reclamo = nuevo();

        reclamo.cambiarEstado(EstadoReclamo.EN_REVISION, null, 8L, AHORA.plusHours(1));
        reclamo.cambiarEstado(EstadoReclamo.REPARADO, "Se cambió la batería", 8L, AHORA.plusDays(2));
        reclamo.cambiarEstado(EstadoReclamo.ENTREGADO, null, 7L, AHORA.plusDays(3));

        assertEquals(EstadoReclamo.ENTREGADO, reclamo.getEstado());
        assertEquals(4, reclamo.getHistorial().size());
        SeguimientoReclamo reparado = reclamo.getHistorial().get(2);
        assertEquals("Se cambió la batería", reparado.nota());
        assertEquals(8L, reparado.idUsuario());
        assertEquals(AHORA.plusDays(2), reparado.fecha());
    }

    @Test
    void noSeSaltaPasos_niSeMueveUnReclamoEntregado() {
        ReclamoGarantia reclamo = nuevo();
        assertThrows(ReglaNegocioException.class,
                () -> reclamo.cambiarEstado(EstadoReclamo.ENTREGADO, null, 8L, AHORA));   // falta revisarlo

        reclamo.cambiarEstado(EstadoReclamo.RECHAZADO, "Golpe, no cubre garantía", 8L, AHORA);
        reclamo.cambiarEstado(EstadoReclamo.ENTREGADO, null, 8L, AHORA);
        assertThrows(ReglaNegocioException.class,
                () -> reclamo.cambiarEstado(EstadoReclamo.EN_REVISION, null, 8L, AHORA));
    }

    @Test
    void mapaDeEstados() {
        assertTrue(EstadoReclamo.RECIBIDO.puedePasarA(EstadoReclamo.EN_REVISION));
        assertTrue(EstadoReclamo.EN_REVISION.puedePasarA(EstadoReclamo.CAMBIADO));
        assertFalse(EstadoReclamo.RECIBIDO.puedePasarA(EstadoReclamo.RECIBIDO));
        assertFalse(EstadoReclamo.ENTREGADO.puedePasarA(EstadoReclamo.RECIBIDO));
    }

    @Test
    void elMotivoEsObligatorio_yNoMuyLargo() {
        assertThrows(ReglaNegocioException.class, () -> ReclamoGarantia.nuevo(1L, 5L, 100L, 30L, " ", 7L, AHORA));
        assertThrows(ReglaNegocioException.class,
                () -> ReclamoGarantia.nuevo(1L, 5L, 100L, 30L, "x".repeat(501), 7L, AHORA));
        assertThrows(ReglaNegocioException.class, () -> nuevo().cambiarEstado(null, null, 8L, AHORA));
    }
}
