package com.kairos.Kairos_backend.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Activa @Async: el correo se envía en segundo plano. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
