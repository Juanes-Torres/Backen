package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.application.port.out.AlmacenamientoArchivosPort;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Marca de la empresa (SCRUM-105): perfil editable y logo, sin migración de base de datos. */
@TestPropertySource(properties = "kairos.archivos.carpeta=target/archivos-prueba")
class MarcaEmpresaIntegracionTest extends PruebaIntegracion {

    static final byte[] PNG = Arrays.copyOf(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}, 40);

    @Autowired
    private AlmacenamientoArchivosPort archivos;

    private Empresa empresa;
    private Usuario admin;
    private Usuario vendedor;

    @BeforeEach
    void preparar() {
        empresa = empresaActiva();
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), almacen(empresa.getId()).getId());
    }

    private MockMultipartHttpServletRequestBuilder subirLogo(byte[] contenido) {
        return multipart("/api/empresas/mi-empresa/logo")
                .file(new MockMultipartFile("archivo", "logo.png", "image/png", contenido));
    }

    @Test
    void losEmpleadosVenSuEmpresa_yElAdminLaEdita() throws Exception {
        mvc.perform(con(vendedor, get("/api/empresas/mi-empresa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(empresa.getNombre()))
                .andExpect(jsonPath("$.nit").value(empresa.getNit()))
                .andExpect(jsonPath("$.logoUrl").doesNotExist());

        mvc.perform(con(admin, json(put("/api/empresas/mi-empresa"),
                        "{\"nombre\":\"TecnoStore Chía\",\"email\":\"ventas@tecnostore.co\",\"telefono\":\"6011234567\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("TecnoStore Chía"))
                .andExpect(jsonPath("$.email").value("ventas@tecnostore.co"))
                .andExpect(jsonPath("$.nit").value(empresa.getNit()));   // el NIT no cambia

        mvc.perform(con(vendedor, json(put("/api/empresas/mi-empresa"), "{\"nombre\":\"X\",\"email\":\"x@x.co\"}")))
                .andExpect(status().isForbidden());
        mvc.perform(con(admin, json(put("/api/empresas/mi-empresa"), "{\"nombre\":\"\",\"email\":\"no-es-correo\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.email").exists());
        mvc.perform(con(usuario(Rol.CLIENTE, null, null), get("/api/empresas/mi-empresa")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdminSubeElLogo_yApareceEnElLogin_laListaPublica_yLaRutaPublica() throws Exception {
        String logoUrl = "/api/empresas/" + empresa.getId() + "/logo";
        mvc.perform(con(admin, subirLogo(PNG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoUrl").value(logoUrl));

        // Público, con "huella" (ETag): si no cambió, el navegador recibe 304 sin descargarlo otra vez
        String huella = mvc.perform(get(logoUrl))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(PNG))
                .andReturn().getResponse().getHeader("ETag");
        mvc.perform(get(logoUrl).header("If-None-Match", huella)).andExpect(status().isNotModified());

        mvc.perform(get("/api/empresas/activas"))
                .andExpect(jsonPath("$[?(@.id == " + empresa.getId() + ")].logoUrl").value(
                        org.hamcrest.Matchers.contains(logoUrl)));
        mvc.perform(json(post("/api/usuarios/login"), "{\"email\":\"" + admin.getEmail() + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa.logoUrl").value(logoUrl));
    }

    @Test
    void quitarLogo_soloImagenes_ySoloEmpresasActivas() throws Exception {
        mvc.perform(con(admin, subirLogo("<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La imagen debe ser JPG, PNG o WEBP"));
        mvc.perform(con(vendedor, subirLogo(PNG))).andExpect(status().isForbidden());

        mvc.perform(con(admin, subirLogo(PNG))).andExpect(status().isOk());
        mvc.perform(con(admin, delete("/api/empresas/mi-empresa/logo"))).andExpect(status().isNoContent());
        mvc.perform(get("/api/empresas/{id}/logo", empresa.getId())).andExpect(status().isNotFound());
        mvc.perform(con(admin, get("/api/empresas/mi-empresa"))).andExpect(jsonPath("$.logoUrl").doesNotExist());

        // Una empresa pendiente no muestra su logo aunque el archivo exista
        Empresa pendiente = empresas.guardar(Empresa.nueva(unico("Pendiente"), unico("NIT"), unico("p") + "@k.co", null));
        archivos.guardar("empresas/" + pendiente.getId() + "/logo", PNG);
        mvc.perform(get("/api/empresas/{id}/logo", pendiente.getId())).andExpect(status().isNotFound());
    }
}
