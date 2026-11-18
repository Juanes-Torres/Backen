package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: crear el superadministrador de la plataforma. Solo lo usa el arranque
 * (SuperadminInicial); va aparte de RegistrarUsuarioUseCase para que los controllers
 * ni siquiera vean este método (SOLID: segregación de interfaces).
 */
public interface CrearSuperadminUseCase {

    Usuario registrarSuperadmin(String nombre, String email, String password);
}
