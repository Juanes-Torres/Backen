package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.ImagenProducto;
import com.kairos.Kairos_backend.domain.model.Producto;

/**
 * CASO DE USO: la foto de un producto (SCRUM-103).
 * Ver la foto es público, como el catálogo; ponerla o quitarla es solo dentro de la empresa del token.
 */
public interface GestionarImagenProductoUseCase {

    /** Foto de un producto visible en el catálogo. 404 si el producto no se ve o no tiene foto. */
    ImagenProducto ver(Long idProducto);

    /** Pone o reemplaza la foto con un archivo subido (JPG, PNG o WEBP, máximo 2 MB). */
    Producto subir(Long idEmpresa, Long idProducto, byte[] contenido);

    /** Toma la primera foto que sirva de las que ofrece el catálogo de códigos de barras y la guarda en KAIRÓS. */
    Producto usarImagenDelCodigoBarras(Long idEmpresa, Long idProducto, String codigoBarras);

    void quitar(Long idEmpresa, Long idProducto);
}
