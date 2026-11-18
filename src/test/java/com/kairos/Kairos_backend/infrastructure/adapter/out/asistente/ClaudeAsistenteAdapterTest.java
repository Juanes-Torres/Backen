package com.kairos.Kairos_backend.infrastructure.adapter.out.asistente;

import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort.HerramientaIa;
import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort.ParametroIa;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El adaptador de Claude probado SIN internet ni costo: un servidor HTTP local hace de API de
 * Anthropic y responde lo que la prueba le indica (primero pide una herramienta, luego responde).
 */
class ClaudeAsistenteAdapterTest {

    private static final List<HerramientaIa> HERRAMIENTAS = List.of(new HerramientaIa("stock_en_sedes",
            "Stock por sede", List.of(new ParametroIa("producto", "string", "Nombre", true))));

    private HttpServer servidor;
    private final Deque<String[]> respuestas = new ArrayDeque<>();   // {código HTTP, cuerpo}
    private final List<String> peticiones = new ArrayList<>();
    private final List<String> encabezadosBeta = new ArrayList<>();
    private ClaudeAsistenteAdapter adaptador;

    @BeforeEach
    void levantarApiFalsa() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/v1/messages", intercambio -> {
            peticiones.add(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            encabezadosBeta.add(intercambio.getRequestHeaders().getFirst("anthropic-beta"));
            String[] respuesta = respuestas.poll();
            byte[] cuerpo = respuesta[1].getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(Integer.parseInt(respuesta[0]), cuerpo.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
        servidor.start();
        adaptador = new ClaudeAsistenteAdapter("clave-de-prueba", "claude-opus-5-5", 5000,
                "http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void apagar() {
        servidor.stop(0);
    }

    private static String mensaje(String contenido, String motivo) {
        return """
                {"id":"msg_1","type":"message","role":"assistant","model":"claude-opus-5-5",
                 "content":[%s],"stop_reason":"%s","stop_sequence":null,
                 "usage":{"input_tokens":10,"output_tokens":5}}
                """.formatted(contenido, motivo);
    }

    @Test
    void cicloDeHerramientas_pideStock_recibeElResultado_yResponde() {
        respuestas.add(new String[]{"200", mensaje(
                "{\"type\":\"tool_use\",\"id\":\"toolu_1\",\"name\":\"stock_en_sedes\",\"input\":{\"producto\":\"iPhone 15\"}}",
                "tool_use")});
        respuestas.add(new String[]{"200", mensaje("{\"type\":\"text\",\"text\":\"Hay 3 en la sede de Chía.\"}", "end_turn")});
        List<Map<String, Object>> entradas = new ArrayList<>();

        String respuesta = adaptador.responder("Eres el asistente", "¿Hay iPhone 15?", HERRAMIENTAS, (nombre, entrada) -> {
            entradas.add(entrada);
            return "iPhone 15 | sede Chía (Chía) | 3 unidades";
        });

        assertEquals("Hay 3 en la sede de Chía.", respuesta);
        assertEquals("iPhone 15", entradas.get(0).get("producto"));
        assertEquals(2, peticiones.size());
        // 1ª petición: modelo, herramientas e instrucciones; con fallback del lado del servidor
        assertTrue(peticiones.get(0).contains("\"model\":\"claude-opus-5-5\""));
        assertTrue(peticiones.get(0).contains("\"name\":\"stock_en_sedes\""));
        assertTrue(peticiones.get(0).contains("\"fallbacks\":\"default\""));
        assertTrue(encabezadosBeta.get(0).contains("server-side-fallback-2026-07-01"));
        // 2ª petición: lleva el resultado de la herramienta
        assertTrue(peticiones.get(1).contains("\"tool_use_id\":\"toolu_1\""));
        assertTrue(peticiones.get(1).contains("3 unidades"));
    }

    @Test
    void siLaHerramientaFalla_claudeRecibeElErrorEnVezDeCaerse() {
        respuestas.add(new String[]{"200", mensaje(
                "{\"type\":\"tool_use\",\"id\":\"toolu_1\",\"name\":\"stock_en_sedes\",\"input\":{\"producto\":\"x\"}}",
                "tool_use")});
        respuestas.add(new String[]{"200", mensaje("{\"type\":\"text\",\"text\":\"No pude consultar el stock.\"}", "end_turn")});

        String respuesta = adaptador.responder("i", "p", HERRAMIENTAS, (nombre, entrada) -> {
            throw new IllegalStateException("base de datos caída");
        });

        assertEquals("No pude consultar el stock.", respuesta);
        assertTrue(peticiones.get(1).contains("\"is_error\":true"));
    }

    @Test
    void siElModeloRechazaLaPregunta_respondeUnMensajeClaro() {
        respuestas.add(new String[]{"200", mensaje("", "refusal")});

        String respuesta = adaptador.responder("i", "p", HERRAMIENTAS, (n, e) -> "");

        assertTrue(respuesta.startsWith("No puedo ayudar con esa consulta"));
    }

    @Test
    void siLaApiFalla_503ConMensajeClaro() {
        respuestas.add(new String[]{"401",
                "{\"type\":\"error\",\"error\":{\"type\":\"authentication_error\",\"message\":\"invalid x-api-key\"}}"});

        ServicioExternoNoDisponibleException error = assertThrows(ServicioExternoNoDisponibleException.class,
                () -> adaptador.responder("i", "p", HERRAMIENTAS, (n, e) -> ""));
        assertEquals("El asistente no está disponible en este momento", error.getMessage());
    }

    @Test
    void sinClaveDeApi_noArranca() {
        assertThrows(IllegalStateException.class,
                () -> new ClaudeAsistenteAdapter(" ", "claude-opus-5-5", 5000, "http://127.0.0.1:1"));
    }
}
