package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import com.kairos.Kairos_backend.application.port.in.GestionarInventarioUseCase;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final GestionarInventarioUseCase inventario;

    public InventarioController(GestionarInventarioUseCase inventario) {
        this.inventario = inventario;
    }

    /** Stock por almacén de la empresa del token (sin idAlmacen = todos). ADMIN y VENDEDOR. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<InventarioResponse> listar(@RequestParam(required = false) Long idAlmacen,
                                           @AuthenticationPrincipal UsuarioAutenticado yo) {
        return inventario.listar(yo.idEmpresa(), idAlmacen).stream().map(InventarioResponse::desde).toList();
    }

    /** Productos por debajo del stock mínimo (base de las alertas, RF12). */
    @GetMapping("/bajo-stock")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<InventarioResponse> bajoStock(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return inventario.listarBajoStock(yo.idEmpresa()).stream().map(InventarioResponse::desde).toList();
    }

    /** Registrar por primera vez un producto en un almacén. */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<InventarioResponse> registrar(@Valid @RequestBody InventarioRequest r,
                                                        @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(InventarioResponse.desde(
                inventario.registrar(yo.idEmpresa(), r.idProducto(), r.idAlmacen(), r.cantidadInicial(), r.stockMinimo())));
    }

    /** ENTRADA o SALIDA de unidades. El stock nunca queda negativo. */
    @PatchMapping("/{id}/ajuste")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public InventarioResponse ajustar(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest r,
                                      @AuthenticationPrincipal UsuarioAutenticado yo) {
        return InventarioResponse.desde(inventario.ajustar(yo.idEmpresa(), id, r.tipo(), r.cantidad()));
    }

    @PatchMapping("/{id}/stock-minimo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public InventarioResponse cambiarStockMinimo(@PathVariable Long id, @Valid @RequestBody StockMinimoRequest r,
                                                 @AuthenticationPrincipal UsuarioAutenticado yo) {
        return InventarioResponse.desde(inventario.cambiarStockMinimo(yo.idEmpresa(), id, r.stockMinimo()));
    }
}
