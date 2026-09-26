package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private static final int LONGITUD_MINIMA_PASSWORD = 6;

    private final UsuarioRepositoryPort usuarioRepository;
    private final AlmacenRepositoryPort almacenRepository;
    private final PasswordEncoderPort passwordEncoder;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                   AlmacenRepositoryPort almacenRepository,
                                   PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.almacenRepository = almacenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrar(RegistrarUsuarioCommand command) {
        // 1. La contraseña en texto plano solo existe aquí: validarla antes de cifrarla
        if (command.password() == null || command.password().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
        }

        // 2. El email no puede estar repetido
        if (command.email() != null && usuarioRepository.existePorEmail(command.email())) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el email " + command.email());
        }

        // 3. Si trae almacén, el almacén debe existir
        if (command.idAlmacen() != null && !almacenRepository.existePorId(command.idAlmacen())) {
            throw new RecursoNoEncontradoException("No existe un almacén con id " + command.idAlmacen());
        }

        // 4. El dominio valida sus reglas (nombre, email, rol, vendedor con almacén...)
        Usuario nuevo = Usuario.nuevo(
                command.nombre(),
                command.email(),
                passwordEncoder.cifrar(command.password()),
                command.rol(),
                command.telefono(),
                command.idAlmacen()
        );

        // 5. Guardar
        return usuarioRepository.guardar(nuevo);
    }
}
