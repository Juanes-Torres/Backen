package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Producto;

import java.util.List;

/**
 * CASO DE USO: el catálogo público con lo que necesita una tienda en línea (SCRUM-107):
 * qué empresa vende cada producto, si tiene logo y si está disponible.
 * Mismas reglas que la búsqueda de productos: solo empresas activas.
 */
public interface ConsultarCatalogoUseCase {

    List<ProductoEnCatalogo> buscar(Long idEmpresa, Long idCategoria, String nombre);

    ProductoEnCatalogo obtener(Long idProducto);

    /** disponible: hay unidades en alguna sede activa (no se muestra la cantidad exacta). */
    record ProductoEnCatalogo(Producto producto, String empresa, boolean empresaConLogo, boolean disponible) {
    }
}
