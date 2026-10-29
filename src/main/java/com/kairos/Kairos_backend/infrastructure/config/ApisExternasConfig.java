package com.kairos.Kairos_backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Clientes HTTP para las APIs externas. La URL y el tiempo máximo de espera salen de
 * variables de entorno (12 factores: III y IV): cambiar de proveedor no exige tocar código.
 * Con un tiempo de espera corto, una API lenta nunca deja colgado a KAIRÓS.
 */
@Configuration
public class ApisExternasConfig {

    private final Duration tiempoEspera;

    public ApisExternasConfig(@Value("${kairos.apis.timeout-ms}") long timeoutMs) {
        this.tiempoEspera = Duration.ofMillis(timeoutMs);
    }

    /** API de códigos de barras (UPCitemdb). */
    @Bean
    public RestClient codigoBarrasRestClient(@Value("${kairos.apis.codigo-barras.url}") String url) {
        return cliente(url);
    }

    /** API de códigos QR (goQR.me). */
    @Bean
    public RestClient qrRestClient(@Value("${kairos.apis.qr.url}") String url) {
        return cliente(url);
    }

    private RestClient cliente(String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(tiempoEspera);
        fabrica.setReadTimeout(tiempoEspera);
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }
}
