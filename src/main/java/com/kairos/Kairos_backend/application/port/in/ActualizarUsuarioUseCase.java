package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: modificar un usuario existente.
 * Cada uno se modifica a sí mismo; un administrador, además, a los usuarios de SU empresa.
 */
public interface ActualizarUsuarioUseCase {

    Usuario actualizarDatos(Solicitante solicitante, Long id, String nombre, String telefono);

    Usuario cambiarEstado(Solicitante solicitante, Long id, boolean activo);
}
