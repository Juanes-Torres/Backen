package com.kairos.Kairos_backend.infrastructure.adapter.in.web.garantia;

import com.kairos.Kairos_backend.application.port.in.ConsultarGarantiasUseCase;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADAPTADOR DE ENTRADA (REST) para garantías (F6).
 * Traduce HTTP/JSON <-> casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/garantias")
public class GarantiaController {

    private final ConsultarGarantiasUseCase consultarGarantias;

    public GarantiaController(ConsultarGarantiasUseCase consultarGarantias) {
        this.consultarGarantias = consultarGarantias;
    }

    /**
     * Garantía de cada producto comprado (SCRUM-74): ?cedula=1020304050 o ?factura=F-000001.
     * Solo compras de la empresa del empleado.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<GarantiaResponse> consultar(@RequestParam(required = false) String cedula,
                                            @RequestParam(required = false) String factura,
                                            @AuthenticationPrincipal UsuarioAutenticado yo) {
        return consultarGarantias.consultar(yo.comoSolicitante(), cedula, factura)
                .stream().map(GarantiaResponse::desde).toList();
    }
}
