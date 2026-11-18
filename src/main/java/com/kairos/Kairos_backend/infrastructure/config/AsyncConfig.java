package com.kairos.Kairos_backend.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;

/** Activa @Async: el correo se envía en segundo plano. */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * La tarea en segundo plano corre en otro hilo: se le copian los datos del log
     * (idPeticion, usuario, empresa) para que sus líneas se puedan relacionar con la petición.
     */
    @Bean
    public TaskDecorator copiarContextoDeLogs() {
        return tarea -> {
            Map<String, String> contexto = MDC.getCopyOfContextMap();
            return () -> {
                if (contexto != null) {
                    MDC.setContextMap(contexto);
                }
                try {
                    tarea.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }
}
