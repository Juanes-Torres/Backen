package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.PreguntarAsistenteUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort;
import com.kairos.Kairos_backend.application.port.out.UsoAsistentePort;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * El asistente virtual (SCRUM-77): recibe la pregunta, le pasa a la IA solo las herramientas que este
 * usuario puede usar y anota qué consultas hizo. Sin @Transactional: la IA puede tardar varios segundos
 * y cada herramienta abre su propia transacción de lectura.
 */
@Service
public class AsistenteService implements PreguntarAsistenteUseCase {

    static final int MAX_CARACTERES = 1000;

    private final AsistenteIaPort ia;
    private final UsoAsistentePort uso;
    private final HerramientasAsistente herramientas;
    private final Clock reloj;
    private final int limiteDiario;

    public AsistenteService(AsistenteIaPort ia, UsoAsistentePort uso, HerramientasAsistente herramientas, Clock reloj,
                            @Value("${kairos.asistente.limite-diario}") int limiteDiario) {
        this.ia = ia;
        this.uso = uso;
        this.herramientas = herramientas;
        this.reloj = reloj;
        this.limiteDiario = limiteDiario;
    }

    @Override
    public RespuestaAsistente preguntar(Solicitante solicitante, String pregunta) {
        boolean empleado = (solicitante.rol() == Rol.ADMINISTRADOR || solicitante.rol() == Rol.VENDEDOR)
                && solicitante.idEmpresa() != null;
        if (!empleado) {
            throw new ReglaNegocioException("Por ahora el asistente es solo para vendedores y administradores");
        }
        String texto = pregunta == null ? "" : pregunta.trim();
        if (texto.isEmpty()) {
            throw new ReglaNegocioException("Escribe una pregunta para el asistente");
        }
        if (texto.length() > MAX_CARACTERES) {
            throw new ReglaNegocioException("La pregunta admite máximo " + MAX_CARACTERES + " caracteres");
        }

        // Límite diario: se revisa ANTES de llamar a la IA (cada pregunta cuesta) y se cuenta solo si respondió
        LocalDate hoy = LocalDate.now(reloj);
        if (uso.preguntasDe(solicitante.idUsuario(), hoy) >= limiteDiario) {
            throw new ReglaNegocioException("Llegaste al límite de " + limiteDiario + " preguntas al asistente por hoy");
        }

        List<ConsultaHecha> consultas = new ArrayList<>();
        String respuesta = ia.responder(instrucciones(solicitante, hoy), texto, herramientas.disponiblesPara(solicitante),
                (nombre, entrada) -> {
                    consultas.add(new ConsultaHecha(nombre, new TreeMap<>(entrada == null ? Map.of() : entrada).toString()));
                    return herramientas.ejecutar(solicitante, nombre, entrada);
                });

        int usadas = uso.registrarPregunta(solicitante.idUsuario(), hoy);
        return new RespuestaAsistente(respuesta, List.copyOf(consultas), Math.max(0, limiteDiario - usadas));
    }

    /** Lo que la IA debe saber y respetar. No lleva datos personales: los datos llegan solo por las herramientas. */
    static String instrucciones(Solicitante solicitante, LocalDate hoy) {
        String rol = solicitante.rol() == Rol.ADMINISTRADOR ? "administrador" : "vendedor";
        return """
                Eres el asistente virtual de KAIRÓS, un sistema de inventario y ventas para tiendas de tecnología en Colombia.
                Hablas con un %s de una tienda. Hoy es %s.

                Reglas:
                - Responde en español, en pocas frases, claro y directo.
                - Usa SOLO los datos que devuelven las herramientas. Nunca inventes facturas, fechas, cantidades ni precios.
                - Si una herramienta dice SIN RESULTADOS o ERROR, dilo con tus palabras y sugiere qué dato revisar.
                - Si la pregunta no tiene que ver con garantías, inventario o ventas de la tienda, dilo amablemente.
                - Si una garantía está VIGENTE, ofrece registrar el reclamo en la sección Garantías.
                - No repitas datos personales que no hagan falta para responder (por ejemplo, la cédula completa).
                """.formatted(rol, hoy);
    }
}
