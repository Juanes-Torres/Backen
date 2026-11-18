package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import com.kairos.Kairos_backend.domain.model.Empresa;

/** Lo que cualquiera puede ver de una empresa activa (sin NIT ni estado). */
public record EmpresaPublicaResponse(Long id, String nombre, String telefono) {

    public static EmpresaPublicaResponse desde(Empresa e) {
        return new EmpresaPublicaResponse(e.getId(), e.getNombre(), e.getTelefono());
    }
}
