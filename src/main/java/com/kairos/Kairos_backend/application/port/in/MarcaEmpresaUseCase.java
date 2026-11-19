package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Imagen;

/**
 * CASO DE USO: la marca de una empresa (SCRUM-105), para que sus empleados y clientes vean
 * su nombre y su logo en lugar de los de KAIRÓS.
 * El perfil lo cambia el administrador de la empresa; el logo de una empresa activa es público.
 */
public interface MarcaEmpresaUseCase {

    /** La empresa del empleado (administrador o vendedor). */
    Empresa miEmpresa(Long idEmpresa);

    /** El administrador cambia nombre, correo y teléfono. El NIT no cambia. */
    Empresa actualizarPerfil(Long idEmpresa, String nombre, String email, String telefono);

    /** Pone o reemplaza el logo (JPG, PNG o WEBP, máximo 2 MB). */
    void subirLogo(Long idEmpresa, byte[] contenido);

    void quitarLogo(Long idEmpresa);

    /** Logo de una empresa ACTIVA. 404 si no tiene o si la empresa no está activa. */
    Imagen logo(Long idEmpresa);

    boolean tieneLogo(Long idEmpresa);
}
