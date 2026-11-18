package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase.Filtro;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.ConsultaVentasPort;
import com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quién ve qué ventas, SIN base de datos: la consulta es una clase falsa que anota el filtro que recibe.
 */
class ConsultarVentasServiceTest {

    private static final Long EMPRESA = 1L;
    private static final Long CLIENTE = 30L;

    private final ConsultaFalsa consulta = new ConsultaFalsa();
    private final ConsultarVentasService servicio = new ConsultarVentasService(consulta);
    private final Filtro sinFiltros = new Filtro(null, null, null, null);

    @Test
    void elAdministradorVeLasVentasDeSuEmpresa_yPuedeFiltrarPorSedeYVendedor() {
        servicio.listar(new Solicitante(5L, Rol.ADMINISTRADOR, EMPRESA), new Filtro(2L, 7L, null, null));

        ConsultaVentasPort.FiltroVentas filtro = consulta.ultimoFiltro;
        assertEquals(EMPRESA, filtro.idEmpresa());
        assertEquals(2L, filtro.idAlmacen());
        assertEquals(7L, filtro.idVendedor());
        assertNull(filtro.idCliente());
    }

    @Test
    void elVendedorSoloVeSusVentas_aunquePidaLasDeOtro() {
        servicio.listar(new Solicitante(7L, Rol.VENDEDOR, EMPRESA), new Filtro(null, 99L, null, null));

        assertEquals(7L, consulta.ultimoFiltro.idVendedor());
        assertEquals(EMPRESA, consulta.ultimoFiltro.idEmpresa());
    }

    @Test
    void elClienteVeSusComprasEnTodasLasEmpresas() {
        servicio.listar(new Solicitante(CLIENTE, Rol.CLIENTE, null), sinFiltros);

        assertEquals(CLIENTE, consulta.ultimoFiltro.idCliente());
        assertNull(consulta.ultimoFiltro.idEmpresa());
    }

    @Test
    void elSuperadminNoConsultaVentas() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.listar(new Solicitante(1L, Rol.SUPERADMIN, null), sinFiltros));
    }

    @Test
    void lasFechasIncluyenElDiaCompleto() {
        servicio.listar(new Solicitante(5L, Rol.ADMINISTRADOR, EMPRESA),
                new Filtro(null, null, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)));

        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0), consulta.ultimoFiltro.desde());
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), consulta.ultimoFiltro.hasta());
    }

    @Test
    void sinFechas_seBuscaEnTodoElHistorialHastaHoy() {
        servicio.listar(new Solicitante(5L, Rol.ADMINISTRADOR, EMPRESA), sinFiltros);

        assertTrue(consulta.ultimoFiltro.desde().getYear() <= 2000);
        assertEquals(LocalDate.now().plusDays(1).atStartOfDay(), consulta.ultimoFiltro.hasta());
    }

    @Test
    void desdePosteriorAHasta_es400() {
        assertThrows(ReglaNegocioException.class, () -> servicio.listar(new Solicitante(5L, Rol.ADMINISTRADOR, EMPRESA),
                new Filtro(null, null, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1))));
    }

    @Test
    void elDetalleLoVenLosEmpleadosDeLaEmpresaYElClienteQueCompro() {
        assertEquals(1, servicio.buscar(new Solicitante(7L, Rol.VENDEDOR, EMPRESA), 10L).lineas().size());
        assertEquals("F-000001", servicio.buscar(new Solicitante(CLIENTE, Rol.CLIENTE, null), 10L).venta().numeroFactura());
    }

    @Test
    void elDetalleDeUnaVentaAjenaOInexistente_es404() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.buscar(new Solicitante(8L, Rol.ADMINISTRADOR, 2L), 10L));   // otra empresa
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.buscar(new Solicitante(99L, Rol.CLIENTE, null), 10L));      // otro cliente
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.buscar(new Solicitante(7L, Rol.VENDEDOR, EMPRESA), 404L));  // no existe
    }

    /** Consulta falsa con una sola venta (id 10, empresa 1, cliente 30). */
    private static class ConsultaFalsa implements ConsultaVentasPort {

        private final VentaResumen venta = new VentaResumen(10L, "F-000001", LocalDateTime.now(), EMPRESA,
                2L, "Sede Centro", 7L, "Luis Vendedor", CLIENTE, "Laura Gómez", "COMPLETADA", BigDecimal.TEN);
        private FiltroVentas ultimoFiltro;

        @Override
        public List<VentaResumen> listar(FiltroVentas filtro) {
            ultimoFiltro = filtro;
            return new ArrayList<>(List.of(venta));
        }

        @Override
        public Optional<VentaResumen> buscarResumen(Long idVenta) {
            return venta.id().equals(idVenta) ? Optional.of(venta) : Optional.empty();
        }

        @Override
        public List<LineaVentaDetalle> lineasDe(Long idVenta) {
            return List.of(new LineaVentaDetalle(100L, "Audífonos", 1, BigDecimal.TEN, BigDecimal.TEN));
        }
    }
}
