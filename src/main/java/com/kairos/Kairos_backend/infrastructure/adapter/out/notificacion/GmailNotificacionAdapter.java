package com.kairos.Kairos_backend.infrastructure.adapter.out.notificacion;

import com.kairos.Kairos_backend.application.port.out.NotificacionPort;
import com.kairos.Kairos_backend.domain.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;

/**
 * ADAPTADOR de NotificacionPort que envía correos con el servidor SMTP de Gmail.
 * Solo se activa si kairos.mail.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "kairos.mail.enabled", havingValue = "true")
public class GmailNotificacionAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(GmailNotificacionAdapter.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String nombreRemitente;
    private final String urlFrontend;

    public GmailNotificacionAdapter(JavaMailSender mailSender,
                                    @Value("${spring.mail.username}") String remitente,
                                    @Value("${kairos.mail.from-name:KAIRÓS}") String nombreRemitente,
                                    @Value("${kairos.frontend.url:http://localhost:5173}") String urlFrontend) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
        this.urlFrontend = urlFrontend;
    }

    @Async
    @Override
    public void enviarBienvenida(Usuario usuario) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setFrom(remitente, nombreRemitente);
            helper.setTo(usuario.getEmail());
            helper.setSubject("¡Bienvenido a KAIRÓS!");
            helper.setText(plantilla(usuario), true);   // true = contenido HTML
            mailSender.send(mensaje);
            log.info("Correo de bienvenida enviado a {}", usuario.getEmail());
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            // El registro ya quedó guardado: solo se deja constancia del fallo
            log.warn("No se pudo enviar el correo de bienvenida a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }

    private String plantilla(Usuario usuario) {
        String nombre = HtmlUtils.htmlEscape(usuario.getNombre());
        String email = HtmlUtils.htmlEscape(usuario.getEmail());
        return """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #d9dee5;border-radius:10px;overflow:hidden">
                  <div style="background:#1f4e79;color:#fff;padding:20px 24px;font-size:24px;font-weight:bold;letter-spacing:2px">KAIRÓS</div>
                  <div style="padding:24px;color:#1f2933;line-height:1.5">
                    <h2 style="margin-top:0">¡Hola, %s!</h2>
                    <p>Tu cuenta en <strong>KAIRÓS</strong> se creó correctamente.</p>
                    <p><strong>Usuario:</strong> %s<br><strong>Rol:</strong> %s</p>
                    <p>Ya puedes iniciar sesión, consultar nuestro catálogo y revisar tus compras.</p>
                    <p style="text-align:center;margin:28px 0">
                      <a href="%s/login" style="background:#1f4e79;color:#fff;padding:12px 22px;border-radius:6px;text-decoration:none">Iniciar sesión</a>
                    </p>
                    <p style="font-size:12px;color:#6b7280">Si no creaste esta cuenta, ignora este mensaje.</p>
                  </div>
                </div>
                """.formatted(nombre, email, usuario.getRol().name(), urlFrontend);
    }
}
