package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * PUERTO DE SALIDA: generar el token de sesión de un usuario.
 * La aplicación no sabe que por detrás se usa JWT.
 */
public interface TokenPort {

    String generarToken(Usuario usuario);
}
