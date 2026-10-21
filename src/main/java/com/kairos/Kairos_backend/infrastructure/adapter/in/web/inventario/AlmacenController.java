package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import com.kairos.Kairos_backend.application.port.in.GestionarAlmacenesUseCase;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    private final GestionarAlmacenesUseCase almacenes;

    public AlmacenController(GestionarAlmacenesUseCase almacenes) {
        this.almacenes = almacenes;
    }

    /** Los empleados ven las sedes de SU empresa (la empresa sale del token). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<AlmacenResponse> listar(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return almacenes.listar(yo.idEmpresa()).stream().map(AlmacenResponse::desde).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public AlmacenResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return AlmacenResponse.desde(almacenes.obtener(yo.idEmpresa(), id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AlmacenResponse> crear(@Valid @RequestBody AlmacenRequest r,
                                                 @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AlmacenResponse.desde(
                almacenes.crear(yo.idEmpresa(), r.nombre(), r.ciudad(), r.direccion())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlmacenRequest r,
                                      @AuthenticationPrincipal UsuarioAutenticado yo) {
        return AlmacenResponse.desde(almacenes.actualizar(yo.idEmpresa(), id, r.nombre(), r.ciudad(), r.direccion()));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenResponse desactivar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return AlmacenResponse.desde(almacenes.cambiarEstado(yo.idEmpresa(), id, false));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenResponse activar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return AlmacenResponse.desde(almacenes.cambiarEstado(yo.idEmpresa(), id, true));
    }
}
