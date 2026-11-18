package com.kairos.Kairos_backend.infrastructure.config;

import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarEmpleadoCommand;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Al arrancar, crea el administrador de la "Empresa demo" si todavía no existe.
 * Así siempre hay alguien que pueda manejar los datos de ejemplo de la V2.
 */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    /** NIT de la "Empresa demo" que crea la migración V3. */
    private static final String NIT_EMPRESA_DEMO = "900000000-1";

    private final UsuarioRepositoryPort usuarioRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final RegistrarUsuarioUseCase registrarUsuario;
    private final String email;
    private final String password;

    public AdminInicial(UsuarioRepositoryPort usuarioRepository,
                        EmpresaRepositoryPort empresaRepository,
                        RegistrarUsuarioUseCase registrarUsuario,
                        @Value("${kairos.admin.email}") String email,
                        @Value("${kairos.admin.password}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.registrarUsuario = registrarUsuario;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existePorEmail(email)) {
            return;
        }
        Optional<Empresa> demo = empresaRepository.buscarPorNit(NIT_EMPRESA_DEMO);
        if (demo.isEmpty()) {
            log.warn("No existe la Empresa demo (NIT {}): no se crea el administrador inicial", NIT_EMPRESA_DEMO);
            return;
        }
        registrarUsuario.registrarEmpleado(new RegistrarEmpleadoCommand(
                "Administrador KAIRÓS", email, password, Rol.ADMINISTRADOR, null, null, demo.get().getId()));
        log.info("Administrador inicial creado: {}", email);
    }
}
