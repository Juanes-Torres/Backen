package com.kairos.Kairos_backend.infrastructure.adapter.out.archivos;

import com.kairos.Kairos_backend.application.port.out.DescargaImagenesPort;
import com.kairos.Kairos_backend.domain.model.Imagen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * ADAPTADOR de DescargaImagenesPort con el cliente HTTP de Java.
 *
 * Seguridad (SSRF): la dirección viene de una API externa, así que alguien podría hacer que KAIRÓS
 * pida páginas de la red interna del servidor (la base de datos, el panel de la nube...). Por eso:
 *   - solo http y https, y nunca direcciones locales o privadas (127.x, 10.x, 192.168.x, 169.254.x...)
 *   - no sigue redirecciones (una página pública podría redirigir a una interna)
 *   - corta la descarga si pasa de 2 MB y si tarda más que kairos.apis.timeout-ms
 */
@Component
public class HttpDescargaImagenesAdapter implements DescargaImagenesPort {

    private static final Logger log = LoggerFactory.getLogger(HttpDescargaImagenesAdapter.class);

    private final HttpClient cliente;
    private final Duration tiempoEspera;
    private final boolean permitirRedLocal;

    public HttpDescargaImagenesAdapter(@Value("${kairos.apis.timeout-ms}") long timeoutMs,
                                       @Value("${kairos.apis.imagenes.permitir-red-local:false}") boolean permitirRedLocal) {
        this.tiempoEspera = Duration.ofMillis(timeoutMs);
        this.permitirRedLocal = permitirRedLocal;   // true SOLO en las pruebas, que usan un servidor local
        this.cliente = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(tiempoEspera)
                .build();
    }

    @Override
    public Optional<byte[]> descargar(String url) {
        try {
            URI uri = URI.create(url.trim());
            if (!esPermitida(uri)) {
                log.warn("Descarga de imagen bloqueada (dirección no permitida): {}", url);
                return Optional.empty();
            }
            HttpRequest peticion = HttpRequest.newBuilder(uri).timeout(tiempoEspera)
                    .header("Accept", "image/jpeg, image/png, image/webp")
                    .header("User-Agent", "KAIROS/1.0").GET().build();
            HttpResponse<InputStream> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream cuerpo = respuesta.body()) {
                if (respuesta.statusCode() != 200) {
                    log.info("La imagen {} respondió {}", url, respuesta.statusCode());
                    return Optional.empty();
                }
                // Se lee un byte de más: si llega, la imagen pasa del máximo y se descarta
                byte[] contenido = cuerpo.readNBytes(Imagen.TAMANO_MAXIMO_BYTES + 1);
                if (contenido.length > Imagen.TAMANO_MAXIMO_BYTES) {
                    log.info("La imagen {} pesa más de 2 MB", url);
                    return Optional.empty();
                }
                return Optional.of(contenido);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | IllegalArgumentException e) {
            log.info("No se pudo descargar la imagen {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean esPermitida(URI uri) throws IOException {
        String esquema = uri.getScheme();
        if (uri.getHost() == null || esquema == null
                || !(esquema.equalsIgnoreCase("http") || esquema.equalsIgnoreCase("https"))) {
            return false;
        }
        if (permitirRedLocal) {
            return true;
        }
        for (InetAddress direccion : InetAddress.getAllByName(uri.getHost())) {
            if (esInterna(direccion)) {
                return false;
            }
        }
        return true;
    }

    static boolean esInterna(InetAddress d) {
        if (d.isAnyLocalAddress() || d.isLoopbackAddress() || d.isLinkLocalAddress()
                || d.isSiteLocalAddress() || d.isMulticastAddress()) {
            return true;
        }
        byte[] b = d.getAddress();
        if (d instanceof Inet4Address) {
            int primero = b[0] & 0xFF;
            int segundo = b[1] & 0xFF;
            return primero == 0                                   // 0.0.0.0/8
                    || (primero == 100 && segundo >= 64 && segundo <= 127);   // 100.64.0.0/10 (red del proveedor)
        }
        return d instanceof Inet6Address && (b[0] & 0xFE) == 0xFC;   // fc00::/7 (red privada IPv6)
    }
}
