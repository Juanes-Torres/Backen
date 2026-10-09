package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpresaTest {

    private Empresa nueva() {
        return Empresa.nueva("TecnoMundo S.A.S.", "901234567-8", "Contacto@TecnoMundo.com", "3001234567");
    }

    @Test
    void unaEmpresaNuevaQuedaPendiente_yNoPuedeOperar() {
        Empresa empresa = nueva();

        assertEquals(EstadoEmpresa.PENDIENTE, empresa.getEstado());
        assertFalse(empresa.estaActiva());
        assertEquals("contacto@tecnomundo.com", empresa.getEmail());
    }

    @Test
    void alAprobarlaQuedaActiva() {
        Empresa empresa = nueva();

        empresa.aprobar();

        assertEquals(EstadoEmpresa.ACTIVA, empresa.getEstado());
        assertTrue(empresa.estaActiva());
    }

    @Test
    void soloSePuedeRechazarUnaEmpresaPendiente() {
        Empresa empresa = nueva();
        empresa.aprobar();

        assertThrows(ReglaNegocioException.class, empresa::rechazar);
        assertEquals(EstadoEmpresa.ACTIVA, empresa.getEstado());
    }

    @Test
    void soloSePuedeSuspenderUnaEmpresaActiva() {
        Empresa pendiente = nueva();
        assertThrows(ReglaNegocioException.class, pendiente::suspender);

        Empresa activa = nueva();
        activa.aprobar();
        activa.suspender();
        assertEquals(EstadoEmpresa.SUSPENDIDA, activa.getEstado());
        assertFalse(activa.estaActiva());
    }

    @Test
    void sinNitONombre_lanzaError() {
        assertThrows(ReglaNegocioException.class,
                () -> Empresa.nueva("TecnoMundo", " ", "a@b.com", null));
        assertThrows(ReglaNegocioException.class,
                () -> Empresa.nueva("", "901234567-8", "a@b.com", null));
    }
}
