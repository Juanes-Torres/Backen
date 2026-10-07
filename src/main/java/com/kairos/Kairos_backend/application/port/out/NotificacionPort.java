package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * PUERTO DE SALIDA: avisarle algo al usuario (hoy por correo).
 * El caso de uso no sabe si por detrás hay Gmail, otro proveedor o nada.
 */
public interface NotificacionPort {

    void enviarBienvenida(Usuario usuario);
}
