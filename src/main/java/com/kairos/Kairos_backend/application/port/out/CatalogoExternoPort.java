package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.CodigoBarras;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE SALIDA: buscar los datos de un producto por su código de barras en un catálogo externo.
 * El caso de uso no sabe qué API hay detrás (hoy UPCitemdb).
 */
public interface CatalogoExternoPort {

    /**
     * Vacío si el catálogo no conoce ese código.
     * Lanza ServicioExternoNoDisponibleException si el servicio está caído, lento o en su límite.
     */
    Optional<DatosProductoExterno> buscarPorCodigoBarras(CodigoBarras codigo);

    /**
     * Lo que el catálogo externo sabe del producto (cualquier campo puede venir vacío).
     * imagenes: direcciones de fotos del producto en internet (lista vacía si no hay).
     */
    record DatosProductoExterno(String nombre, String marca, String descripcion, String categoria,
                                List<String> imagenes) {

        public DatosProductoExterno {
            imagenes = imagenes == null ? List.of() : List.copyOf(imagenes);
        }

        public DatosProductoExterno(String nombre, String marca, String descripcion, String categoria) {
            this(nombre, marca, descripcion, categoria, List.of());
        }
    }
}
