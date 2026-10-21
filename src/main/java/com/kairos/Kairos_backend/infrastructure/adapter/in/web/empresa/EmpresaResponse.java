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
        LocalDate fechaRegistro
) {
    public static EmpresaResponse desde(Empresa e) {
        return new EmpresaResponse(e.getId(), e.getNombre(), e.getNit(), e.getEmail(),
                e.getTelefono(), e.getEstado(), e.getFechaRegistro());
    }
}
