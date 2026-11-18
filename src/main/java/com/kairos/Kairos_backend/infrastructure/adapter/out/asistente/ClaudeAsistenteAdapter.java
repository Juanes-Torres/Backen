package com.kairos.Kairos_backend.infrastructure.adapter.out.asistente;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ADAPTADOR de AsistenteIaPort con la API de Claude (SDK oficial de Anthropic).
 * Solo se activa si kairos.asistente.enabled=true; la clave sale de ANTHROPIC_API_KEY (nunca va en Git).
 *
 * Ciclo de herramientas: Claude pide una consulta (tool_use) → KAIRÓS la ejecuta con los permisos
 * del usuario → se le devuelve el resultado (tool_result) → se repite hasta que Claude responde.
 */
@Component
@ConditionalOnProperty(name = "kairos.asistente.enabled", havingValue = "true")
public class ClaudeAsistenteAdapter implements AsistenteIaPort {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAsistenteAdapter.class);

    private static final long MAX_TOKENS = 16_000L;
    private static final int MAX_VUELTAS = 6;   // tope de idas y vueltas con herramientas por pregunta

    private final AnthropicClient cliente;
    private final String modelo;

    public ClaudeAsistenteAdapter(@Value("${kairos.asistente.api-key}") String apiKey,
                                  @Value("${kairos.asistente.modelo}") String modelo,
                                  @Value("${kairos.asistente.timeout-ms}") long timeoutMs,
                                  @Value("${kairos.asistente.url}") String url) {
        if (apiKey == null || apiKey.isBlank()) {
            // Mejor fallar al arrancar que en la primera pregunta (12 factores: III)
            throw new IllegalStateException("El asistente está encendido pero falta la variable ANTHROPIC_API_KEY");
        }
        this.cliente = AnthropicOkHttpClient.builder()
                .baseUrl(url)
                .apiKey(apiKey)
                .timeout(Duration.ofMillis(timeoutMs))
                .maxRetries(2)
                .build();
        this.modelo = modelo;
    }

    @Override
    public String responder(String instrucciones, String pregunta, List<HerramientaIa> herramientas,
                            EjecutorHerramientas ejecutor) {
        MessageCreateParams.Builder conversacion = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(MAX_TOKENS)
                .system(instrucciones)
                // Si los filtros de seguridad del modelo rechazan la pregunta, la API reintenta sola con
                // otro modelo adecuado en vez de cortar la respuesta (server-side fallback)
                .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                .addUserMessage(pregunta);
        herramientas.stream().map(ClaudeAsistenteAdapter::aTool).forEach(conversacion::addTool);

        try {
            for (int vuelta = 0; vuelta < MAX_VUELTAS; vuelta++) {
                Message respuesta = cliente.messages().create(conversacion.build());
                StopReason motivo = respuesta.stopReason().orElse(StopReason.END_TURN);

                if (StopReason.REFUSAL.equals(motivo)) {
                    return "No puedo ayudar con esa consulta. Intenta preguntar sobre garantías, inventario o ventas.";
                }
                List<ToolUseBlock> pedidas = respuesta.content().stream()
                        .flatMap(bloque -> bloque.toolUse().stream())
                        .toList();
                if (!StopReason.TOOL_USE.equals(motivo) || pedidas.isEmpty()) {
                    String texto = texto(respuesta);
                    return StopReason.MAX_TOKENS.equals(motivo) ? texto + " (respuesta recortada)" : texto;
                }

                // La respuesta completa vuelve a la conversación (incluye el razonamiento, que no se modifica)
                conversacion.addMessage(respuesta);
                // Todos los resultados de esta vuelta van juntos en UN solo mensaje
                conversacion.addUserMessageOfContentBlockParams(pedidas.stream()
                        .map(pedida -> ContentBlockParam.ofToolResult(ejecutar(pedida, ejecutor)))
                        .toList());
            }
            return "La consulta necesitó demasiados pasos. Intenta con una pregunta más concreta.";
        } catch (AnthropicException e) {
            log.warn("No se pudo consultar la IA del asistente: {}", e.getMessage());
            throw new ServicioExternoNoDisponibleException("El asistente no está disponible en este momento");
        }
    }

    /** Ejecuta una herramienta; si algo inesperado falla, Claude recibe el error en vez de que se caiga todo. */
    private static ToolResultBlockParam ejecutar(ToolUseBlock pedida, EjecutorHerramientas ejecutor) {
        ToolResultBlockParam.Builder resultado = ToolResultBlockParam.builder().toolUseId(pedida.id());
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> entrada = pedida._input().convert(Map.class);
            return resultado.content(ejecutor.ejecutar(pedida.name(), entrada == null ? Map.of() : entrada)).build();
        } catch (RuntimeException e) {
            log.warn("Falló la herramienta {} del asistente: {}", pedida.name(), e.getMessage());
            return resultado.content("ERROR: no se pudo hacer la consulta " + pedida.name()).isError(true).build();
        }
    }

    private static Tool aTool(HerramientaIa herramienta) {
        Tool.InputSchema.Properties.Builder propiedades = Tool.InputSchema.Properties.builder();
        for (ParametroIa p : herramienta.parametros()) {
            Map<String, Object> esquema = new LinkedHashMap<>();
            esquema.put("type", p.tipo());
            esquema.put("description", p.descripcion());
            propiedades.putAdditionalProperty(p.nombre(), JsonValue.from(esquema));
        }
        return Tool.builder()
                .name(herramienta.nombre())
                .description(herramienta.descripcion())
                .inputSchema(Tool.InputSchema.builder()
                        .properties(propiedades.build())
                        .required(herramienta.parametros().stream().filter(ParametroIa::obligatorio)
                                .map(ParametroIa::nombre).toList())
                        .build())
                .build();
    }

    private static String texto(Message respuesta) {
        return respuesta.content().stream()
                .flatMap(bloque -> bloque.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();
    }
}
