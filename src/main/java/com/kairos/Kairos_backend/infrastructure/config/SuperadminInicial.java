package com.kairos.Kairos_backend.infrastructure.config;

import com.kairos.Kairos_backend.application.port.in.CrearSuperadminUseCase;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, crea el SUPERADMINISTRADOR de la plataforma si todavía no existe.
 * Sin él nadie podría aprobar empresas, porque la API no permite crearlo.
 */
@Component
public class SuperadminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperadminInicial.class);

    private final UsuarioRepositoryPort usuarioRepository;
    private final CrearSuperadminUseCase crearSuperadmin;
    private final String email;
    private final String password;

    public SuperadminInicial(UsuarioRepositoryPort usuarioRepository,
                             CrearSuperadminUseCase crearSuperadmin,
                             @Value("${kairos.superadmin.email}") String email,
                             @Value("${kairos.superadmin.password}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.crearSuperadmin = crearSuperadmin;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existePorEmail(email)) {
            return;
        }
        crearSuperadmin.registrarSuperadmin("Superadministrador KAIRÓS", email, password);
        log.info("Superadministrador inicial creado: {}", email);
    }
}
