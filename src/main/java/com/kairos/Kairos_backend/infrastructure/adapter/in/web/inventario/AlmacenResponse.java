package com.kairos.Kairos_backend.infrastructure.adapter.in.web.inventario;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.EstadoAlmacen;

public record AlmacenResponse(Long id, String nombre, String ciudad, String direccion, EstadoAlmacen estado) {

    public static AlmacenResponse desde(Almacen a) {
        return new AlmacenResponse(a.getId(), a.getNombre(), a.getCiudad(), a.getDireccion(), a.getEstado());
    }
}
