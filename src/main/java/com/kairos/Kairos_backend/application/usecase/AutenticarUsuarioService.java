package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.TokenPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.CredencialesInvalidasException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenPort tokenPort;

    public AutenticarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                    EmpresaRepositoryPort empresaRepository,
                                    PasswordEncoderPort passwordEncoder,
                                    TokenPort tokenPort) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenPort = tokenPort;
    }

    @Override
    public ResultadoLogin autenticar(String email, String password) {
        if (email == null || password == null) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }

        // Mismo mensaje si no existe el email o si la contraseña falla:
        // así nadie puede averiguar qué emails están registrados.
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos"));

        if (!passwordEncoder.coincide(password, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");
        }

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("El usuario está desactivado");
        }

        // Los empleados solo entran si su empresa está ACTIVA (clientes y superadmin no tienen empresa)
        Empresa empresa = null;
        if (usuario.getIdEmpresa() != null) {
            empresa = empresaRepository.buscarPorId(usuario.getIdEmpresa())
                    .orElseThrow(() -> new CredencialesInvalidasException("La empresa del usuario no existe"));
            exigirEmpresaActiva(empresa);
        }

        return new ResultadoLogin(tokenPort.generarToken(usuario), usuario, empresa);
    }

    private void exigirEmpresaActiva(Empresa empresa) {
        if (empresa.estaActiva()) {
            return;
        }
        String motivo = switch (empresa.getEstado()) {
            case PENDIENTE -> "La empresa aún no ha sido aprobada por el superadministrador";
            case RECHAZADA -> "La empresa fue rechazada por el superadministrador";
            case SUSPENDIDA -> "La empresa está suspendida";
            case ACTIVA -> throw new IllegalStateException("No debería llegar aquí");
        };
        throw new CredencialesInvalidasException(motivo);
    }
}
