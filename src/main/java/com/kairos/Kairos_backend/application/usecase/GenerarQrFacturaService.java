package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GenerarQrFacturaUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.GeneradorQrPort;
import com.kairos.Kairos_backend.application.port.out.VentaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Venta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GenerarQrFacturaService implements GenerarQrFacturaUseCase {

    private final VentaRepositoryPort ventaRepository;
    private final GeneradorQrPort generadorQr;
    private final String urlFrontend;

    public GenerarQrFacturaService(VentaRepositoryPort ventaRepository,
                                   GeneradorQrPort generadorQr,
                                   @Value("${kairos.frontend.url}") String urlFrontend) {
        this.ventaRepository = ventaRepository;
        this.generadorQr = generadorQr;
        this.urlFrontend = urlFrontend.endsWith("/") ? urlFrontend.substring(0, urlFrontend.length() - 1) : urlFrontend;
    }

    @Override
    public byte[] generarQr(Solicitante solicitante, Long idVenta) {
        Venta venta = ventaRepository.buscarPorId(idVenta)
                .filter(v -> PermisosVenta.puedeVer(solicitante, v.getIdEmpresa(), v.getIdCliente()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una venta con id " + idVenta));
        return generadorQr.generarPng(enlaceGarantia(venta));
    }

    /** El enlace que abre el QR: la consulta de garantía de esa factura en la página web. */
    String enlaceGarantia(Venta venta) {
        return urlFrontend + "/garantias?empresa=" + venta.getIdEmpresa() + "&factura=" + venta.getNumeroFactura();
    }
}
