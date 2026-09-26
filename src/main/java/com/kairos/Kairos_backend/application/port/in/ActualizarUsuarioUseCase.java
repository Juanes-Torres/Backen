package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: modificar un usuario existente.
 */
public interface ActualizarUsuarioUseCase {

    Usuario actualizarDatos(Long id, String nombre, String telefono);

    Usuario cambiarEstado(Long id, boolean activo);
}
