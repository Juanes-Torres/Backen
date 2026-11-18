package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

/**
 * Foto de un producto ya validada: es JPG, PNG o WEBP y pesa máximo 2 MB (SCRUM-103).
 * Solo se crea con {@link #desde(byte[])}, así que toda ImagenProducto que existe es válida.
 */
public record ImagenProducto(byte[] contenido, TipoImagen tipo) {

    public static final int TAMANO_MAXIMO_BYTES = 2 * 1024 * 1024;

    public ImagenProducto {
        if (contenido == null || contenido.length == 0) {
            throw new ReglaNegocioException("La imagen está vacía");
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new ReglaNegocioException("La imagen no puede pesar más de 2 MB");
        }
        if (tipo == null || TipoImagen.detectar(contenido).orElse(null) != tipo) {
            throw new ReglaNegocioException("La imagen debe ser JPG, PNG o WEBP");
        }
    }

    /** Revisa el contenido real del archivo (no su nombre ni lo que diga el navegador). */
    public static ImagenProducto desde(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new ReglaNegocioException("La imagen está vacía");
        }
        TipoImagen tipo = TipoImagen.detectar(contenido)
                .orElseThrow(() -> new ReglaNegocioException("La imagen debe ser JPG, PNG o WEBP"));
        return new ImagenProducto(contenido, tipo);
    }

    // equals/hashCode/toString de un record con arreglo comparan la referencia y no el contenido:
    // se ajustan para comparar los bytes y no imprimir 2 MB en un log.

    @Override
    public boolean equals(Object otro) {
        return otro instanceof ImagenProducto(byte[] c, TipoImagen t)
                && t == tipo && java.util.Arrays.equals(c, contenido);
    }

    @Override
    public int hashCode() {
        return 31 * java.util.Arrays.hashCode(contenido) + tipo.hashCode();
    }

    @Override
    public String toString() {
        return "ImagenProducto[" + tipo + ", " + contenido.length + " bytes]";
    }
}
