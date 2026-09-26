package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.UsuarioEntity;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioEntity toEntity(Usuario u) {
        return new UsuarioEntity(u.getId(), u.getNombre(), u.getEmail(), u.getPasswordHash(),
                u.getRol().name(), u.getTelefono(), u.getIdAlmacen(), u.getFechaRegistro(), u.isActivo());
    }

    public static Usuario toDomain(UsuarioEntity e) {
        return new Usuario(e.getId(), e.getNombre(), e.getEmail(), e.getPasswordHash(),
                Rol.valueOf(e.getRol()), e.getTelefono(), e.getIdAlmacen(), e.getFechaRegistro(), e.isActivo());
    }
}
