package com.kairos.Kairos_backend.infrastructure.adapter.in.web.garantia;

import com.kairos.Kairos_backend.application.port.in.GestionarReclamosUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarReclamosUseCase.RegistrarReclamoCommand;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADAPTADOR DE ENTRADA (REST) para los reclamos de garantía (SCRUM-75).
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/garantias/reclamos")
public class ReclamoController {

    private final GestionarReclamosUseCase reclamos;

    public ReclamoController(GestionarReclamosUseCase reclamos) {
        this.reclamos = reclamos;
    }

    /** Registrar un reclamo: solo si el producto está en la factura y su garantía sigue vigente. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ResponseEntity<ReclamoResponse> registrar(@Valid @RequestBody ReclamoRequest request,
                                                     @AuthenticationPrincipal UsuarioAutenticado yo) {
        var reclamo = reclamos.registrar(yo.comoSolicitante(),
                new RegistrarReclamoCommand(request.idVenta(), request.idProducto(), request.motivo()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReclamoResponse.desde(reclamo));
    }

    /** Empleados: los reclamos de su empresa. Cliente: los suyos. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR','CLIENTE')")
    public List<ReclamoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return reclamos.listar(yo.comoSolicitante()).stream().map(ReclamoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ReclamoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ReclamoResponse.desde(reclamos.buscar(yo.comoSolicitante(), id));
    }

    /** Cambiar el estado: queda en el historial con la fecha y quién lo hizo. */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ReclamoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoReclamoRequest request,
                                         @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ReclamoResponse.desde(reclamos.cambiarEstado(yo.comoSolicitante(), id, request.estado(), request.nota()));
    }
}
