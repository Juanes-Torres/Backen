package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase.RegistrarEmpresaCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADAPTADOR DE ENTRADA (REST) para empresas.
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final RegistrarEmpresaUseCase registrarEmpresa;

    public EmpresaController(RegistrarEmpresaUseCase registrarEmpresa) {
        this.registrarEmpresa = registrarEmpresa;
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
}
