package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarGarantiasUseCase;
import com.kairos.Kairos_backend.application.port.in.ConsultarGarantiasUseCase.GarantiaDeProducto;
import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarAlmacenesUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarInventarioUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarReclamosUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort.HerramientaIa;
import com.kairos.Kairos_backend.application.port.out.AsistenteIaPort.ParametroIa;
import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Las "herramientas" del asistente: consultas que la IA puede pedir (SCRUM-78, 79 y 82).
 * Cada una llama a un caso de uso que YA existe con el Solicitante del token, así que respeta
 * los mismos permisos y la misma empresa que la API. La IA nunca ve la base de datos.
 *
 * Ley 1581: los resultados llevan solo lo necesario para responder (sin emails ni teléfonos).
 */
@Component
public class HerramientasAsistente {

    static final String HERRAMIENTA_GARANTIA = "consultar_garantia";
    static final String HERRAMIENTA_RECLAMOS = "consultar_reclamos";
    static final String HERRAMIENTA_STOCK = "stock_en_sedes";
    static final String HERRAMIENTA_VENTAS = "resumen_ventas";
    static final String HERRAMIENTA_BAJO_STOCK = "productos_bajo_stock";

    private static final int MAX_VENTAS_EN_RESUMEN = 200;

    private final ConsultarGarantiasUseCase garantias;
    private final GestionarReclamosUseCase reclamos;
    private final GestionarInventarioUseCase inventario;
    private final GestionarAlmacenesUseCase almacenes;
    private final ConsultarVentasUseCase ventas;
    private final Clock reloj;

    public HerramientasAsistente(ConsultarGarantiasUseCase garantias, GestionarReclamosUseCase reclamos,
                                 GestionarInventarioUseCase inventario, GestionarAlmacenesUseCase almacenes,
                                 ConsultarVentasUseCase ventas, Clock reloj) {
        this.garantias = garantias;
        this.reclamos = reclamos;
        this.inventario = inventario;
        this.almacenes = almacenes;
        this.ventas = ventas;
        this.reloj = reloj;
    }

    /** Las herramientas que puede usar este usuario: el vendedor atiende clientes, el admin además ve el negocio. */
    public List<HerramientaIa> disponiblesPara(Solicitante solicitante) {
        List<HerramientaIa> deVendedor = List.of(
                new HerramientaIa(HERRAMIENTA_GARANTIA,
                        "Busca las compras de un cliente por su cédula o por el número de factura, con la fecha "
                                + "de fin de la garantía de cada producto y si sigue vigente. Envía solo uno de los dos.",
                        List.of(new ParametroIa("cedula", "string", "Cédula del cliente, solo números", false),
                                new ParametroIa("factura", "string", "Número de factura, por ejemplo F-000001", false))),
                new HerramientaIa(HERRAMIENTA_RECLAMOS,
                        "Lista los reclamos de garantía de una venta (id_venta sale de consultar_garantia), "
                                + "con su estado actual.",
                        List.of(new ParametroIa("id_venta", "integer", "Id de la venta", true))),
                new HerramientaIa(HERRAMIENTA_STOCK,
                        "Busca un producto por nombre en todas las sedes de la empresa y dice cuántas unidades "
                                + "hay en cada sede y en qué ciudad está.",
                        List.of(new ParametroIa("producto", "string", "Parte del nombre del producto, por ejemplo iPhone 15", true))));
        if (solicitante.rol() != Rol.ADMINISTRADOR) {
            return deVendedor;
        }
        return java.util.stream.Stream.concat(deVendedor.stream(), java.util.stream.Stream.of(
                new HerramientaIa(HERRAMIENTA_VENTAS,
                        "Resumen de ventas de la empresa en un periodo: número de ventas, total vendido y "
                                + "productos más vendidos. Sin fechas, son los últimos 7 días.",
                        List.of(new ParametroIa("desde", "string", "Fecha inicial aaaa-mm-dd", false),
                                new ParametroIa("hasta", "string", "Fecha final aaaa-mm-dd", false))),
                new HerramientaIa(HERRAMIENTA_BAJO_STOCK,
                        "Productos que están por debajo de su stock mínimo, por sede.", List.of())))
                .toList();
    }

