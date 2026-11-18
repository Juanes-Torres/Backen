package com.kairos.Kairos_backend.infrastructure.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Un archivo de más de 3 MB lo corta el servidor (Tomcat) antes de llegar al controller.
 * MockMvc no pasa por Tomcat, así que este error se prueba llamando directo al manejador.
 */
class ErroresDeSubidaTest {

    @Test
    void archivoMasGrandeQueElLimiteDelServidor_400ConMensajeClaro() {
        var respuesta = new GlobalExceptionHandler().archivoMuyGrande(new MaxUploadSizeExceededException(3_145_728));

        assertEquals(400, respuesta.getStatusCode().value());
        assertEquals("La imagen no puede pesar más de 2 MB", respuesta.getBody().mensaje());
    }
}
