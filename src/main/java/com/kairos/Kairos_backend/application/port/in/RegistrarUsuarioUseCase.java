package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: registrar un usuario.
 */
public interface RegistrarUsuarioUseCase {

    Usuario registrar(RegistrarUsuarioCommand command);

    /** Datos necesarios para registrar (el password llega en texto plano y aquí se cifra). */
    record RegistrarUsuarioCommand(
            String nombre,
            String email,
            String password,
            Rol rol,
            String telefono,
            Long idAlmacen
    ) {
    }
}
