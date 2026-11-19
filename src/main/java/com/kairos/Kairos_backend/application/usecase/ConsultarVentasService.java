package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.ConsultaVentasPort;
import com.kairos.Kairos_backend.application.port.out.ConsultaVentasPort.FiltroVentas;
import com.kairos.Kairos_backend.application.port.out.ProductoVendido;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarVentasService implements ConsultarVentasUseCase {

    // Sin fechas se busca en todo el historial
    private static final LocalDate PRIMER_DIA = LocalDate.of(2000, 1, 1);

    private final ConsultaVentasPort consultaVentas;

    public ConsultarVentasService(ConsultaVentasPort consultaVentas) {
        this.consultaVentas = consultaVentas;
    }

    @Override
    public List<VentaResumen> listar(Solicitante solicitante, Filtro filtro) {
        LocalDate desde = filtro.desde() == null ? PRIMER_DIA : filtro.desde();
        LocalDate hasta = filtro.hasta() == null ? LocalDate.now() : filtro.hasta();
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return consultaVentas.listar(filtroPara(solicitante, filtro.idAlmacen(), filtro.idVendedor(), desde, hasta));
    }

    @Override
    public List<ProductoVendido> masVendidos(Solicitante solicitante, LocalDate desde, LocalDate hasta, int limite) {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        return consultaVentas.masVendidos(filtroPara(solicitante, null, null, desde, hasta), limite);
    }

    /** La regla de quién ve qué ventas, en un solo lugar (la usan listar y masVendidos). */
    private static FiltroVentas filtroPara(Solicitante solicitante, Long idAlmacen, Long idVendedor,
                                           LocalDate desde, LocalDate hasta) {
        // hasta incluye el día completo: se busca hasta el inicio del día siguiente
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();
        return switch (solicitante.rol()) {
            case ADMINISTRADOR -> new FiltroVentas(solicitante.idEmpresa(), idAlmacen, idVendedor, null, inicio, fin);
            // El vendedor solo ve sus ventas, aunque pida las de otro
            case VENDEDOR -> new FiltroVentas(solicitante.idEmpresa(), idAlmacen, solicitante.idUsuario(), null,
                    inicio, fin);
            // El cliente ve sus compras en todas las empresas
            case CLIENTE -> new FiltroVentas(null, idAlmacen, idVendedor, solicitante.idUsuario(), inicio, fin);
            case SUPERADMIN -> throw new ReglaNegocioException("El superadministrador no consulta ventas de las empresas");
        };
    }

    @Override
    public VentaConDetalle buscar(Solicitante solicitante, Long idVenta) {
        VentaResumen venta = consultaVentas.buscarResumen(idVenta)
                .filter(v -> PermisosVenta.puedeVer(solicitante, v.idEmpresa(), v.idCliente()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una venta con id " + idVenta));
        return new VentaConDetalle(venta, consultaVentas.lineasDe(idVenta));
    }
}
