package com.kairos.Kairos_backend.infrastructure.adapter.in.web;

import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public UsuarioController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                             AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@RequestBody RegistrarUsuarioRequest request) {

        Usuario usuario = registrarUsuarioUseCase.registrar(
                request.getNombre(),
                request.getEmail(),
                request.getPassword(),
                request.getRol(),
                request.getTelefono(),
                request.getIdAlmacen()
        );

        return UsuarioResponse.desde(usuario);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        return autenticarUsuarioUseCase.autenticar(
                request.getEmail(),
                request.getPassword()
        );
    }
}