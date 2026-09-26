package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void crearClienteNuevo_quedaActivoYConEmailEnMinusculas() {

        Usuario usuario = Usuario.nuevo(
                "Juan Torres",
                "  JUAN@Mail.com ",
                "hash",
                Rol.CLIENTE,
                null,
                null
        );

        assertEquals("juan@mail.com", usuario.getEmail());
        assertTrue(usuario.isActivo());
        assertNull(usuario.getId());
    }

    @Test
    void crearVendedorSinAlmacen_lanzaError() {

        assertThrows(ReglaNegocioException.class, () ->
                Usuario.nuevo(
                        "Ana",
                        "ana@kairos.com",
                        "hash",
                        Rol.VENDEDOR,
                        null,
                        null
                )
        );
    }

    @Test
    void crearUsuarioConEmailInvalido_lanzaError() {

        assertThrows(ReglaNegocioException.class, () ->
                Usuario.nuevo(
                        "Ana",
                        "correo-sin-arroba",
                        "hash",
                        Rol.CLIENTE,
                        null,
                        null
                )
        );
    }
}