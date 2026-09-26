package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.TokenPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.CredencialesInvalidasException;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenPort tokenPort;

    public AutenticarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                    PasswordEncoderPort passwordEncoder,
                                    TokenPort tokenPort) {
        this.usuarioRepository = usuarioRepository;
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

        return new ResultadoLogin(tokenPort.generarToken(usuario), usuario);
    }
}
