package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F1 de punta a punta: registro, login con JWT, perfil, empleados y permisos (401 / 403 / 404).
 */
class UsuariosIntegracionTest extends PruebaIntegracion {

    private Empresa empresa;
    private Almacen sede;
    private Usuario admin;

    @BeforeEach
    void preparar() {
        empresa = empresaActiva();
        sede = almacen(empresa.getId());
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
    }

    @Test
    void registroPublico_creaUnClienteSinDevolverLaContrasena() throws Exception {
        String email = unico("cliente") + "@correo.com";

        mvc.perform(json(post("/api/usuarios"),
                        "{\"nombre\":\"Ana Cliente\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"telefono\":\"3001234567\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // El mismo email otra vez: 409
        mvc.perform(json(post("/api/usuarios"),
                        "{\"nombre\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void camposVaciosOJsonMalEscrito_responden400ConElDetalle() throws Exception {
        mvc.perform(json(post("/api/usuarios"), "{\"nombre\":\"\",\"email\":\"no-es-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());

        mvc.perform(json(post("/api/usuarios"), "{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void login_devuelveElTokenYConEseTokenSeVeElPerfil() throws Exception {
        mvc.perform(json(post("/api/usuarios/login"),
                        "{\"email\":\"" + admin.getEmail() + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(con(admin, get("/api/usuarios/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(admin.getEmail()));
    }

    @Test
    void loginConContrasenaIncorrecta_responde401() throws Exception {
        mvc.perform(json(post("/api/usuarios/login"),
                        "{\"email\":\"" + admin.getEmail() + "\",\"password\":\"Otra123*\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void empleadoDeUnaEmpresaSuspendida_noPuedeEntrar() throws Exception {
        empresa.suspender();
        empresas.guardar(empresa);

        mvc.perform(json(post("/api/usuarios/login"),
                        "{\"email\":\"" + admin.getEmail() + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenOConTokenAlterado_responde401() throws Exception {
        mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer token.falso.alterado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unClienteNoPuedeListarUsuarios_403() throws Exception {
        Usuario cliente = usuario(Rol.CLIENTE, null, null);

        mvc.perform(con(cliente, get("/api/usuarios")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void elAdminCreaUnVendedorYSoloVeLosEmpleadosDeSuEmpresa() throws Exception {
        Usuario deOtraEmpresa = vendedorDeOtraEmpresa();
        String email = unico("vendedor") + "@kairos.com";

        mvc.perform(con(admin, json(post("/api/usuarios/empleados"),
                        "{\"nombre\":\"Luis Vendedor\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD
                                + "\",\"rol\":\"VENDEDOR\",\"idAlmacen\":" + sede.getId() + "}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"));

        mvc.perform(con(admin, get("/api/usuarios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)))
                .andExpect(jsonPath("$[*].email", not(hasItem(deOtraEmpresa.getEmail()))))
                .andExpect(jsonPath("$[*].idEmpresa", everyItem(org.hamcrest.Matchers.is(empresa.getId().intValue()))));
    }

    @Test
    void aislamiento_elAdminNoVeNiModificaEmpleadosDeOtraEmpresa_404() throws Exception {
        Usuario ajeno = vendedorDeOtraEmpresa();

        mvc.perform(con(admin, get("/api/usuarios/" + ajeno.getId()))).andExpect(status().isNotFound());
        mvc.perform(con(admin, patch("/api/usuarios/" + ajeno.getId() + "/desactivar"))).andExpect(status().isNotFound());
    }

    @Test
    void elAdminDesactivaYActivaASuVendedor() throws Exception {
        Usuario vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());

        mvc.perform(con(admin, patch("/api/usuarios/" + vendedor.getId() + "/desactivar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(con(admin, patch("/api/usuarios/" + vendedor.getId() + "/activar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void cadaUsuarioActualizaSusDatos_peroNoLosDeOtro() throws Exception {
        Usuario cliente = usuario(Rol.CLIENTE, null, null);
        Usuario otroCliente = usuario(Rol.CLIENTE, null, null);

        mvc.perform(con(cliente, json(put("/api/usuarios/" + cliente.getId()), "{\"nombre\":\"Nombre Nuevo\",\"telefono\":\"3110000000\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Nuevo"));

        mvc.perform(con(cliente, json(put("/api/usuarios/" + otroCliente.getId()), "{\"nombre\":\"Intruso\"}")))
                .andExpect(status().is4xxClientError());
    }

    private Usuario vendedorDeOtraEmpresa() {
        Long otra = empresaActiva().getId();
        return usuario(Rol.VENDEDOR, otra, almacen(otra).getId());
    }

    @Test
    void cadaRespuestaTraeSuIdDePeticion() throws Exception {
        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"));
    }
}
