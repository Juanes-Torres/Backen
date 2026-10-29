package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import com.kairos.Kairos_backend.application.port.in.GenerarQrFacturaUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase.Item;
import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase.RegistrarVentaCommand;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * ADAPTADOR DE ENTRADA (REST) para ventas (RF04).
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVenta;
    private final GenerarQrFacturaUseCase qrFactura;

    public VentaController(RegistrarVentaUseCase registrarVenta, GenerarQrFacturaUseCase qrFactura) {
        this.registrarVenta = registrarVenta;
        this.qrFactura = qrFactura;
    }

    /** Registrar una venta: descuenta el stock y genera el número de factura. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody RegistrarVentaRequest request,
                                                   @AuthenticationPrincipal UsuarioAutenticado yo) {
        var items = request.items().stream().map(i -> new Item(i.idProducto(), i.cantidad())).toList();
        var venta = registrarVenta.registrar(yo.comoSolicitante(),
                new RegistrarVentaCommand(request.idAlmacen(), request.emailCliente(), items));
        return ResponseEntity.status(HttpStatus.CREATED).body(VentaResponse.desde(venta));
    }

    /**
     * Imagen PNG con el código QR de la factura (API externa).
     * La ven los empleados de la empresa de la venta o el cliente que compró.
     */
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> qr(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        byte[] imagen = qrFactura.generarQr(yo.comoSolicitante(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                // El QR de una factura no cambia: el navegador lo guarda un día y no vuelve a pedirlo
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .body(imagen);
    }
}
