package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.BuscarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Rol;
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
    public Usuario buscarPorId(Solicitante solicitante, Long id) {
        return usuarioRepository.buscarPorId(id)
                .filter(usuario -> puedeGestionar(solicitante, usuario))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + id));
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con email " + email));
    }

    @Override
    public List<Usuario> listar(Solicitante solicitante) {
        return usuarioRepository.listarPorEmpresa(solicitante.idEmpresa());
    }

    /**
     * Regla: cada uno se gestiona a sí mismo; un administrador gestiona a los empleados de SU empresa,
     * y el superadmin a los clientes (su cuenta es de toda la plataforma, no de una empresa).
     * Un usuario que no le corresponde se trata igual que uno que no existe.
     */
    static boolean puedeGestionar(Solicitante solicitante, Usuario usuario) {
        return usuario.getId().equals(solicitante.idUsuario())
                || (solicitante.esAdministrador() && usuario.perteneceA(solicitante.idEmpresa()))
                || (solicitante.esSuperadmin() && usuario.getRol() == Rol.CLIENTE);
    }
}
