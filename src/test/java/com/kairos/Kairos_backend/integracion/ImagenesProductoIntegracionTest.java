package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.application.port.out.AlmacenamientoArchivosPort;
import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort;
import com.kairos.Kairos_backend.application.port.out.DescargaImagenesPort;
import com.kairos.Kairos_backend.domain.model.CodigoBarras;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fotos de los productos de punta a punta (SCRUM-103). Las fotos se guardan en target/archivos-prueba;
 * el catálogo de códigos de barras y las descargas de internet son falsos (sin internet).
 */
@TestPropertySource(properties = "kairos.archivos.carpeta=target/archivos-prueba")
class ImagenesProductoIntegracionTest extends PruebaIntegracion {

    static final byte[] PNG = foto(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
    static final byte[] JPG = foto(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});

    static final String CODIGO_CON_FOTO = "190199098572";
    static final String CODIGO_SIN_FOTO_UTIL = "12345670";

    private static byte[] foto(byte[] firma) {
        byte[] contenido = Arrays.copyOf(firma, firma.length + 50);
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 3);
        return contenido;
    }

    @TestConfiguration
    static class SinInternet {
        /** El catálogo ofrece una foto rota, una que no es imagen y una buena: se debe usar la buena. */
        @Bean
        @Primary
        CatalogoExternoPort catalogoConFotos() {
            Map<String, List<String>> fotos = Map.of(
                    CODIGO_CON_FOTO, List.of("http://roto/1.jpg", "http://texto/2.png", "http://bien/3.png"),
                    CODIGO_SIN_FOTO_UTIL, List.of("http://roto/1.jpg"));
            return (CodigoBarras codigo) -> Optional.ofNullable(fotos.get(codigo.valor()))
                    .map(urls -> new CatalogoExternoPort.DatosProductoExterno("AirPods", "Apple", null, null, urls));
        }

        @Bean
        @Primary
        DescargaImagenesPort descargasFalsas() {
            Map<String, byte[]> paginas = Map.of(
                    "http://texto/2.png", "no soy una foto".getBytes(StandardCharsets.UTF_8),
                    "http://bien/3.png", PNG);
            return url -> Optional.ofNullable(paginas.get(url));
        }
    }

    @Autowired
    private AlmacenamientoArchivosPort archivos;

    private Long idEmpresa;
    private Usuario admin;
    private Producto producto;

    @BeforeEach
    void preparar() {
        idEmpresa = empresaActiva().getId();
        admin = usuario(Rol.ADMINISTRADOR, idEmpresa, null);
        producto = producto(idEmpresa, categoria().getId(), "1500000");
    }

    private MockMultipartHttpServletRequestBuilder subir(byte[] contenido) {
        return multipart("/api/productos/{id}/imagen", producto.getId())
                .file(new MockMultipartFile("archivo", "foto.png", "image/png", contenido));
    }

    private String rutaGuardada() {
        return productos.buscarPorId(producto.getId()).orElseThrow().getImagen();
    }

    @Test
    void elAdminSubeLaFoto_yCualquieraLaVeEnElCatalogo() throws Exception {
        mvc.perform(con(admin, subir(PNG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenUrl", startsWith("/api/productos/" + producto.getId() + "/imagen?v=")));

        // Público y sin token, como el catálogo
        mvc.perform(get("/api/productos/{id}/imagen", producto.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")))
                .andExpect(content().bytes(PNG));
        mvc.perform(get("/api/productos/{id}", producto.getId()))
                .andExpect(jsonPath("$.imagenUrl", startsWith("/api/productos/")));
    }

    @Test
    void alCambiarLaFoto_seBorraLaAnterior() throws Exception {
        mvc.perform(con(admin, subir(PNG))).andExpect(status().isOk());
        String anterior = rutaGuardada();

        mvc.perform(con(admin, subir(JPG))).andExpect(status().isOk());

        assertTrue(archivos.leer(anterior).isEmpty());
        assertTrue(rutaGuardada().endsWith(".jpg"));
        mvc.perform(get("/api/productos/{id}/imagen", producto.getId()))
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void soloFotosDeVerdad_yMaximo2MB() throws Exception {
        mvc.perform(con(admin, subir("<html>hola</html>".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La imagen debe ser JPG, PNG o WEBP"));
        mvc.perform(con(admin, subir(Arrays.copyOf(PNG, 2 * 1024 * 1024 + 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La imagen no puede pesar más de 2 MB"));
        mvc.perform(con(admin, multipart("/api/productos/{id}/imagen", producto.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("'archivo'")));
        mvc.perform(con(admin, json(post("/api/productos/{id}/imagen", producto.getId()), "{}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("form-data")));
    }

    @Test
    void permisos_soloElAdminDeLaMismaEmpresa() throws Exception {
        Usuario vendedor = usuario(Rol.VENDEDOR, idEmpresa, almacen(idEmpresa).getId());
        Long otra = empresaActiva().getId();
        Usuario adminAjeno = usuario(Rol.ADMINISTRADOR, otra, null);

        mvc.perform(subir(PNG)).andExpect(status().isUnauthorized());
        mvc.perform(con(vendedor, subir(PNG))).andExpect(status().isForbidden());
        mvc.perform(con(adminAjeno, subir(PNG))).andExpect(status().isNotFound());
        mvc.perform(con(adminAjeno, delete("/api/productos/{id}/imagen", producto.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void quitarLaFoto() throws Exception {
        mvc.perform(get("/api/productos/{id}/imagen", producto.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("no tiene imagen")));
        mvc.perform(con(admin, delete("/api/productos/{id}/imagen", producto.getId())))
                .andExpect(status().isNoContent());   // sin foto: no pasa nada

        mvc.perform(con(admin, subir(PNG))).andExpect(status().isOk());
        String ruta = rutaGuardada();
        mvc.perform(con(admin, delete("/api/productos/{id}/imagen", producto.getId())))
                .andExpect(status().isNoContent());

        assertTrue(archivos.leer(ruta).isEmpty());
        mvc.perform(get("/api/productos/{id}/imagen", producto.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/productos/{id}", producto.getId())).andExpect(jsonPath("$.imagenUrl").doesNotExist());
    }

    @Test
    void fotoDesdeElCodigoDeBarras_usaLaPrimeraQueSirve() throws Exception {
        // La consulta del código ya muestra las fotos sugeridas
        mvc.perform(con(admin, get("/api/productos/codigo-barras/" + CODIGO_CON_FOTO)))
                .andExpect(jsonPath("$.imagenes.length()").value(3));

        mvc.perform(con(admin, json(post("/api/productos/{id}/imagen/codigo-barras", producto.getId()),
                        "{\"codigoBarras\":\"" + CODIGO_CON_FOTO + "\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenUrl", startsWith("/api/productos/")));
        mvc.perform(get("/api/productos/{id}/imagen", producto.getId())).andExpect(content().bytes(PNG));
    }

    @Test
    void fotoDesdeElCodigoDeBarras_sinFotoUtilOCodigoDesconocido() throws Exception {
        mvc.perform(con(admin, json(post("/api/productos/{id}/imagen/codigo-barras", producto.getId()),
                        "{\"codigoBarras\":\"" + CODIGO_SIN_FOTO_UTIL + "\"}")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("súbala a mano")));
        mvc.perform(con(admin, json(post("/api/productos/{id}/imagen/codigo-barras", producto.getId()),
                        "{\"codigoBarras\":\"4006381333931\"}")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("No se encontró un producto")));
        mvc.perform(con(admin, json(post("/api/productos/{id}/imagen/codigo-barras", producto.getId()), "{}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.codigoBarras").exists());
    }
}
