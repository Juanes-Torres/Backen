package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase.ProductoCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final GestionarProductosUseCase productos;

    public ProductoController(GestionarProductosUseCase productos) {
        this.productos = productos;
    }

    /** RF03 - Consulta pública del catálogo con filtros opcionales. */
    @GetMapping
    public List<ProductoResponse> buscar(@RequestParam(required = false) Long idCategoria,
                                         @RequestParam(required = false) String nombre) {
        return productos.buscar(idCategoria, nombre).stream().map(ProductoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.desde(productos.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoResponse.desde(productos.crear(aCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return ProductoResponse.desde(productos.actualizar(id, aCommand(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productos.eliminar(id);
        return ResponseEntity.noContent().build();   // 204: se borró y no hay nada que devolver
    }

    private ProductoCommand aCommand(ProductoRequest r) {
        return new ProductoCommand(r.nombre(), r.descripcion(), r.precio(), r.marca(), r.idCategoria());
    }
}
