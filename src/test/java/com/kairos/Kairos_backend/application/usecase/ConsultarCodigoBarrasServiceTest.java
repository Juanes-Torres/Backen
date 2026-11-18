package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort.DatosProductoExterno;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El caso de uso se prueba SIN internet: el catálogo externo es una clase falsa (una lambda).
 */
class ConsultarCodigoBarrasServiceTest {

    private final List<String> consultados = new ArrayList<>();

    private final ConsultarCodigoBarrasService servicio = new ConsultarCodigoBarrasService(codigo -> {
        consultados.add(codigo.valor());
        return codigo.valor().equals("190199098572")
                ? Optional.of(new DatosProductoExterno("AirPods 2nd Generation", "Apple", null, null))
                : Optional.empty();
    });

    @Test
    void devuelveLosDatosDelCatalogoExterno() {
        DatosProductoExterno datos = servicio.consultar(" 190199098572 ");

        assertEquals("AirPods 2nd Generation", datos.nombre());
        assertEquals("Apple", datos.marca());
    }

    @Test
    void unCodigoDesconocidoRespondeNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.consultar("7702004003508"));
    }

    @Test
    void unCodigoMalEscritoNoGastaUnaConsultaDeLaApi() {
        assertThrows(ReglaNegocioException.class, () -> servicio.consultar("abc"));

        assertTrue(consultados.isEmpty());
    }
}
