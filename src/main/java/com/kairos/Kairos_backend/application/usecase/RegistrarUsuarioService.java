package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrar(String nombre,
                             String email,
                             String password,
                             Rol rol,
                             String telefono,
                             Long idAlmacen) {

        if (usuarioRepository.existePorEmail(email)) {
            throw new ReglaNegocioException("El email ya está registrado");
        }

        String passwordHash = passwordEncoder.encode(password);

        Usuario usuario = Usuario.nuevo(
                nombre,
                email,
                passwordHash,
                rol,
                telefono,
                idAlmacen
        );

        return usuarioRepository.guardar(usuario);
    }
}