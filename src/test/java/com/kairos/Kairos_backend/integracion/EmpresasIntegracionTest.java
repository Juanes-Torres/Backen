package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Multiempresa de punta a punta: registro público de la empresa y su ciclo de vida
 * (pendiente → activa → suspendida → activa), que solo maneja el SUPERADMIN.
 */
class EmpresasIntegracionTest extends PruebaIntegracion {

    private Usuario superadmin;

    @BeforeEach
    void preparar() {
        superadmin = usuario(Rol.SUPERADMIN, null, null);
    }

    @Test
    void unaEmpresaSeRegistraPendienteYElSuperadminLaApruebaSuspendeYReactiva() throws Exception {
        String nit = unico("900");
        String emailAdmin = unico("admin") + "@nueva.com";

        String respuesta = mvc.perform(json(post("/api/empresas"),
                        "{\"nombre\":\"Tecno Nueva\",\"nit\":\"" + nit + "\",\"email\":\"" + unico("c") + "@nueva.com\",\"telefono\":\"3000000000\","
                                + "\"administrador\":{\"nombre\":\"Admin Nuevo\",\"email\":\"" + emailAdmin + "\",\"password\":\"" + PASSWORD + "\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresa.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.administrador.rol").value("ADMINISTRADOR"))
                .andReturn().getResponse().getContentAsString();
        long id = idDe(respuesta, "$.empresa.id");

        // Mientras está pendiente, su administrador no puede entrar
        mvc.perform(json(post("/api/usuarios/login"), "{\"email\":\"" + emailAdmin + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(con(superadmin, get("/api/empresas").param("estado", "PENDIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(con(superadmin, patch("/api/empresas/" + id + "/aprobar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        mvc.perform(json(post("/api/usuarios/login"), "{\"email\":\"" + emailAdmin + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/empresas/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(con(superadmin, patch("/api/empresas/" + id + "/suspender")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDA"));
        mvc.perform(get("/api/empresas/activas"))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) id))));

        mvc.perform(con(superadmin, patch("/api/empresas/" + id + "/reactivar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
        mvc.perform(con(superadmin, get("/api/empresas/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nit").value(nit));
    }

    @Test
    void elSuperadminRechazaUnaEmpresaPendiente_yNoSePuedeAprobarDespues() throws Exception {
        Empresa pendiente = empresas.guardar(Empresa.nueva(unico("Dudosa"), unico("NIT"), unico("d") + "@kairos.com", "300"));

        mvc.perform(con(superadmin, patch("/api/empresas/" + pendiente.getId() + "/rechazar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
        mvc.perform(con(superadmin, patch("/api/empresas/" + pendiente.getId() + "/aprobar")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nitRepetidoEsConflicto_409() throws Exception {
        Empresa existente = empresaActiva();

        mvc.perform(json(post("/api/empresas"),
                        "{\"nombre\":\"Copia\",\"nit\":\"" + existente.getNit() + "\",\"email\":\"" + unico("x") + "@k.com\","
                                + "\"administrador\":{\"nombre\":\"A\",\"email\":\"" + unico("a") + "@k.com\",\"password\":\"" + PASSWORD + "\"}}"))
                .andExpect(status().isConflict());
    }

    @Test
    void unAdministradorNoGestionaEmpresas_403() throws Exception {
        Empresa empresa = empresaActiva();
        Usuario admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);

        mvc.perform(con(admin, get("/api/empresas"))).andExpect(status().isForbidden());
        mvc.perform(con(admin, patch("/api/empresas/" + empresa.getId() + "/suspender"))).andExpect(status().isForbidden());
    }

    @Test
    void empresaQueNoExiste_404() throws Exception {
        mvc.perform(con(superadmin, get("/api/empresas/99999999"))).andExpect(status().isNotFound());
    }
}
