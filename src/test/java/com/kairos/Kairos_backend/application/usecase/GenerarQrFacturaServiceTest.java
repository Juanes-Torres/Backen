package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.VentaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.DetalleVenta;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Venta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * QR de la factura SIN internet: el generador de QR es una clase falsa que anota qué se le pidió.
 */
class GenerarQrFacturaServiceTest {

    private static final Long EMPRESA = 1L;
    private static final Long CLIENTE = 30L;
    private static final byte[] PNG_FALSO = {1, 2, 3};

    private final Venta venta = new Venta(5L, "F-000007", EMPRESA, 10L, 7L, CLIENTE, null, null,
            List.of(new DetalleVenta(100L, 1, BigDecimal.TEN)));
    private final List<String> contenidos = new ArrayList<>();

    private final GenerarQrFacturaService servicio = new GenerarQrFacturaService(
            new VentaRepositorioDeUnaVenta(venta),
            contenido -> {
                contenidos.add(contenido);
                return PNG_FALSO;
            },
            "https://kairos.app/");

    @Test
    void elQrLlevaElEnlaceDeLaGarantiaDeEsaFactura() {
        byte[] imagen = servicio.generarQr(new Solicitante(7L, Rol.VENDEDOR, EMPRESA), 5L);

        assertArrayEquals(PNG_FALSO, imagen);
        assertEquals("https://kairos.app/garantias?empresa=1&factura=F-000007", contenidos.get(0));
    }

    @Test
    void elClienteQueComproPuedeVerSuQr() {
        servicio.generarQr(new Solicitante(CLIENTE, Rol.CLIENTE, null), 5L);

        assertEquals(1, contenidos.size());
    }

    @Test
    void nadieMasPuedeVerElQr_niSeLlamaALaApi() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.generarQr(new Solicitante(8L, Rol.ADMINISTRADOR, 2L), 5L));   // otra empresa
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.generarQr(new Solicitante(99L, Rol.CLIENTE, null), 5L));     // otro cliente
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.generarQr(new Solicitante(7L, Rol.VENDEDOR, EMPRESA), 404L)); // no existe

        assertTrue(contenidos.isEmpty());
    }

    /** Repositorio falso con una sola venta guardada. */
    private record VentaRepositorioDeUnaVenta(Venta venta) implements VentaRepositoryPort {

        @Override
        public Venta guardar(Venta v) {
            return v;
        }

        @Override
        public Optional<Venta> buscarPorId(Long id) {
            return venta.getId().equals(id) ? Optional.of(venta) : Optional.empty();
        }

        @Override
        public long contarPorEmpresa(Long idEmpresa) {
            return 1;
        }
    }
}
