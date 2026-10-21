package com.kairos.Kairos_backend.infrastructure.security;

import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Lo que se sabe del usuario a partir de su token, en cada petición.
 * Los controllers lo reciben con @AuthenticationPrincipal.
 * idEmpresa es nulo para clientes y superadmin.
 */
public record UsuarioAutenticado(Long id, String email, Rol rol, Long idEmpresa) implements AuthenticatedPrincipal {

    /** authentication.getName() sigue devolviendo el email. */
    @Override
    public String getName() {
        return email;
    }

    public Solicitante comoSolicitante() {
        return new Solicitante(id, rol, idEmpresa);
    }
}
