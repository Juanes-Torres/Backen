package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.application.port.in.GestionarCategoriasUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final GestionarCategoriasUseCase categorias;

    public CategoriaController(GestionarCategoriasUseCase categorias) {
        this.categorias = categorias;
    }

    /** Público: el catálogo web necesita las categorías para filtrar. */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categorias.listar().stream().map(CategoriaResponse::desde).toList();
    }

    /**
     * Las categorías son COMPARTIDAS por todas las empresas (SCRUM-57): solo el superadmin
     * las crea y renombra. Cada empresa las usa para clasificar sus productos.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoriaResponse.desde(categorias.crear(request.nombre())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public CategoriaResponse renombrar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.desde(categorias.renombrar(id, request.nombre()));
    }
}
