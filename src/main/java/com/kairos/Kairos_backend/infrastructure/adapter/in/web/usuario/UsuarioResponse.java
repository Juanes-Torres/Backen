package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;

import java.time.LocalDate;

/**
 * Lo que la API devuelve de un usuario. NUNCA incluye el passwordHash.
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        String telefono,
        Long idAlmacen,
        LocalDate fechaRegistro,
        boolean activo
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol(),
                u.getTelefono(), u.getIdAlmacen(), u.getFechaRegistro(), u.isActivo());
    }
}
