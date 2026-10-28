package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * Regla ÚNICA de quién puede ver o modificar a quién. La usan los casos de uso de
 * consultar y de actualizar usuarios, así la regla no se repite (SOLID: responsabilidad única).
 */
final class PermisosUsuario {

    private PermisosUsuario() {
    }

    /**
     * Cada uno se gestiona a sí mismo; un administrador gestiona a los empleados de SU empresa,
     * y el superadmin a los clientes (su cuenta es de toda la plataforma, no de una empresa).
     * Un usuario que no le corresponde se trata igual que uno que no existe.
     */
    static boolean puedeGestionar(Solicitante solicitante, Usuario usuario) {
        return usuario.getId().equals(solicitante.idUsuario())
                || (solicitante.esAdministrador() && usuario.perteneceA(solicitante.idEmpresa()))
                || (solicitante.esSuperadmin() && usuario.getRol() == Rol.CLIENTE);
    }
}
