package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Asistente virtual SIN IA real (SCRUM-80): la IA es una clase falsa que sigue un "guion" escrito
 * en cada prueba (qué herramientas pide). Así se comprueba que cada herramienta respeta los
 * permisos y la empresa del usuario, y que si la IA falla el sistema responde claro sin caerse.
 */
@TestPropertySource(properties = "kairos.asistente.limite-diario=3")
class AsistenteIntegracionTest extends PruebaIntegracion {

    /** IA falsa: ejecuta el guion de la prueba y anota qué herramientas se le ofrecieron. */
    static class IaGuionada implements AsistenteIaPort {
        Function<EjecutorHerramientas, String> guion = ejecutor -> "Hola";
        final List<String> herramientasOfrecidas = new ArrayList<>();

        @Override
        public String responder(String instrucciones, String pregunta, List<HerramientaIa> herramientas,
                                EjecutorHerramientas ejecutor) {
            herramientasOfrecidas.clear();
            herramientas.forEach(h -> herramientasOfrecidas.add(h.nombre()));
            return guion.apply(ejecutor);
        }
    }

    @TestConfiguration
    static class ConIaFalsa {
        @Bean
        @Primary
        IaGuionada iaGuionada() {
            return new IaGuionada();
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private IaGuionada ia;

    private Empresa empresa;
    private Usuario admin;
    private Usuario vendedor;
    private Usuario cliente;
    private Producto celular;
    private Almacen centro;

    @BeforeEach
    void preparar() throws Exception {
        empresa = empresaActiva();
        centro = almacenes.guardar(Almacen.nuevo(unico("Centro"), "Bogotá", "Cra 7", empresa.getId()));
        Almacen chia = almacenes.guardar(Almacen.nuevo(unico("Chia"), "Chía", "Calle 1", empresa.getId()));
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), centro.getId());
        cliente = usuario(Rol.CLIENTE, null, null);
        cliente.asignarCedula(String.valueOf(System.nanoTime()).substring(0, 10));
        cliente = usuarios.guardar(cliente);

        celular = productos.guardar(Producto.nuevo(unico("Galaxy A55"), null, new BigDecimal("1500000"), null,
                categoria().getId(), empresa.getId(), 12));
        inventario(celular.getId(), centro.getId(), 2, 5);    // bajo stock
        inventario(celular.getId(), chia.getId(), 7, 1);

        mvc.perform(con(vendedor, json(post("/api/ventas"), "{\"emailCliente\":\"" + cliente.getEmail()
                        + "\",\"items\":[{\"idProducto\":" + celular.getId() + ",\"cantidad\":1}]}")))
                .andExpect(status().isCreated());
        ia.guion = ejecutor -> "Hola";
    }

    private org.springframework.test.web.servlet.ResultActions preguntar(Usuario quien, String pregunta) throws Exception {
        return mvc.perform(con(quien, json(post("/api/asistente"), "{\"pregunta\":\"" + pregunta + "\"}")));
    }

