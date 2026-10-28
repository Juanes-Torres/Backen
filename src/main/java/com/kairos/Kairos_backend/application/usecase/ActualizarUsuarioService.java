package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ActualizarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class ActualizarUsuarioService implements ActualizarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public ActualizarUsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario actualizarDatos(Solicitante solicitante, Long id, String nombre, String telefono) {
        Usuario usuario = obtener(solicitante, id);
        usuario.actualizarDatos(nombre, telefono);   // el dominio valida
        return usuarioRepository.guardar(usuario);
    }

    @Override
    public Usuario cambiarEstado(Solicitante solicitante, Long id, boolean activo) {
        Usuario usuario = obtener(solicitante, id);
        if (activo) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }
        return usuarioRepository.guardar(usuario);
    }

    private Usuario obtener(Solicitante solicitante, Long id) {
        return usuarioRepository.buscarPorId(id)
                .filter(usuario -> PermisosUsuario.puedeGestionar(solicitante, usuario))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + id));
    }
}
