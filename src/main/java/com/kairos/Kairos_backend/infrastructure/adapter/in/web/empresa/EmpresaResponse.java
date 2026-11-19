package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;

import java.time.LocalDate;

public record EmpresaResponse(
        Long id,
        String nombre,
        String nit,
        String email,
        String telefono,
        EstadoEmpresa estado,
        LocalDate fechaRegistro,
        String logoUrl            // nulo si la empresa no tiene logo
) {
    public static EmpresaResponse desde(Empresa e, boolean tieneLogo) {
        return new EmpresaResponse(e.getId(), e.getNombre(), e.getNit(), e.getEmail(),
                e.getTelefono(), e.getEstado(), e.getFechaRegistro(), tieneLogo ? logoUrl(e.getId()) : null);
    }

    /** Dirección pública del logo (relativa al backend). Se puede poner directo en un <img src="...">. */
    public static String logoUrl(Long idEmpresa) {
        return "/api/empresas/" + idEmpresa + "/logo";
    }
}
