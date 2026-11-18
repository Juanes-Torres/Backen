package com.kairos.Kairos_backend.infrastructure.adapter.out.archivos;

import com.kairos.Kairos_backend.domain.model.ImagenProducto;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Descarga de fotos SIN internet: un servidor HTTP local hace de página con fotos. */
class HttpDescargaImagenesAdapterTest {

    private HttpServer servidor;
    private String base;

    @BeforeEach
    void levantarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        responder("/foto.png", 200, new byte[]{1, 2, 3});
        responder("/no-existe.png", 404, new byte[0]);
        responder("/enorme.png", 200, new byte[ImagenProducto.TAMANO_MAXIMO_BYTES + 10]);
        servidor.createContext("/redirige", intercambio -> {
            intercambio.getResponseHeaders().add("Location", "/foto.png");
            intercambio.sendResponseHeaders(302, -1);
            intercambio.close();
        });
        servidor.start();
        base = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    private void responder(String ruta, int estado, byte[] cuerpo) {
        servidor.createContext(ruta, intercambio -> {
            intercambio.sendResponseHeaders(estado, cuerpo.length == 0 ? -1 : cuerpo.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(cuerpo);
            }
        });
    }

    @AfterEach
    void apagar() {
        servidor.stop(0);
    }

    /** En las pruebas se permite la red local: el servidor de prueba está en 127.0.0.1. */
    private HttpDescargaImagenesAdapter conRedLocal() {
        return new HttpDescargaImagenesAdapter(3000, true);
    }

    @Test
    void descargaLaFoto() {
        assertArrayEquals(new byte[]{1, 2, 3}, conRedLocal().descargar(base + "/foto.png").orElseThrow());
    }

    @Test
    void siNoExisteEsMuyGrandeORedirige_devuelveVacio() {
        var adaptador = conRedLocal();
        assertTrue(adaptador.descargar(base + "/no-existe.png").isEmpty());
        assertTrue(adaptador.descargar(base + "/enorme.png").isEmpty());
        assertTrue(adaptador.descargar(base + "/redirige").isEmpty());   // no sigue redirecciones
    }

    @Test
    void direccionesMalas_devuelvenVacio() {
        var adaptador = conRedLocal();
        assertTrue(adaptador.descargar("file:///etc/passwd").isEmpty());
        assertTrue(adaptador.descargar("ftp://servidor/foto.png").isEmpty());
        assertTrue(adaptador.descargar("no es una url").isEmpty());
        assertTrue(adaptador.descargar("/solo/ruta.png").isEmpty());
        assertTrue(adaptador.descargar("http://127.0.0.1:1/nadie-escucha.png").isEmpty());
    }

    @Test
    void enProduccion_nuncaPideDireccionesDeLaRedInterna() {
        var adaptador = new HttpDescargaImagenesAdapter(3000, false);
        assertTrue(adaptador.descargar(base + "/foto.png").isEmpty());          // 127.0.0.1
        assertTrue(adaptador.descargar("http://localhost:8080/api").isEmpty());
        assertTrue(adaptador.descargar("http://169.254.169.254/latest/meta-data").isEmpty());   // panel de la nube
    }

    @Test
    void clasificaLasDireccionesInternas() throws Exception {
        for (String interna : new String[]{"127.0.0.1", "10.1.2.3", "172.16.0.1", "192.168.1.10", "169.254.169.254",
                "0.0.0.0", "100.64.0.1", "224.0.0.1", "::1", "fd00::1", "fe80::1"}) {
            assertTrue(HttpDescargaImagenesAdapter.esInterna(InetAddress.getByName(interna)), interna);
        }
        for (String publica : new String[]{"8.8.8.8", "100.128.0.1", "1.1.1.1", "2606:4700::1111"}) {
            assertFalse(HttpDescargaImagenesAdapter.esInterna(InetAddress.getByName(publica)), publica);
        }
    }
}
