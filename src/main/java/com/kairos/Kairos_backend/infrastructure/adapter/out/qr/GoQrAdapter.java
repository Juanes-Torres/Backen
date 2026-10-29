package com.kairos.Kairos_backend.infrastructure.adapter.out.qr;

import com.kairos.Kairos_backend.application.port.out.GeneradorQrPort;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * ADAPTADOR de GeneradorQrPort con la API pública goQR.me (gratis, sin clave).
 */
@Component
public class GoQrAdapter implements GeneradorQrPort {

    private static final Logger log = LoggerFactory.getLogger(GoQrAdapter.class);
    private static final String NO_DISPONIBLE =
            "El servicio de códigos QR no está disponible en este momento; intente de nuevo en un minuto";
    private static final String TAMANO = "300x300";

    private final RestClient restClient;

    public GoQrAdapter(@Qualifier("qrRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public byte[] generarPng(String contenido) {
        try {
            // {contenido} es una variable de la URL: RestClient la codifica completa (los "&" y "=" del enlace incluidos)
            byte[] imagen = restClient.get()
                    .uri("/v1/create-qr-code/?size={tamano}&format=png&margin=10&data={contenido}", TAMANO, contenido)
                    .accept(MediaType.IMAGE_PNG)
                    .retrieve()
                    .body(byte[].class);
            if (imagen == null || imagen.length == 0) {
                throw new ServicioExternoNoDisponibleException(NO_DISPONIBLE);
            }
            return imagen;
        } catch (RestClientException e) {
            // Sin conexión, respuesta de error (4xx/5xx) o tardó más que el tiempo máximo
            log.warn("No se pudo generar el QR con goQR: {}", e.getMessage());
            throw new ServicioExternoNoDisponibleException(NO_DISPONIBLE);
        }
    }
}
