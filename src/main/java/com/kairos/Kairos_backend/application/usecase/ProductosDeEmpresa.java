package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Producto;

/**
 * Regla compartida por ProductoService e ImagenProductoService: un administrador solo toca los
 * productos de su empresa, y uno de otra empresa se trata igual que uno que no existe (404),
 * para no revelar qué productos tienen las demás.
 */
final class ProductosDeEmpresa {

    private ProductosDeEmpresa() {
    }

    static Producto obtener(ProductoRepositoryPort productos, Long idEmpresa, Long idProducto) {
        return productos.buscarPorId(idProducto)
                .filter(producto -> producto.perteneceA(idEmpresa))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + idProducto));
    }
}
