package com.kairos.Kairos_backend.infrastructure.adapter.out.catalogoexterno;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import com.kairos.Kairos_backend.domain.model.CodigoBarras;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR de CatalogoExternoPort con la API pública UPCitemdb (plan gratis, sin clave).
 * Límites del plan gratis: 100 consultas al día y pocas por minuto (responde "TOO_FAST").
 */
@Component
public class UpcItemDbAdapter implements CatalogoExternoPort {

    private static final Logger log = LoggerFactory.getLogger(UpcItemDbAdapter.class);
    private static final String NO_DISPONIBLE =
            "El servicio de códigos de barras no está disponible en este momento; intente en un minuto o llene los datos a mano";

    private final RestClient restClient;

    public UpcItemDbAdapter(@Qualifier("codigoBarrasRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<DatosProductoExterno> buscarPorCodigoBarras(CodigoBarras codigo) {
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/prod/trial/lookup").queryParam("upc", codigo.valor()).build())
                    .exchange((peticion, respuesta) -> {
                        int estado = respuesta.getStatusCode().value();
                        if (estado == 400 || estado == 404) {
                            return Optional.<DatosProductoExterno>empty();   // código inválido o desconocido
                        }
                        if (!respuesta.getStatusCode().is2xxSuccessful()) {
                            // 429 = demasiadas consultas; 5xx = falla del proveedor
                            log.warn("UPCitemdb respondió {} para el código {}", estado, codigo.valor());
                            throw new ServicioExternoNoDisponibleException(NO_DISPONIBLE);
                        }
                        return aDatos(respuesta.bodyTo(RespuestaUpc.class));
                    });
        } catch (RestClientException e) {
            // Sin conexión o tardó más que el tiempo máximo de espera
            log.warn("No se pudo consultar UPCitemdb para el código {}: {}", codigo.valor(), e.getMessage());
            throw new ServicioExternoNoDisponibleException(NO_DISPONIBLE);
        }
    }

    private static Optional<DatosProductoExterno> aDatos(RespuestaUpc respuesta) {
        if (respuesta == null || respuesta.items() == null) {
            return Optional.empty();
        }
        return respuesta.items().stream()
                .filter(item -> item.title() != null && !item.title().isBlank())
                .findFirst()
                .map(item -> new DatosProductoExterno(item.title().trim(), vacioSiNoAplica(item.brand()),
                        vacioSiNoAplica(item.description()), vacioSiNoAplica(item.category())));
    }

    /** UPCitemdb escribe "N/A" cuando no conoce un dato. */
    private static String vacioSiNoAplica(String valor) {
        return valor == null || valor.isBlank() || valor.equalsIgnoreCase("N/A") ? null : valor.trim();
    }

    // Solo los campos que usa KAIRÓS; el resto de la respuesta se ignora

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaUpc(String code, List<ItemUpc> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ItemUpc(String title, String brand, String description, String category) {
    }
}
