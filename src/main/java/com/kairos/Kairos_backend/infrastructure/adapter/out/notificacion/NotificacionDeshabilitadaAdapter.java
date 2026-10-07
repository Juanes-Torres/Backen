package com.kairos.Kairos_backend.infrastructure.adapter.out.notificacion;

import com.kairos.Kairos_backend.application.port.out.NotificacionPort;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Se usa cuando el correo está apagado (kairos.mail.enabled=false):
 * la app funciona igual y solo deja constancia en el log.
 */
@Component
@ConditionalOnProperty(name = "kairos.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NotificacionDeshabilitadaAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacionDeshabilitadaAdapter.class);

    @Override
    public void enviarBienvenida(Usuario usuario) {
        log.info("Correo deshabilitado: no se envía la bienvenida a {}", usuario.getEmail());
    }
}
