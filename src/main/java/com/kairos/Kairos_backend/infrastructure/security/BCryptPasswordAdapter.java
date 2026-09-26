package com.kairos.Kairos_backend.infrastructure.security;

import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * ADAPTADOR de PasswordEncoderPort usando BCrypt (RNF02 del documento).
 */
@Component
public class BCryptPasswordAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String cifrar(String passwordPlano) {
        return passwordEncoder.encode(passwordPlano);
    }

    @Override
    public boolean coincide(String passwordPlano, String passwordHash) {
        return passwordEncoder.matches(passwordPlano, passwordHash);
    }
}
