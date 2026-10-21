package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa.EmpresaResponse;

/** empresa es nula para clientes y superadmin. */
public record LoginResponse(
        String token,
        String tipo,
        UsuarioResponse usuario,
        EmpresaResponse empresa
) {
}
