package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;

/**
 * CASO DE USO: registrar usuarios.
 * Las reglas de qué rol se puede crear en cada caso viven aquí, no en el controller.
 */
public interface RegistrarUsuarioUseCase {

    /** Registro público: siempre crea un CLIENTE. */
    Usuario registrarCliente(RegistrarClienteCommand command);

    /** Lo usa un administrador: crea VENDEDOR o ADMINISTRADOR, nunca CLIENTE. */
    Usuario registrarEmpleado(RegistrarEmpleadoCommand command);

    /** Datos del registro público (el password llega en texto plano y aquí se cifra). */
    record RegistrarClienteCommand(
            String nombre,
            String email,
            String password,
            String telefono
    ) {
    }

    /** Datos para crear un empleado (el password llega en texto plano y aquí se cifra). */
    record RegistrarEmpleadoCommand(
            String nombre,
            String email,
            String password,
            Rol rol,
            String telefono,
            Long idAlmacen
    ) {
    }
}
