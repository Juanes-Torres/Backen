package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Usuario;

import java.util.List;

/**
 * CASO DE USO: consultar usuarios.
 */
public interface BuscarUsuarioUseCase {

    Usuario buscarPorId(Long id);

    Usuario buscarPorEmail(String email);

    List<Usuario> listar();
}
