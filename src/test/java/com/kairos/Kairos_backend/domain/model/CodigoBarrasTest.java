package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodigoBarrasTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "190199098572", "7702004003508", "12345678901234", " 190199098572 "})
    void aceptaDe8A14Digitos(String codigo) {
        assertDoesNotThrow(() -> new CodigoBarras(codigo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567", "123456789012345", "19019909857A", "190-199-098"})
    void rechazaLoQueNoEsUnCodigoDeBarras(String codigo) {
        assertThrows(ReglaNegocioException.class, () -> new CodigoBarras(codigo));
    }
}
