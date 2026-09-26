package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import com.kairos.Kairos_backend.application.port.in.ActualizarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.BuscarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarUsuarioCommand;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADAPTADOR DE ENTRADA (REST) para usuarios.
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final BuscarUsuarioUseCase buscarUsuario;
    private final ActualizarUsuarioUseCase actualizarUsuario;

    public UsuarioController(RegistrarUsuarioUseCase registrarUsuario,
                             AutenticarUsuarioUseCase autenticarUsuario,
                             BuscarUsuarioUseCase buscarUsuario,
                             ActualizarUsuarioUseCase actualizarUsuario) {
        this.registrarUsuario = registrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.buscarUsuario = buscarUsuario;
        this.actualizarUsuario = actualizarUsuario;
    }

    /** RF01 - Registro público de clientes. Siempre crea rol CLIENTE. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrarCliente(@Valid @RequestBody RegistroClienteRequest request) {
        Usuario creado = registrarUsuario.registrar(new RegistrarUsuarioCommand(
                request.nombre(), request.email(), request.password(),
                Rol.CLIENTE, request.telefono(), null));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    /** RF02 - Inicio de sesión: devuelve el JWT. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var resultado = autenticarUsuario.autenticar(request.email(), request.password());
        return new LoginResponse(resultado.token(), "Bearer", UsuarioResponse.desde(resultado.usuario()));
    }

    /** Datos del usuario que tiene la sesión iniciada. */
    @GetMapping("/me")
    public UsuarioResponse miPerfil(Authentication authentication) {
        return UsuarioResponse.desde(buscarUsuario.buscarPorEmail(authentication.getName()));
    }

    /** Solo ADMINISTRADOR: crear vendedores u otros administradores. */
    @PostMapping("/empleados")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody CrearEmpleadoRequest request) {
        if (request.rol() == Rol.CLIENTE) {
            throw new ReglaNegocioException("Los clientes se registran por POST /api/usuarios");
        }
        Usuario creado = registrarUsuario.registrar(new RegistrarUsuarioCommand(
                request.nombre(), request.email(), request.password(),
                request.rol(), request.telefono(), request.idAlmacen()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    /** Solo ADMINISTRADOR: listar todos los usuarios. */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioResponse> listar() {
        return buscarUsuario.listar().stream().map(UsuarioResponse::desde).toList();
    }

    /** ADMINISTRADOR ve a cualquiera; los demás solo a sí mismos. */
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id, Authentication authentication) {
        validarPropietarioOAdmin(id, authentication);
        return UsuarioResponse.desde(buscarUsuario.buscarPorId(id));
    }

    /** ADMINISTRADOR modifica a cualquiera; los demás solo a sí mismos. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest request,
                                      Authentication authentication) {
        validarPropietarioOAdmin(id, authentication);
        return UsuarioResponse.desde(actualizarUsuario.actualizarDatos(id, request.nombre(), request.telefono()));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse desactivar(@PathVariable Long id) {
        return UsuarioResponse.desde(actualizarUsuario.cambiarEstado(id, false));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse activar(@PathVariable Long id) {
        return UsuarioResponse.desde(actualizarUsuario.cambiarEstado(id, true));
    }

    private void validarPropietarioOAdmin(Long id, Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        if (esAdmin) {
            return;
        }
        Usuario actual = buscarUsuario.buscarPorEmail(authentication.getName());
        if (!actual.getId().equals(id)) {
            throw new AccessDeniedException("Solo puede consultar o modificar su propio usuario");
        }
    }
}
