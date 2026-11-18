package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reclamos de garantía de punta a punta (SCRUM-75): un cliente compra un celular (12 meses)
 * y un cable sin garantía (0 meses), y luego reclama.
 */
class ReclamosIntegracionTest extends PruebaIntegracion {

    private Usuario vendedor;
    private Usuario admin;
    private Usuario cliente;
    private Producto celular;
    private Producto cable;
    private long idVenta;

    @BeforeEach
    void preparar() throws Exception {
        Empresa empresa = empresaActiva();
        Almacen sede = almacen(empresa.getId());
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        cliente = usuario(Rol.CLIENTE, null, null);
        Long categoria = categoria().getId();
        celular = productos.guardar(Producto.nuevo(unico("Celular"), null, new BigDecimal("900000"), null, categoria,
                empresa.getId(), 12));
        cable = productos.guardar(Producto.nuevo(unico("Cable"), null, new BigDecimal("15000"), null, categoria,
                empresa.getId(), 0));
        inventario(celular.getId(), sede.getId(), 3, 1);
        inventario(cable.getId(), sede.getId(), 3, 1);

        String respuesta = mvc.perform(con(vendedor, json(post("/api/ventas"),
                        "{\"emailCliente\":\"" + cliente.getEmail() + "\",\"items\":["
                                + "{\"idProducto\":" + celular.getId() + ",\"cantidad\":1},"
                                + "{\"idProducto\":" + cable.getId() + ",\"cantidad\":1}]}")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        idVenta = idDe(respuesta, "$.id");
    }

    private String reclamo(Long idProducto) {
        return "{\"idVenta\":" + idVenta + ",\"idProducto\":" + idProducto + ",\"motivo\":\"La pantalla no enciende\"}";
    }

    private long registrar() throws Exception {
        String respuesta = mvc.perform(con(vendedor, json(post("/api/garantias/reclamos"), reclamo(celular.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECIBIDO"))
                .andExpect(jsonPath("$.idCliente").value(cliente.getId()))
                .andExpect(jsonPath("$.historial", hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        return idDe(respuesta, "$.id");
    }

    @Test
    void seRegistraYSeSigueHastaLaEntrega_conHistorial() throws Exception {
        long id = registrar();

        mvc.perform(con(vendedor, json(patch("/api/garantias/reclamos/" + id + "/estado"), "{\"estado\":\"EN_REVISION\"}")))
                .andExpect(status().isOk());
        mvc.perform(con(admin, json(patch("/api/garantias/reclamos/" + id + "/estado"),
                        "{\"estado\":\"REPARADO\",\"nota\":\"Se cambió el flex de la pantalla\"}")))
                .andExpect(status().isOk());
        mvc.perform(con(vendedor, json(patch("/api/garantias/reclamos/" + id + "/estado"), "{\"estado\":\"ENTREGADO\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.historial", hasSize(4)))
                .andExpect(jsonPath("$.historial[2].estado").value("REPARADO"))
                .andExpect(jsonPath("$.historial[2].nota").value("Se cambió el flex de la pantalla"))
                .andExpect(jsonPath("$.historial[2].idUsuario").value(admin.getId()));
    }

    @Test
    void elClienteVeElEstadoDeSusReclamos() throws Exception {
        long id = registrar();

        mvc.perform(con(cliente, get("/api/garantias/reclamos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("RECIBIDO"));
        mvc.perform(con(cliente, get("/api/garantias/reclamos/" + id))).andExpect(status().isOk());

        // Otro cliente no lo ve, y el cliente no cambia estados
        mvc.perform(con(usuario(Rol.CLIENTE, null, null), get("/api/garantias/reclamos/" + id)))
                .andExpect(status().isNotFound());
        mvc.perform(con(cliente, json(patch("/api/garantias/reclamos/" + id + "/estado"), "{\"estado\":\"EN_REVISION\"}")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinGarantiaVigente_oProductoQueNoEstaEnLaFactura_400() throws Exception {
        mvc.perform(con(vendedor, json(post("/api/garantias/reclamos"), reclamo(cable.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("venció")));

        Producto otro = producto(celular.getIdEmpresa(), celular.getIdCategoria(), "1000");
        mvc.perform(con(vendedor, json(post("/api/garantias/reclamos"), reclamo(otro.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unPasoNoPermitido_400() throws Exception {
        long id = registrar();

        mvc.perform(con(vendedor, json(patch("/api/garantias/reclamos/" + id + "/estado"), "{\"estado\":\"ENTREGADO\"}")))
                .andExpect(status().isBadRequest());
        mvc.perform(con(vendedor, json(patch("/api/garantias/reclamos/" + id + "/estado"), "{\"estado\":\"INVENTADO\"}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aislamiento_otraEmpresaNoRegistraNiVeNiCambiaEstosReclamos() throws Exception {
        long id = registrar();
        Long otra = empresaActiva().getId();
        Usuario vendedorAjeno = usuario(Rol.VENDEDOR, otra, almacen(otra).getId());

        mvc.perform(con(vendedorAjeno, json(post("/api/garantias/reclamos"), reclamo(celular.getId()))))
                .andExpect(status().isNotFound());
        mvc.perform(con(vendedorAjeno, get("/api/garantias/reclamos"))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(con(vendedorAjeno, get("/api/garantias/reclamos/" + id))).andExpect(status().isNotFound());
        mvc.perform(con(vendedorAjeno, json(patch("/api/garantias/reclamos/" + id + "/estado"), "{\"estado\":\"EN_REVISION\"}")))
                .andExpect(status().isNotFound());
    }

    @Test
    void camposObligatorios_400() throws Exception {
        mvc.perform(con(vendedor, json(post("/api/garantias/reclamos"), "{\"motivo\":\"\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.idVenta").exists())
                .andExpect(jsonPath("$.campos.motivo").exists());
    }
}
