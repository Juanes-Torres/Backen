package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.BuscarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuscarUsuarioService implements BuscarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public BuscarUsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + id));
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con email " + email));
    }

    @Override
    public List<Usuario> listar() {
        return usuarioRepository.listarTodos();
    }
}
