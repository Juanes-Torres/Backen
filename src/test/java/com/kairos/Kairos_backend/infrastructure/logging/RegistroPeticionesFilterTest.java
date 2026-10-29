package com.kairos.Kairos_backend.infrastructure.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Prueba del filtro de logs SIN servidor: peticiones y respuestas de mentira (MockHttpServlet...).
 */
class RegistroPeticionesFilterTest {

    private final RegistroPeticionesFilter filtro = new RegistroPeticionesFilter();
    private final AtomicReference<String> idDuranteLaPeticion = new AtomicReference<>();

    @Test
    void cadaPeticionTieneUnId_queSaleEnLosLogsYEnLaRespuesta() throws Exception {
        MockHttpServletResponse respuesta = filtrar(new MockHttpServletRequest("GET", "/api/productos"));

        String id = respuesta.getHeader(RegistroPeticionesFilter.ENCABEZADO);
        assertNotNull(id);
        assertEquals(id, idDuranteLaPeticion.get());
    }

    @Test
    void siElFrontendMandaSuId_seUsaElMismo() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/api/productos");
        peticion.addHeader(RegistroPeticionesFilter.ENCABEZADO, "abc-123");

        assertEquals("abc-123", filtrar(peticion).getHeader(RegistroPeticionesFilter.ENCABEZADO));
    }

    @Test
    void unIdConCaracteresRaros_seReemplaza() throws Exception {
        MockHttpServletRequest peticion = new MockHttpServletRequest("GET", "/api/productos");
        peticion.addHeader(RegistroPeticionesFilter.ENCABEZADO, "falso\nERROR inventado en el log");

        String id = filtrar(peticion).getHeader(RegistroPeticionesFilter.ENCABEZADO);
        assertNotEquals("falso\nERROR inventado en el log", id);
        assertEquals(36, id.length());   // un UUID nuevo
    }

    @Test
    void alTerminar_noQuedanDatosParaLaSiguientePeticion() throws Exception {
        filtrar(new MockHttpServletRequest("GET", "/api/productos"));

        assertNull(MDC.get(ContextoLog.ID_PETICION));
        assertNull(MDC.get(ContextoLog.ID_USUARIO));
    }

    private MockHttpServletResponse filtrar(MockHttpServletRequest peticion) throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        filtro.doFilter(peticion, respuesta, (req, res) -> {
            idDuranteLaPeticion.set(MDC.get(ContextoLog.ID_PETICION));
            MDC.put(ContextoLog.ID_USUARIO, "7");   // como lo haría el filtro JWT
        });
        return respuesta;
    }
}