    @Test
    void elVendedorConsultaUnaGarantiaPorCedula_yLaRespuestaDiceQueConsultasHizo() throws Exception {
        ia.guion = ejecutor -> ejecutor.ejecutar("consultar_garantia", Map.of("cedula", cliente.getCedula()));

        preguntar(vendedor, "El cliente tiene garantia?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta", containsString("VIGENTE")))
                .andExpect(jsonPath("$.respuesta", containsString(celular.getNombre())))
                .andExpect(jsonPath("$.respuesta", containsString(centro.getNombre())))
                .andExpect(jsonPath("$.consultas[0].herramienta").value("consultar_garantia"))
                .andExpect(jsonPath("$.preguntasRestantesHoy").value(2));
    }

    @Test
    void stockEnOtrasSedes_conCiudad() throws Exception {
        ia.guion = ejecutor -> ejecutor.ejecutar("stock_en_sedes", Map.of("producto", "galaxy a55"));

        preguntar(vendedor, "Hay Galaxy A55 en otra sede?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta", containsString("(Chía) | 7 unidades")))
                .andExpect(jsonPath("$.respuesta", containsString("(Bogotá) | 1 unidades")));
    }

    @Test
    void aislamiento_unVendedorDeOtraEmpresaNoVeEstosDatos() throws Exception {
        Long otra = empresaActiva().getId();
        Usuario ajeno = usuario(Rol.VENDEDOR, otra, almacen(otra).getId());
        ia.guion = ejecutor -> ejecutor.ejecutar("consultar_garantia", Map.of("cedula", cliente.getCedula()))
                + "\n" + ejecutor.ejecutar("stock_en_sedes", Map.of("producto", celular.getNombre()));

        preguntar(ajeno, "garantia y stock")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta", containsString("SIN RESULTADOS: no hay compras")))
                .andExpect(jsonPath("$.respuesta", containsString("SIN RESULTADOS: ningún producto")));
    }

    @Test
    void elVendedorNoTieneLasHerramientasDelAdmin_niPuedeForzarlas() throws Exception {
        ia.guion = ejecutor -> ejecutor.ejecutar("resumen_ventas", Map.of());

        preguntar(vendedor, "como van las ventas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta", containsString("no está disponible para este usuario")));
        assertFalse(ia.herramientasOfrecidas.contains("resumen_ventas"));
        assertTrue(ia.herramientasOfrecidas.contains("consultar_garantia"));
    }

    @Test
    void elAdminVeElResumenDeVentasYLoQueEstaPorAgotarse() throws Exception {
        ia.guion = ejecutor -> ejecutor.ejecutar("resumen_ventas", Map.of())
                + "\n" + ejecutor.ejecutar("productos_bajo_stock", Map.of());

        preguntar(admin, "Que se vendio mas esta semana y que esta por agotarse?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta", containsString("ventas 1 | total $1500000")))
                .andExpect(jsonPath("$.respuesta", containsString(celular.getNombre() + " (1 unidades)")))
                .andExpect(jsonPath("$.respuesta", containsString("quedan 1 (mínimo 5)")))
                .andExpect(jsonPath("$.consultas[*].herramienta", hasItem("productos_bajo_stock")));
    }

    @Test
    void parametrosMalos_seLeDevuelvenComoErrorALaIA() throws Exception {
        ia.guion = ejecutor -> ejecutor.ejecutar("resumen_ventas", Map.of("desde", "ayer"))
                + "\n" + ejecutor.ejecutar("consultar_garantia", Map.of());
        preguntar(admin, "ventas desde ayer")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta", containsString("ERROR: El parámetro desde no es válido")))
                .andExpect(jsonPath("$.respuesta", containsString("ERROR: Envíe la cédula o el número de factura")));
    }

    @Test
    void siLaIaFalla_503ConMensajeClaro_yNoGastaPreguntasDelDia() throws Exception {
        ia.guion = ejecutor -> {
            throw new ServicioExternoNoDisponibleException("El asistente no está disponible en este momento");
        };
        preguntar(vendedor, "hola")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("El asistente no está disponible en este momento"));

        ia.guion = ejecutor -> "Hola";
        preguntar(vendedor, "hola").andExpect(jsonPath("$.preguntasRestantesHoy").value(2));
    }

    @Test
    void limiteDePreguntasPorDia() throws Exception {
        preguntar(vendedor, "uno").andExpect(jsonPath("$.preguntasRestantesHoy").value(2));
        preguntar(vendedor, "dos").andExpect(jsonPath("$.preguntasRestantesHoy").value(1));
        preguntar(vendedor, "tres").andExpect(jsonPath("$.preguntasRestantesHoy").value(0));
        preguntar(vendedor, "cuatro")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("límite de 3 preguntas")));

        // El límite es por usuario: otro empleado sigue preguntando
        preguntar(admin, "uno").andExpect(status().isOk());
    }

    @Test
    void soloEmpleados_yLaPreguntaEsObligatoria() throws Exception {
        preguntar(cliente, "hola").andExpect(status().isForbidden());
        preguntar(vendedor, "").andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.pregunta").exists());
        mvc.perform(json(post("/api/asistente"), "{\"pregunta\":\"hola\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void laRespuestaNoIncluyeEmailsDeLosClientes() throws Exception {
        ia.guion = ejecutor -> ejecutor.ejecutar("consultar_garantia", Map.of("cedula", cliente.getCedula()));
        preguntar(vendedor, "garantia").andExpect(jsonPath("$.respuesta", not(containsString(cliente.getEmail()))));
    }
}
