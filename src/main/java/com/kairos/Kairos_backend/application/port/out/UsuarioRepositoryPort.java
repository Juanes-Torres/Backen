package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE SALIDA: qué necesita la aplicación para guardar y buscar usuarios.
 */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);

    List<Usuario> listarTodos();
}
