package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AutenticarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String autenticar(String email, String password) {

        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() ->
                        new ReglaNegocioException("Email o contraseña incorrectos"));

        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario está inactivo");
        }

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new ReglaNegocioException("Email o contraseña incorrectos");
        }

        return jwtService.generarToken(usuario);
    }
}