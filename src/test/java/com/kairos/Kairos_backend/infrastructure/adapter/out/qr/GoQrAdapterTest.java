package com.kairos.Kairos_backend.infrastructure.adapter.out.qr;

import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Prueba del adaptador SIN internet: MockRestServiceServer simula la API goQR.
 */
class GoQrAdapterTest {

    private static final String ENLACE = "https://kairos.app/garantias?empresa=1&factura=F-000007";

    private MockRestServiceServer api;
    private GoQrAdapter adaptador;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        api = MockRestServiceServer.bindTo(builder).build();
        adaptador = new GoQrAdapter(builder.baseUrl("https://qr.prueba").build());
    }

    @Test
    void pideElPngConElEnlaceCodificado_yDevuelveLaImagen() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};
        // Los "&" y "=" del enlace deben ir codificados (%26, %3D) para no romper la URL de la API
        api.expect(requestTo(allOf(
                        containsString("https://qr.prueba/v1/create-qr-code/?size=300x300&format=png"),
                        containsString("data=https%3A%2F%2Fkairos.app%2Fgarantias%3Fempresa%3D1%26factura%3DF-000007"))))
                .andRespond(withSuccess(png, MediaType.IMAGE_PNG));

        assertArrayEquals(png, adaptador.generarPng(ENLACE));
        api.verify();
    }

    @Test
    void siLaApiFalla_respondeServicioNoDisponible() {
        api.expect(requestTo(containsString("/v1/create-qr-code/"))).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(ServicioExternoNoDisponibleException.class, () -> adaptador.generarPng(ENLACE));
    }

    @Test
    void siLaApiDevuelveUnaImagenVacia_respondeServicioNoDisponible() {
        api.expect(requestTo(containsString("/v1/create-qr-code/"))).andRespond(withSuccess(new byte[0], MediaType.IMAGE_PNG));

        assertThrows(ServicioExternoNoDisponibleException.class, () -> adaptador.generarPng(ENLACE));
    }
}
