package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

public record LoginResponse(
        String token,
        String tipo,
        UsuarioResponse usuario
) {
}
