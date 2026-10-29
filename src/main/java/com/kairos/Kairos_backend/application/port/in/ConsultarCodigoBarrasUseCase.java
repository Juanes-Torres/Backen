package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort.DatosProductoExterno;

/**
 * CASO DE USO: al crear un producto, traer nombre, marca y descripción a partir del código de barras.
 * Solo sugiere datos: el administrador los revisa y guarda el producto con POST /api/productos.
 */
public interface ConsultarCodigoBarrasUseCase {

    DatosProductoExterno consultar(String codigoBarras);
}
