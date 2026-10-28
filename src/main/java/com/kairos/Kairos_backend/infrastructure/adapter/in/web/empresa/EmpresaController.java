package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import com.kairos.Kairos_backend.application.port.in.ConsultarEmpresasActivasUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarEmpresasUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase.RegistrarEmpresaCommand;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADAPTADOR DE ENTRADA (REST) para empresas.
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final RegistrarEmpresaUseCase registrarEmpresa;
    private final GestionarEmpresasUseCase gestionarEmpresas;
    private final ConsultarEmpresasActivasUseCase empresasActivas;

    public EmpresaController(RegistrarEmpresaUseCase registrarEmpresa,
                             GestionarEmpresasUseCase gestionarEmpresas,
                             ConsultarEmpresasActivasUseCase empresasActivas) {
        this.registrarEmpresa = registrarEmpresa;
        this.gestionarEmpresas = gestionarEmpresas;
        this.empresasActivas = empresasActivas;
    }

    /** Público: las empresas activas, para que el cliente elija dónde comprar. */
    @GetMapping("/activas")
    public List<EmpresaPublicaResponse> listarActivas() {
        return empresasActivas.listarActivas().stream().map(EmpresaPublicaResponse::desde).toList();
    }

    /** Registro público: la empresa queda PENDIENTE junto con su administrador. */
    @PostMapping
    public ResponseEntity<RegistroEmpresaResponse> registrar(@Valid @RequestBody RegistroEmpresaRequest request) {
        var admin = request.administrador();
        var resultado = registrarEmpresa.registrar(new RegistrarEmpresaCommand(
                request.nombre(), request.nit(), request.email(), request.telefono(),
                admin.nombre(), admin.email(), admin.password(), admin.telefono()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistroEmpresaResponse.desde(resultado));
    }

    /** Solo SUPERADMIN: todas las empresas, o solo las de un estado (?estado=PENDIENTE). */
    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<EmpresaResponse> listar(@RequestParam(required = false) EstadoEmpresa estado) {
        return gestionarEmpresas.listar(estado).stream().map(EmpresaResponse::desde).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public EmpresaResponse buscarPorId(@PathVariable Long id) {
        return EmpresaResponse.desde(gestionarEmpresas.buscarPorId(id));
    }

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public EmpresaResponse aprobar(@PathVariable Long id) {
        return EmpresaResponse.desde(gestionarEmpresas.aprobar(id));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public EmpresaResponse rechazar(@PathVariable Long id) {
        return EmpresaResponse.desde(gestionarEmpresas.rechazar(id));
    }

    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public EmpresaResponse suspender(@PathVariable Long id) {
        return EmpresaResponse.desde(gestionarEmpresas.suspender(id));
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public EmpresaResponse reactivar(@PathVariable Long id) {
        return EmpresaResponse.desde(gestionarEmpresas.reactivar(id));
    }
}
