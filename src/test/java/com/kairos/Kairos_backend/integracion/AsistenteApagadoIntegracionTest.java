package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Con la configuración por defecto el asistente está APAGADO: la app arranca sin clave de API
 * y la pregunta responde 503 con un mensaje claro (como el correo deshabilitado).
 */
class AsistenteApagadoIntegracionTest extends PruebaIntegracion {

    @Test
    void sinClaveDeApi_elAsistenteRespondeQueEstaApagado() throws Exception {
        Long empresa = empresaActiva().getId();
        Usuario vendedor = usuario(Rol.VENDEDOR, empresa, almacen(empresa).getId());

        mvc.perform(con(vendedor, json(post("/api/asistente"), "{\"pregunta\":\"hola\"}")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("El asistente virtual está apagado en este ambiente"));
    }
}
