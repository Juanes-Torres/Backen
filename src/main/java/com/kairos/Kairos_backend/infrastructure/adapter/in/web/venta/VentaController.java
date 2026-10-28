package com.kairos.Kairos_backend.infrastructure.adapter.in.web.venta;

import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase.Item;
import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase.RegistrarVentaCommand;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADAPTADOR DE ENTRADA (REST) para ventas (RF04).
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVenta;

    public VentaController(RegistrarVentaUseCase registrarVenta) {
        this.registrarVenta = registrarVenta;
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
}
