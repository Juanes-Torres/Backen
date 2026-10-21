package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase.ResultadoRegistroEmpresa;
import com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario.UsuarioResponse;

/** Respuesta del registro: la empresa (PENDIENTE) y su administrador, sin passwordHash. */
public record RegistroEmpresaResponse(
        EmpresaResponse empresa,
        UsuarioResponse administrador,
        String mensaje
) {
    public static RegistroEmpresaResponse desde(ResultadoRegistroEmpresa r) {
        return new RegistroEmpresaResponse(
                EmpresaResponse.desde(r.empresa()),
                UsuarioResponse.desde(r.administrador()),
                "Empresa registrada. Queda pendiente de aprobación por el superadministrador");
    }
}
