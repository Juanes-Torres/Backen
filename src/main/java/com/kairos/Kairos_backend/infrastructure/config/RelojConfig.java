package com.kairos.Kairos_backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * El reloj del sistema como bean: los casos de uso que dependen de "hoy" (vigencia de la garantía)
 * lo reciben inyectado, y en las pruebas se reemplaza por un reloj con una fecha fija.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
