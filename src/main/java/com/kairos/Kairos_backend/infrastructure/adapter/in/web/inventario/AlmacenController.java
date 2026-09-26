package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import com.kairos.Kairos_backend.application.port.in.GestionarAlmacenesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /** Cualquier usuario con sesión puede ver las sedes. */
    @GetMapping
    public List<AlmacenResponse> listar() {
        return almacenes.listar().stream().map(AlmacenResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public AlmacenResponse obtener(@PathVariable Long id) {
        return AlmacenResponse.desde(almacenes.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AlmacenResponse> crear(@Valid @RequestBody AlmacenRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AlmacenResponse.desde(almacenes.crear(r.nombre(), r.ciudad(), r.direccion())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlmacenRequest r) {
        return AlmacenResponse.desde(almacenes.actualizar(id, r.nombre(), r.ciudad(), r.direccion()));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenResponse desactivar(@PathVariable Long id) {
        return AlmacenResponse.desde(almacenes.cambiarEstado(id, false));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenResponse activar(@PathVariable Long id) {
        return AlmacenResponse.desde(almacenes.cambiarEstado(id, true));
    }
}
