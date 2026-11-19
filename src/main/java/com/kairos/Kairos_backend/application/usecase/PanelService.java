package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarPanelUseCase;
import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase;
import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase.Filtro;
import com.kairos.Kairos_backend.application.port.in.GestionarEmpresasUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarInventarioUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarReclamosUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Arma el panel con los casos de uso que ya existen: así cada número respeta los mismos permisos
 * que su pantalla (el vendedor ve solo sus ventas, cada empresa solo lo suyo).
 */
@Service
public class PanelService implements ConsultarPanelUseCase {

    private static final int MAS_VENDIDOS = 5;

    private final ConsultarVentasUseCase ventas;
    private final GestionarInventarioUseCase inventario;
    private final GestionarReclamosUseCase reclamos;
    private final GestionarEmpresasUseCase empresas;
    private final UsuarioRepositoryPort usuarios;
    private final Clock reloj;

    public PanelService(ConsultarVentasUseCase ventas, GestionarInventarioUseCase inventario,
                        GestionarReclamosUseCase reclamos, GestionarEmpresasUseCase empresas,
                        UsuarioRepositoryPort usuarios, Clock reloj) {
        this.ventas = ventas;
        this.inventario = inventario;
        this.reclamos = reclamos;
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @Override
    public Panel panel(Solicitante solicitante) {
        return switch (solicitante.rol()) {
            case SUPERADMIN -> new Panel(Rol.SUPERADMIN.name(), null, null, null, empresasPorEstado());
            case ADMINISTRADOR, VENDEDOR -> new Panel(solicitante.rol().name(), ventas(solicitante),
                    productosBajoStock(solicitante), reclamosAbiertos(solicitante), null);
            case CLIENTE -> throw new ReglaNegocioException("El panel de inicio es para los empleados");
        };
    }

    private Ventas ventas(Solicitante s) {
        LocalDate hoy = LocalDate.now(reloj);
        LocalDate inicioSemana = hoy.minusDays(6);
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate desde = inicioSemana.isBefore(inicioMes) ? inicioSemana : inicioMes;

        // Una sola consulta cubre los tres periodos; la regla de quién ve qué está en ConsultarVentas
        List<VentaResumen> lista = ventas.listar(s, new Filtro(null, null, desde, hoy));

        List<VentasDelDia> porDia = inicioSemana.datesUntil(hoy.plusDays(1))
                .map(dia -> {
                    Resumen r = resumir(lista, v -> dia(v).equals(dia));
                    return new VentasDelDia(dia, r.cantidad(), r.total());
                })
                .toList();
        return new Ventas(
                resumir(lista, v -> dia(v).equals(hoy)),
                resumir(lista, v -> !dia(v).isBefore(inicioSemana)),
                resumir(lista, v -> !dia(v).isBefore(inicioMes)),
                porDia,
                ventas.masVendidos(s, inicioMes, hoy, MAS_VENDIDOS));
    }

    private static LocalDate dia(VentaResumen venta) {
        return venta.fecha().toLocalDate();
    }

    private static Resumen resumir(List<VentaResumen> lista, Predicate<VentaResumen> condicion) {
        List<VentaResumen> elegidas = lista.stream().filter(condicion).toList();
        BigDecimal total = elegidas.stream().map(VentaResumen::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resumen(elegidas.size(), total);
    }

    /** El administrador ve toda su empresa; el vendedor solo su sede. */
    private int productosBajoStock(Solicitante s) {
        Long sedeDelVendedor = s.rol() == Rol.VENDEDOR
                ? usuarios.buscarPorId(s.idUsuario()).map(Usuario::getIdAlmacen).orElse(null)
                : null;
        return (int) inventario.listarBajoStock(s.idEmpresa()).stream()
                .filter(i -> s.rol() != Rol.VENDEDOR || i.idAlmacen().equals(sedeDelVendedor))
                .count();
    }

    private int reclamosAbiertos(Solicitante s) {
        return (int) reclamos.listar(s).stream().filter(r -> r.getEstado().estaAbierto()).count();
    }

    /** Todas los estados aparecen, aunque estén en cero, para que el frontend no tenga que suponer nada. */
    private Map<String, Long> empresasPorEstado() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (EstadoEmpresa estado : EstadoEmpresa.values()) {
            conteo.put(estado.name(), 0L);
        }
        empresas.listar(null).forEach(e -> conteo.merge(e.getEstado().name(), 1L, Long::sum));
        return conteo;
    }
}
