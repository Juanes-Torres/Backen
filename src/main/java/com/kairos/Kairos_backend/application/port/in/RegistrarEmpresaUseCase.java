package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: una empresa se registra en la plataforma junto con su primer administrador.
 * La empresa queda PENDIENTE hasta que el superadministrador la apruebe.
 */
public interface RegistrarEmpresaUseCase {

    ResultadoRegistroEmpresa registrar(RegistrarEmpresaCommand command);

    /** Datos de la empresa y de su administrador (el password llega en texto plano y aquí se cifra). */
    record RegistrarEmpresaCommand(
            String nombre,
            String nit,
            String email,
            String telefono,
            String nombreAdmin,
            String emailAdmin,
            String passwordAdmin,
            String telefonoAdmin
    ) {
    }

    record ResultadoRegistroEmpresa(Empresa empresa, Usuario administrador) {
    }
}