    /** Ejecuta la herramienta que pidió la IA. Un error se devuelve como texto para que la IA lo explique. */
    public String ejecutar(Solicitante solicitante, String nombre, Map<String, Object> entrada) {
        boolean permitida = disponiblesPara(solicitante).stream().anyMatch(h -> h.nombre().equals(nombre));
        if (!permitida) {
            return "ERROR: la herramienta " + nombre + " no está disponible para este usuario.";
        }
        try {
            return switch (nombre) {
                case HERRAMIENTA_GARANTIA -> consultarGarantia(solicitante, texto(entrada, "cedula"), texto(entrada, "factura"));
                case HERRAMIENTA_RECLAMOS -> consultarReclamos(solicitante, entero(entrada, "id_venta"));
                case HERRAMIENTA_STOCK -> stockEnSedes(solicitante, texto(entrada, "producto"));
                case HERRAMIENTA_VENTAS -> resumenVentas(solicitante, fecha(entrada, "desde"), fecha(entrada, "hasta"));
                case HERRAMIENTA_BAJO_STOCK -> bajoStock(solicitante);
                default -> "ERROR: herramienta desconocida " + nombre;
            };
        } catch (ReglaNegocioException | RecursoNoEncontradoException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // ---------- Herramientas ----------

    private String consultarGarantia(Solicitante s, String cedula, String factura) {
        List<GarantiaDeProducto> encontradas = garantias.consultar(s, cedula, factura);
        if (encontradas.isEmpty()) {
            return "SIN RESULTADOS: no hay compras con esa " + (cedula != null ? "cédula" : "factura") + " en esta empresa.";
        }
        return encontradas.stream().map(g -> {
            var c = g.compra();
            return "id_venta=" + c.idVenta() + " | factura " + c.numeroFactura()
                    + " | compra " + c.fechaCompra().toLocalDate() + " | sede " + c.almacen()
                    + " | cliente " + (c.cliente() == null ? "sin registrar" : c.cliente())
                    + " | producto " + c.producto() + " (id " + c.idProducto() + ", cantidad " + c.cantidad() + ")"
                    + " | garantía " + c.mesesGarantia() + " meses, hasta " + g.fechaFin()
                    + " | " + (g.vigente() ? "VIGENTE" : "VENCIDA");
        }).collect(Collectors.joining("\n"));
    }

    private String consultarReclamos(Solicitante s, Long idVenta) {
        List<ReclamoGarantia> deLaVenta = reclamos.listar(s).stream()
                .filter(r -> r.getIdVenta().equals(idVenta))
                .toList();
        if (deLaVenta.isEmpty()) {
            return "SIN RESULTADOS: la venta " + idVenta + " no tiene reclamos de garantía.";
        }
        return deLaVenta.stream().map(r -> "reclamo #" + r.getId() + " | producto id " + r.getIdProducto()
                        + " | estado " + r.getEstado() + " | registrado " + r.getFechaRegistro().toLocalDate()
                        + " | motivo: " + r.getMotivo())
                .collect(Collectors.joining("\n"));
    }

    private String stockEnSedes(Solicitante s, String producto) {
        if (producto == null) {
            return "ERROR: falta el nombre del producto.";
        }
        String buscado = producto.toLowerCase(Locale.ROOT);
        Map<Long, String> ciudadDeSede = almacenes.listar(s.idEmpresa()).stream()
                .collect(Collectors.toMap(Almacen::getId, Almacen::getCiudad));
        List<InventarioDetalle> conStock = inventario.listar(s.idEmpresa(), null).stream()
                .filter(i -> i.producto().toLowerCase(Locale.ROOT).contains(buscado))
                .toList();
        if (conStock.isEmpty()) {
            return "SIN RESULTADOS: ningún producto de la empresa coincide con \"" + producto + "\".";
        }
        return conStock.stream().map(i -> i.producto() + " | sede " + i.almacen()
                        + " (" + ciudadDeSede.getOrDefault(i.idAlmacen(), "ciudad desconocida") + ")"
                        + " | " + i.cantidadDisponible() + " unidades")
                .collect(Collectors.joining("\n"));
    }

    private String resumenVentas(Solicitante s, LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta == null ? LocalDate.now(reloj) : hasta;
        LocalDate inicio = desde == null ? fin.minusDays(6) : desde;
        List<VentaResumen> lista = ventas.listar(s, new ConsultarVentasUseCase.Filtro(null, null, inicio, fin));
        if (lista.isEmpty()) {
            return "SIN RESULTADOS: no hubo ventas entre " + inicio + " y " + fin + ".";
        }
        BigDecimal total = lista.stream().map(VentaResumen::total).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Unidades vendidas por producto (de las ventas más recientes, hasta un máximo)
        Map<String, Integer> unidades = new LinkedHashMap<>();
        lista.stream().limit(MAX_VENTAS_EN_RESUMEN)
                .flatMap(v -> ventas.buscar(s, v.id()).lineas().stream())
                .forEach(l -> unidades.merge(l.producto(), l.cantidad(), Integer::sum));
        String masVendidos = unidades.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> e.getKey() + " (" + e.getValue() + " unidades)")
                .collect(Collectors.joining(", "));

        Map<String, Long> porSede = lista.stream()
                .collect(Collectors.groupingBy(VentaResumen::almacen, LinkedHashMap::new, Collectors.counting()));
        return "periodo " + inicio + " a " + fin + " | ventas " + lista.size() + " | total $" + total.toPlainString()
                + "\nmás vendidos: " + masVendidos
                + "\nventas por sede: " + porSede.entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(", "));
    }

    private String bajoStock(Solicitante s) {
        List<InventarioDetalle> bajos = inventario.listarBajoStock(s.idEmpresa());
        if (bajos.isEmpty()) {
            return "SIN RESULTADOS: ningún producto está por debajo de su stock mínimo.";
        }
        return bajos.stream().map(i -> i.producto() + " | sede " + i.almacen() + " | quedan " + i.cantidadDisponible()
                        + " (mínimo " + i.stockMinimo() + ")")
                .collect(Collectors.joining("\n"));
    }

    // ---------- Lectura de los parámetros que manda la IA ----------

    private static String texto(Map<String, Object> entrada, String clave) {
        Object valor = entrada == null ? null : entrada.get(clave);
        String texto = valor == null ? null : valor.toString().trim();
        return texto == null || texto.isEmpty() ? null : texto;
    }

    private static Long entero(Map<String, Object> entrada, String clave) {
        return convertir(texto(entrada, clave), clave, v -> (long) Double.parseDouble(v));
    }

    private static LocalDate fecha(Map<String, Object> entrada, String clave) {
        return convertir(texto(entrada, clave), clave, LocalDate::parse);
    }

    private static <T> T convertir(String valor, String clave, Function<String, T> conversion) {
        if (valor == null) {
            return null;
        }
        try {
            return conversion.apply(valor);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ReglaNegocioException("El parámetro " + clave + " no es válido: " + valor);
        }
    }
}
