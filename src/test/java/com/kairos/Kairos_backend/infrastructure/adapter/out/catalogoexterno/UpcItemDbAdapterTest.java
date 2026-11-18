package com.kairos.Kairos_backend.infrastructure.adapter.out.catalogoexterno;

import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort.DatosProductoExterno;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import com.kairos.Kairos_backend.domain.model.CodigoBarras;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Prueba del adaptador SIN internet: MockRestServiceServer simula las respuestas de UPCitemdb
 * (así no se gastan las 100 consultas diarias del plan gratis).
 */
class UpcItemDbAdapterTest {

    private static final String URL = "https://upc.prueba/prod/trial/lookup?upc=190199098572";
    private static final CodigoBarras CODIGO = new CodigoBarras("190199098572");

    private MockRestServiceServer api;
    private UpcItemDbAdapter adaptador;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        api = MockRestServiceServer.bindTo(builder).build();
        adaptador = new UpcItemDbAdapter(builder.baseUrl("https://upc.prueba").build());
    }

    @Test
    void traduceLaRespuestaDeLaApiALosDatosDelProducto() {
        api.expect(requestTo(URL)).andRespond(withSuccess("""
                {"code":"OK","total":1,"offset":0,"items":[{"ean":"0190199098572",
                 "title":"GENUINE Apple AirPods 2nd Generation","brand":"Apple","description":"N/A",
                 "category":"Electronics > Audio > Headphones","images":["x.jpg"]}]}
                """, MediaType.APPLICATION_JSON));

        DatosProductoExterno datos = adaptador.buscarPorCodigoBarras(CODIGO).orElseThrow();

        assertEquals("GENUINE Apple AirPods 2nd Generation", datos.nombre());
        assertEquals("Apple", datos.marca());
        assertNull(datos.descripcion());   // "N/A" se trata como vacío
        api.verify();
    }

    @Test
    void sinResultadosDevuelveVacio() {
        api.expect(requestTo(URL)).andRespond(withSuccess(
                "{\"code\":\"OK\",\"total\":0,\"offset\":0,\"items\":[]}", MediaType.APPLICATION_JSON));

        assertTrue(adaptador.buscarPorCodigoBarras(CODIGO).isEmpty());
    }

    @Test
    void unCodigoQueLaApiRechazaDevuelveVacio() {
        api.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"INVALID_UPC\"}"));

        assertEquals(Optional.empty(), adaptador.buscarPorCodigoBarras(CODIGO));
    }

    @Test
    void siLaApiEstaEnSuLimite_respondeServicioNoDisponible() {
        api.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"TOO_FAST\"}"));

        assertThrows(ServicioExternoNoDisponibleException.class, () -> adaptador.buscarPorCodigoBarras(CODIGO));
    }

    @Test
    void siLaApiFalla_respondeServicioNoDisponible() {
        api.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(ServicioExternoNoDisponibleException.class, () -> adaptador.buscarPorCodigoBarras(CODIGO));
    }
}
