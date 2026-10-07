package com.kairos.Kairos_backend.infrastructure.config;

import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarEmpleadoCommand;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, crea el PRIMER administrador si todavía no existe.
 * Sin esto nadie podría crear vendedores, productos ni almacenes,
 * porque el registro público solo crea clientes.
 */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepositoryPort usuarioRepository;
    private final RegistrarUsuarioUseCase registrarUsuario;
    private final String email;
    private final String password;

    public AdminInicial(UsuarioRepositoryPort usuarioRepository,
                        RegistrarUsuarioUseCase registrarUsuario,
                        @Value("${kairos.admin.email}") String email,
                        @Value("${kairos.admin.password}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.registrarUsuario = registrarUsuario;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existePorEmail(email)) {
            return;
        }
        registrarUsuario.registrarEmpleado(new RegistrarEmpleadoCommand(
                "Administrador KAIRÓS", email, password, Rol.ADMINISTRADOR, null, null));
        log.info("Administrador inicial creado: {}", email);
    }
}
