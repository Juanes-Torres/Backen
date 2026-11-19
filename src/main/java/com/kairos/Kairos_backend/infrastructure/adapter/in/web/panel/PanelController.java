package com.kairos.Kairos_backend.infrastructure.adapter.in.web.panel;

import com.kairos.Kairos_backend.application.port.in.ConsultarPanelUseCase;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Panel de inicio con indicadores (SCRUM-106). */
@RestController
@RequestMapping("/api/panel")
public class PanelController {

    private final ConsultarPanelUseCase panel;

    public PanelController(ConsultarPanelUseCase panel) {
        this.panel = panel;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR','SUPERADMIN')")
    public PanelResponse panel(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return PanelResponse.desde(panel.panel(yo.comoSolicitante()));
    }
}
