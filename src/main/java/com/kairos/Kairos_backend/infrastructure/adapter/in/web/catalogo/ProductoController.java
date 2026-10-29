package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.application.port.in.ConsultarCodigoBarrasUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase.ProductoCommand;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final ConsultarCodigoBarrasUseCase codigoBarras;

    public ProductoController(GestionarProductosUseCase productos, ConsultarCodigoBarrasUseCase codigoBarras) {
        this.productos = productos;
        this.codigoBarras = codigoBarras;
    }

    /** Solo ADMINISTRADOR: sugiere nombre, marca y descripción a partir del código de barras (API externa). */
    @GetMapping("/codigo-barras/{codigo}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DatosCodigoBarrasResponse buscarPorCodigoBarras(@PathVariable String codigo) {
        return DatosCodigoBarrasResponse.desde(codigo, codigoBarras.consultar(codigo));
    }

    /** RF03 - Catálogo público (solo empresas activas) con filtros opcionales. */
    @GetMapping
    public List<ProductoResponse> buscar(@RequestParam(required = false) Long idEmpresa,
                                         @RequestParam(required = false) Long idCategoria,
                                         @RequestParam(required = false) String nombre) {
        return productos.buscar(idEmpresa, idCategoria, nombre).stream().map(ProductoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.desde(productos.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request,
                                                  @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoResponse.desde(productos.crear(yo.idEmpresa(), aCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request,
                                       @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ProductoResponse.desde(productos.actualizar(yo.idEmpresa(), id, aCommand(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        productos.eliminar(yo.idEmpresa(), id);
        return ResponseEntity.noContent().build();   // 204: se borró y no hay nada que devolver
    }

    private ProductoCommand aCommand(ProductoRequest r) {
        return new ProductoCommand(r.nombre(), r.descripcion(), r.precio(), r.marca(), r.idCategoria());
    }
}
