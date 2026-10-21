package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Rol;

/**
 * Quién hace la petición (sale del token). Con esto los casos de uso deciden
 * qué datos puede ver o cambiar: cada empresa solo ve lo suyo.
 * idEmpresa es nulo para clientes y superadmin.
 */
public record Solicitante(Long idUsuario, Rol rol, Long idEmpresa) {

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public boolean esSuperadmin() {
        return rol == Rol.SUPERADMIN;
    }
}
