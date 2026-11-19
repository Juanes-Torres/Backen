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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La factura completa: quién vende, dónde, a quién, qué y hasta cuándo tiene garantía. */
class FacturaIntegracionTest extends PruebaIntegracion {

    private Empresa empresa;
    private Almacen sede;
    private Usuario admin;
    private Usuario vendedor;
    private Usuario cliente;
    private Producto celular;
    private Producto cable;

    @BeforeEach
    void preparar() {
        empresa = empresaActiva();
        sede = almacenes.guardar(Almacen.nuevo(unico("Sede Centro"), "Bogotá", "Cra 7 # 12-45", empresa.getId()));
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());
        cliente = usuario(Rol.CLIENTE, null, null);
        cliente.asignarCedula(String.valueOf(System.nanoTime()).substring(0, 10));
        cliente = usuarios.guardar(cliente);

        Long categoria = categoria().getId();
        celular = productos.guardar(Producto.nuevo(unico("Galaxy A55"), null, new BigDecimal("1500000"), null,
                categoria, empresa.getId(), 12));
        cable = productos.guardar(Producto.nuevo(unico("Cable USB-C"), null, new BigDecimal("20000"), null,
                categoria, empresa.getId(), 0));   // sin garantía
        inventario(celular.getId(), sede.getId(), 5, 1);
        inventario(cable.getId(), sede.getId(), 5, 1);
    }

    private long vender(String emailCliente) throws Exception {
        String cuerpo = "{" + (emailCliente == null ? "" : "\"emailCliente\":\"" + emailCliente + "\",")
                + "\"items\":[{\"idProducto\":" + celular.getId() + ",\"cantidad\":1},"
                + "{\"idProducto\":" + cable.getId() + ",\"cantidad\":2}]}";
        String respuesta = mvc.perform(con(vendedor, json(post("/api/ventas"), cuerpo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return idDe(respuesta, "$.id");
    }

    @Test
    void laFacturaTraeEmpresaSedeVendedorClienteYGarantiaPorProducto() throws Exception {
        long id = vender(cliente.getEmail());
        String hasta = LocalDate.now().plusMonths(12).toString();

        mvc.perform(con(admin, get("/api/ventas/{id}/factura", id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("F-")))
                .andExpect(jsonPath("$.empresa.nombre").value(empresa.getNombre()))
                .andExpect(jsonPath("$.empresa.nit").value(empresa.getNit()))
                .andExpect(jsonPath("$.sede.nombre").value(sede.getNombre()))
                .andExpect(jsonPath("$.sede.ciudad").value("Bogotá"))
                .andExpect(jsonPath("$.sede.direccion").value("Cra 7 # 12-45"))
                .andExpect(jsonPath("$.vendedor").value(vendedor.getNombre()))
                .andExpect(jsonPath("$.cliente.nombre").value(cliente.getNombre()))
                .andExpect(jsonPath("$.cliente.cedula").value(cliente.getCedula()))
                .andExpect(jsonPath("$.lineas.length()").value(2))
                .andExpect(jsonPath("$.lineas[?(@.producto == '" + celular.getNombre() + "')].garantiaHasta").value(
                        org.hamcrest.Matchers.contains(org.hamcrest.Matchers.lessThanOrEqualTo(hasta))))
                .andExpect(jsonPath("$.lineas[?(@.producto == '" + cable.getNombre() + "')].mesesGarantia").value(
                        org.hamcrest.Matchers.contains(0)))
                .andExpect(jsonPath("$.lineas[?(@.producto == '" + cable.getNombre() + "')].garantiaHasta").value(
                        org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue())))
                .andExpect(jsonPath("$.total").value(1540000.0))
                // Del cliente solo nombre y cédula: nunca su correo
                .andExpect(content().string(not(containsString(cliente.getEmail()))));
    }

    @Test
    void ventaDeMostrador_sinCliente() throws Exception {
        long id = vender(null);
        mvc.perform(con(vendedor, get("/api/ventas/{id}/factura", id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").doesNotExist());
    }

    @Test
    void permisos_iguales_alDetalleDeLaVenta() throws Exception {
        long id = vender(cliente.getEmail());
        Long otra = empresaActiva().getId();
        Usuario adminAjeno = usuario(Rol.ADMINISTRADOR, otra, null);
        Usuario otroCliente = usuario(Rol.CLIENTE, null, null);

        mvc.perform(con(cliente, get("/api/ventas/{id}/factura", id))).andExpect(status().isOk());
        mvc.perform(con(adminAjeno, get("/api/ventas/{id}/factura", id))).andExpect(status().isNotFound());
        mvc.perform(con(otroCliente, get("/api/ventas/{id}/factura", id))).andExpect(status().isNotFound());
        mvc.perform(get("/api/ventas/{id}/factura", id)).andExpect(status().isUnauthorized());
        mvc.perform(con(admin, get("/api/ventas/{id}/factura", 999999999L))).andExpect(status().isNotFound());
    }
}
