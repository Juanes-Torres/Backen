package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.NotificacionPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private static final int LONGITUD_MINIMA_PASSWORD = 6;

    private final UsuarioRepositoryPort usuarioRepository;
    private final AlmacenRepositoryPort almacenRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final NotificacionPort notificacionPort;

    public RegistrarUsuarioService(UsuarioRepositoryPort usuarioRepository,
                                   AlmacenRepositoryPort almacenRepository,
                                   PasswordEncoderPort passwordEncoder,
                                   NotificacionPort notificacionPort) {
        this.usuarioRepository = usuarioRepository;
        this.almacenRepository = almacenRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificacionPort = notificacionPort;
    }

    @Override
    public Usuario registrarCliente(RegistrarClienteCommand command) {
        // Regla: el registro público siempre crea un CLIENTE y sin almacén
        return registrar(command.nombre(), command.email(), command.password(),
                Rol.CLIENTE, command.telefono(), null);
    }

    @Override
    public Usuario registrarEmpleado(RegistrarEmpleadoCommand command) {
        // Regla: un empleado nunca es CLIENTE (los clientes usan el registro público)
        if (command.rol() == Rol.CLIENTE) {
            throw new ReglaNegocioException("Los clientes se registran por POST /api/usuarios");
        }
        return registrar(command.nombre(), command.email(), command.password(),
                command.rol(), command.telefono(), command.idAlmacen());
    }

    /** Pasos comunes a todo registro. */
    private Usuario registrar(String nombre, String email, String password,
                              Rol rol, String telefono, Long idAlmacen) {
        // 1. La contraseña en texto plano solo existe aquí: validarla antes de cifrarla
        if (password == null || password.length() < LONGITUD_MINIMA_PASSWORD) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
        }

        // 2. El email no puede estar repetido
        if (email != null && usuarioRepository.existePorEmail(email)) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el email " + email);
        }

        // 3. Si trae almacén, el almacén debe existir
        if (idAlmacen != null && !almacenRepository.existePorId(idAlmacen)) {
            throw new RecursoNoEncontradoException("No existe un almacén con id " + idAlmacen);
        }

        // 4. El dominio valida sus reglas (nombre, email, rol, vendedor con almacén...)
        Usuario nuevo = Usuario.nuevo(
                nombre,
                email,
                passwordEncoder.cifrar(password),
                rol,
                telefono,
                idAlmacen
        );

        // 5. Guardar
        Usuario guardado = usuarioRepository.guardar(nuevo);

        // 6. Dar la bienvenida: es asíncrono, no bloquea ni hace fallar el registro
        notificacionPort.enviarBienvenida(guardado);
        return guardado;
    }
}
