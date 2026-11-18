package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarGarantiasUseCase.GarantiaDeProducto;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.CompraConGarantia;
import com.kairos.Kairos_backend.application.port.out.ConsultaGarantiasPort;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Consulta de garantías SIN base de datos y con un reloj fijo: "hoy" es el 8 de noviembre de 2026.
 */
class ConsultarGarantiasServiceTest {

    private static final Long EMPRESA = 1L;
    private final Clock hoy8Nov = Clock.fixed(LocalDate.of(2026, 11, 8).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
    private final ConsultaFalsa consulta = new ConsultaFalsa();
    private final ConsultarGarantiasService servicio = new ConsultarGarantiasService(consulta, hoy8Nov);
    private final Solicitante vendedor = new Solicitante(7L, Rol.VENDEDOR, EMPRESA);

    @Test
    void calculaElFinYSiSigueVigente_conLaFechaDeHoy() {
        List<GarantiaDeProducto> garantias = servicio.consultar(vendedor, "1020304050", null);

        GarantiaDeProducto audifonos = garantias.get(0);   // comprados el 1 de marzo de 2026, 12 meses
        assertEquals(LocalDate.of(2027, 3, 1), audifonos.fechaFin());
        assertTrue(audifonos.vigente());

        GarantiaDeProducto cable = garantias.get(1);       // comprado el 1 de marzo de 2026, 3 meses
        assertEquals(LocalDate.of(2026, 6, 1), cable.fechaFin());
        assertFalse(cable.vigente());
    }

    @Test
    void buscaSoloEnLaEmpresaDelEmpleado_yLimpiaLosDatos() {
        servicio.consultar(vendedor, null, " f-000001 ");

        assertEquals(EMPRESA, consulta.empresaConsultada);
        assertEquals("F-000001", consulta.facturaConsultada);
    }

    @Test
    void exigeCedulaOFactura_soloUnaDeLasDos() {
        assertThrows(ReglaNegocioException.class, () -> servicio.consultar(vendedor, null, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.consultar(vendedor, " ", ""));
        assertThrows(ReglaNegocioException.class, () -> servicio.consultar(vendedor, "1020304050", "F-000001"));
    }

    @Test
    void quienNoEsEmpleadoDeUnaEmpresaNoConsulta() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.consultar(new Solicitante(30L, Rol.CLIENTE, null), "1020304050", null));
    }

    /** Consulta falsa: dos productos de una misma compra y anota qué se le pidió. */
    private static class ConsultaFalsa implements ConsultaGarantiasPort {

        private Long empresaConsultada;
        private String facturaConsultada;
        private final LocalDateTime compra = LocalDateTime.of(2026, 3, 1, 10, 30);

        private List<CompraConGarantia> compras() {
            return new ArrayList<>(List.of(
                    new CompraConGarantia(5L, "F-000001", compra, 1L, "Sede Centro", 30L, "Laura", "1020304050",
                            100L, "Audífonos", 1, 12),
                    new CompraConGarantia(5L, "F-000001", compra, 1L, "Sede Centro", 30L, "Laura", "1020304050",
                            101L, "Cable USB", 2, 3)));
        }

        @Override
        public List<CompraConGarantia> comprasPorCedula(Long idEmpresa, String cedula) {
            empresaConsultada = idEmpresa;
            return compras();
        }

        @Override
        public List<CompraConGarantia> comprasPorFactura(Long idEmpresa, String numeroFactura) {
            empresaConsultada = idEmpresa;
            facturaConsultada = numeroFactura;
            return compras();
        }
    }
}
