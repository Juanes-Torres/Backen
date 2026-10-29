package com.kairos.Kairos_backend.infrastructure.config;

import com.kairos.Kairos_backend.infrastructure.logging.ContextoLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * La tarea en segundo plano (el correo) debe llevar el id de la petición que la creó,
 * y no dejarlo pegado en el hilo cuando termina.
 */
class AsyncConfigTest {

    private final TaskDecorator decorador = new AsyncConfig().copiarContextoDeLogs();

    @AfterEach
    void limpiar() {
        MDC.clear();
    }

    @Test
    void laTareaEnSegundoPlanoConservaElIdDeLaPeticion_yLoLimpiaAlTerminar() throws Exception {
        MDC.put(ContextoLog.ID_PETICION, "registro-demo");
        AtomicReference<String> idEnLaTarea = new AtomicReference<>();
        Runnable tarea = decorador.decorate(() -> idEnLaTarea.set(MDC.get(ContextoLog.ID_PETICION)));
        MDC.clear();

        Thread hilo = new Thread(() -> {
            tarea.run();
            assertNull(MDC.get(ContextoLog.ID_PETICION));
        });
        hilo.start();
        hilo.join();

        assertEquals("registro-demo", idEnLaTarea.get());
    }

    @Test
    void sinDatosDeLog_laTareaCorreIgual() {
        AtomicReference<String> idEnLaTarea = new AtomicReference<>("sin correr");

        decorador.decorate(() -> idEnLaTarea.set(MDC.get(ContextoLog.ID_PETICION))).run();

        assertNull(idEnLaTarea.get());
    }
}
