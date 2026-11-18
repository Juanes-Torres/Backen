package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Usuario;

import java.util.List;

/**
 * CASO DE USO: consultar usuarios.
 * Cada uno se ve a sí mismo; un administrador ve además a los usuarios de SU empresa.
 */
public interface BuscarUsuarioUseCase {

    Usuario buscarPorId(Solicitante solicitante, Long id);

    Usuario buscarPorEmail(String email);

    /** Los usuarios de la empresa del administrador. */
    List<Usuario> listar(Solicitante solicitante);
}
