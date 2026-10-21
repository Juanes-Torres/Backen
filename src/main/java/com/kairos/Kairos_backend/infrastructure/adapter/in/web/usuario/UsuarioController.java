package com.kairos.Kairos_backend.infrastructure.adapter.in.web.usuario;

import com.kairos.Kairos_backend.application.port.in.ActualizarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.BuscarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarClienteCommand;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarEmpleadoCommand;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa.EmpresaResponse;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        Usuario creado = registrarUsuario.registrarCliente(new RegistrarClienteCommand(
                request.nombre(), request.email(), request.password(), request.telefono()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    /** RF02 - Inicio de sesión: devuelve el JWT. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var resultado = autenticarUsuario.autenticar(request.email(), request.password());
        var empresa = resultado.empresa() == null ? null : EmpresaResponse.desde(resultado.empresa());
        return new LoginResponse(resultado.token(), "Bearer", UsuarioResponse.desde(resultado.usuario()), empresa);
    }

    /** Datos del usuario que tiene la sesión iniciada. */
    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.desde(buscarUsuario.buscarPorEmail(yo.email()));
    }

    /** Solo ADMINISTRADOR: crea vendedores u otros administradores en SU empresa. */
    @PostMapping("/empleados")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> crearEmpleado(@Valid @RequestBody CrearEmpleadoRequest request,
                                                         @AuthenticationPrincipal UsuarioAutenticado yo) {
        Usuario creado = registrarUsuario.registrarEmpleado(new RegistrarEmpleadoCommand(
                request.nombre(), request.email(), request.password(),
                request.rol(), request.telefono(), request.idAlmacen(), yo.idEmpresa()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    /** Solo ADMINISTRADOR: los usuarios de SU empresa. */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioResponse> listar(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return buscarUsuario.listar(yo.comoSolicitante()).stream().map(UsuarioResponse::desde).toList();
    }

    /** Cada uno se ve a sí mismo; el ADMINISTRADOR, a los de su empresa; el SUPERADMIN, a los clientes. */
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.desde(buscarUsuario.buscarPorId(yo.comoSolicitante(), id));
    }

    /** Cada uno se modifica a sí mismo; el ADMINISTRADOR, además, a los de su empresa. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.desde(actualizarUsuario.actualizarDatos(
                yo.comoSolicitante(), id, request.nombre(), request.telefono()));
    }

    /** ADMINISTRADOR: empleados de su empresa. SUPERADMIN: clientes. */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERADMIN')")
    public UsuarioResponse desactivar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.desde(actualizarUsuario.cambiarEstado(yo.comoSolicitante(), id, false));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERADMIN')")
    public UsuarioResponse activar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        return UsuarioResponse.desde(actualizarUsuario.cambiarEstado(yo.comoSolicitante(), id, true));
    }
}
