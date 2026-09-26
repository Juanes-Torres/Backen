package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: iniciar sesión.
 */
public interface AutenticarUsuarioUseCase {

    ResultadoLogin autenticar(String email, String password);

    record ResultadoLogin(String token, Usuario usuario) {
    }
}
