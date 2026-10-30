package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Consultar ventas por sede y por vendedor (SCRUM-48) de punta a punta:
 * 3 ventas en 2 sedes, hechas por 2 vendedores y el admin; cada rol ve solo lo que le toca.
 */
class ConsultaVentasIntegracionTest extends PruebaIntegracion {

    private Usuario admin;
    private Usuario vendedorCentro;
    private Usuario vendedorNorte;
    private Usuario cliente;
    private Almacen centro;
    private Almacen norte;
    private Producto producto;
    private long ventaConCliente;

    @BeforeEach
    void preparar() throws Exception {
        Empresa empresa = empresaActiva();
        centro = almacen(empresa.getId());
        norte = almacen(empresa.getId());
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedorCentro = usuario(Rol.VENDEDOR, empresa.getId(), centro.getId());
        vendedorNorte = usuario(Rol.VENDEDOR, empresa.getId(), norte.getId());
        cliente = usuario(Rol.CLIENTE, null, null);
        producto = producto(empresa.getId(), categoria().getId(), "100000");
        inventario(producto.getId(), centro.getId(), 10, 1);
        inventario(producto.getId(), norte.getId(), 10, 1);

        ventaConCliente = vender(vendedorCentro, "\"emailCliente\":\"" + cliente.getEmail() + "\",", 2);
        vender(vendedorNorte, "", 1);
        vender(admin, "\"idAlmacen\":" + norte.getId() + ",", 3);
    }

    private long vender(Usuario quien, String extra, int cantidad) throws Exception {
        String respuesta = mvc.perform(con(quien, json(post("/api/ventas"),
                        "{" + extra + "\"items\":[{\"idProducto\":" + producto.getId() + ",\"cantidad\":" + cantidad + "}]}")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idDe(respuesta, "$.id");
    }

    @Test
    void elAdminVeTodasLasVentasDeSuEmpresa_conLosNombres() throws Exception {
        mvc.perform(con(admin, get("/api/ventas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.id == " + ventaConCliente + ")].almacen").value(centro.getNombre()))
                .andExpect(jsonPath("$[?(@.id == " + ventaConCliente + ")].vendedor").value(vendedorCentro.getNombre()))
                .andExpect(jsonPath("$[?(@.id == " + ventaConCliente + ")].cliente").value(cliente.getNombre()));
    }

    @Test
    void elAdminFiltraPorSedeYPorVendedor() throws Exception {
        mvc.perform(con(admin, get("/api/ventas").param("idAlmacen", norte.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].idAlmacen", everyItem(is(norte.getId().intValue()))));

        mvc.perform(con(admin, get("/api/ventas").param("idVendedor", vendedorCentro.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(ventaConCliente));
    }

    @Test
    void elVendedorSoloVeSusVentas_aunquePidaLasDeOtro() throws Exception {
        mvc.perform(con(vendedorCentro, get("/api/ventas").param("idVendedor", vendedorNorte.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idVendedor").value(vendedorCentro.getId()));
    }

    @Test
    void elClienteVeSusCompras() throws Exception {
        mvc.perform(con(cliente, get("/api/ventas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroFactura").exists());
    }

    @Test
    void elDetalleTraeLosProductosConSuNombre() throws Exception {
        mvc.perform(con(admin, get("/api/ventas/" + ventaConCliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venta.total").value(200000))
                .andExpect(jsonPath("$.lineas", hasSize(1)))
                .andExpect(jsonPath("$.lineas[0].producto").value(producto.getNombre()))
                .andExpect(jsonPath("$.lineas[0].cantidad").value(2));

        mvc.perform(con(cliente, get("/api/ventas/" + ventaConCliente))).andExpect(status().isOk());
    }

    @Test
    void aislamiento_otraEmpresaNoVeEstasVentas() throws Exception {
        Usuario adminAjeno = usuario(Rol.ADMINISTRADOR, empresaActiva().getId(), null);

        mvc.perform(con(adminAjeno, get("/api/ventas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(con(adminAjeno, get("/api/ventas/" + ventaConCliente))).andExpect(status().isNotFound());
    }

    @Test
    void filtroPorFechas() throws Exception {
        String hoy = LocalDate.now().toString();
        String manana = LocalDate.now().plusDays(1).toString();

        mvc.perform(con(admin, get("/api/ventas").param("desde", hoy).param("hasta", hoy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(con(admin, get("/api/ventas").param("desde", manana).param("hasta", manana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(con(admin, get("/api/ventas").param("desde", manana).param("hasta", hoy)))
                .andExpect(status().isBadRequest());
        mvc.perform(con(admin, get("/api/ventas").param("desde", "ayer")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elSuperadminNoConsultaVentas_403() throws Exception {
        mvc.perform(con(usuario(Rol.SUPERADMIN, null, null), get("/api/ventas"))).andExpect(status().isForbidden());
    }
}
