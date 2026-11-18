package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Consultar garantía por cédula o factura (SCRUM-74) de punta a punta:
 * un cliente con cédula compra un televisor (24 meses) y un cable sin garantía (0 meses).
 */
class GarantiasIntegracionTest extends PruebaIntegracion {

    private Usuario vendedor;
    private Usuario cliente;
    private Producto televisor;
    private String factura;

    @BeforeEach
    void preparar() throws Exception {
        Empresa empresa = empresaActiva();
        Almacen sede = almacen(empresa.getId());
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());

        cliente = usuario(Rol.CLIENTE, null, null);
        cliente.asignarCedula(String.valueOf(System.nanoTime()).substring(0, 10));
        cliente = usuarios.guardar(cliente);

        Long categoria = categoria().getId();
        televisor = productos.guardar(Producto.nuevo(unico("TV"), null, new BigDecimal("2000000"), null, categoria,
                empresa.getId(), 24));
        Producto cable = productos.guardar(Producto.nuevo(unico("Cable"), null, new BigDecimal("20000"), null, categoria,
                empresa.getId(), 0));
        inventario(televisor.getId(), sede.getId(), 5, 1);
        inventario(cable.getId(), sede.getId(), 5, 1);

        String respuesta = mvc.perform(con(vendedor, json(post("/api/ventas"),
                        "{\"emailCliente\":\"" + cliente.getEmail() + "\",\"items\":["
                                + "{\"idProducto\":" + televisor.getId() + ",\"cantidad\":1},"
                                + "{\"idProducto\":" + cable.getId() + ",\"cantidad\":2}]}")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        factura = com.jayway.jsonpath.JsonPath.read(respuesta, "$.numeroFactura");
    }

    @Test
    void porCedula_cadaProductoConSuFinDeGarantiaYSiEstaVigente() throws Exception {
        String finTv = LocalDate.now().plusMonths(24).toString();

        mvc.perform(con(vendedor, get("/api/garantias").param("cedula", cliente.getCedula())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.idProducto == " + televisor.getId() + ")].vigente").value(true))
                .andExpect(jsonPath("$[?(@.idProducto == " + televisor.getId() + ")].fechaFinGarantia").value(finTv))
                .andExpect(jsonPath("$[?(@.mesesGarantia == 0)].vigente").value(false))
                .andExpect(jsonPath("$[0].numeroFactura").value(factura))
                .andExpect(jsonPath("$[0].almacen").exists());
    }

    @Test
    void porFactura_aunqueSeEscribaEnMinusculas() throws Exception {
        mvc.perform(con(vendedor, get("/api/garantias").param("factura", factura.toLowerCase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cedulaCliente").value(cliente.getCedula()));
    }

    @Test
    void aislamiento_otraEmpresaNoVeEstasCompras() throws Exception {
        Long otra = empresaActiva().getId();
        Usuario vendedorAjeno = usuario(Rol.VENDEDOR, otra, almacen(otra).getId());

        mvc.perform(con(vendedorAjeno, get("/api/garantias").param("cedula", cliente.getCedula())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void sinCedulaNiFactura_400_yUnClienteNoConsulta_403() throws Exception {
        mvc.perform(con(vendedor, get("/api/garantias"))).andExpect(status().isBadRequest());
        mvc.perform(con(cliente, get("/api/garantias").param("cedula", cliente.getCedula())))
                .andExpect(status().isForbidden());
    }
}
