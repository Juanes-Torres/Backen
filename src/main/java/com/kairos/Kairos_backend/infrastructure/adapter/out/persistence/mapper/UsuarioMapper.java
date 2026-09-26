package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.UsuarioEntity;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioEntity toEntity(Usuario usuario) {

        return new UsuarioEntity(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getRol().name(),
                usuario.getTelefono(),
                usuario.getIdAlmacen(),
                usuario.getFechaRegistro(),
                usuario.isActivo()
        );
    }

    public static Usuario toDomain(UsuarioEntity entity) {

        return new Usuario(
                entity.getId(),
                entity.getNombre(),
                entity.getEmail(),
                entity.getPasswordHash(),
                Rol.valueOf(entity.getRol()),
                entity.getTelefono(),
                entity.getIdAlmacen(),
                entity.getFechaRegistro(),
                entity.isActivo()
        );
    }
}