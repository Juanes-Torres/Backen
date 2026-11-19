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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Panel de inicio por rol (SCRUM-106) y catálogo con empresa y disponibilidad (SCRUM-107). */
class PanelYCatalogoIntegracionTest extends PruebaIntegracion {

    private Empresa empresa;
    private Almacen sede;
    private Usuario admin;
    private Usuario vendedor;
    private Usuario otroVendedor;
    private Usuario cliente;
    private Producto celular;
    private Producto cargador;

    @BeforeEach
    void preparar() {
        empresa = empresaActiva();
        sede = almacenes.guardar(Almacen.nuevo(unico("Centro"), "Bogotá", "Cra 7", empresa.getId()));
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());
        otroVendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());
        cliente = usuario(Rol.CLIENTE, null, null);
        Long categoria = categoria().getId();
        celular = productos.guardar(Producto.nuevo(unico("Galaxy"), null, new BigDecimal("1000000"), null,
                categoria, empresa.getId(), 12));
        cargador = productos.guardar(Producto.nuevo(unico("Cargador"), null, new BigDecimal("50000"), null,
                categoria, empresa.getId(), 6));
        inventario(celular.getId(), sede.getId(), 10, 2);
        inventario(cargador.getId(), sede.getId(), 3, 5);     // queda bajo el mínimo
    }

    private long vender(Usuario quien, Producto producto, int cantidad, String emailCliente) throws Exception {
        String cuerpo = "{" + (emailCliente == null ? "" : "\"emailCliente\":\"" + emailCliente + "\",")
                + "\"items\":[{\"idProducto\":" + producto.getId() + ",\"cantidad\":" + cantidad + "}]}";
        return idDe(mvc.perform(con(quien, json(post("/api/ventas"), cuerpo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
    }

    // ---------- Panel de inicio ----------

    @Test
    void elAdminVeTodaSuEmpresa_conGraficaYMasVendidos() throws Exception {
        long idVenta = vender(vendedor, celular, 2, cliente.getEmail());
        vender(otroVendedor, celular, 1, null);
        vender(vendedor, cargador, 1, null);
        mvc.perform(con(vendedor, json(post("/api/garantias/reclamos"),
                        "{\"idVenta\":" + idVenta + ",\"idProducto\":" + celular.getId() + ",\"motivo\":\"No enciende\"}")))
                .andExpect(status().isCreated());

        mvc.perform(con(admin, get("/api/panel")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.ventas.hoy.cantidad").value(3))
                .andExpect(jsonPath("$.ventas.hoy.total").value(3050000.0))
                .andExpect(jsonPath("$.ventas.ultimos7Dias.cantidad").value(3))
                .andExpect(jsonPath("$.ventas.mes.cantidad").value(3))
                .andExpect(jsonPath("$.ventas.porDia.length()").value(7))
                .andExpect(jsonPath("$.ventas.porDia[6].dia").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.ventas.porDia[6].cantidad").value(3))
                .andExpect(jsonPath("$.ventas.porDia[0].cantidad").value(0))
                .andExpect(jsonPath("$.ventas.masVendidosDelMes[0].producto").value(celular.getNombre()))
                .andExpect(jsonPath("$.ventas.masVendidosDelMes[0].unidades").value(3))
                .andExpect(jsonPath("$.ventas.masVendidosDelMes[1].producto").value(cargador.getNombre()))
                .andExpect(jsonPath("$.productosBajoStock").value(1))
                .andExpect(jsonPath("$.reclamosAbiertos").value(1))
                .andExpect(jsonPath("$.empresasPorEstado").doesNotExist());
    }

    @Test
    void elVendedorVeSoloLoSuyo() throws Exception {
        vender(vendedor, celular, 2, null);
        vender(otroVendedor, celular, 5, null);

        mvc.perform(con(vendedor, get("/api/panel")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andExpect(jsonPath("$.ventas.hoy.cantidad").value(1))
                .andExpect(jsonPath("$.ventas.masVendidosDelMes[0].unidades").value(2))
                .andExpect(jsonPath("$.productosBajoStock").value(1));
    }

    @Test
    void elSuperadminVeLasEmpresasPorEstado_yElClienteNoTienePanel() throws Exception {
        Usuario superadmin = usuario(Rol.SUPERADMIN, null, null);
        mvc.perform(con(superadmin, get("/api/panel")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventas").doesNotExist())
                .andExpect(jsonPath("$.empresasPorEstado.ACTIVA").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.empresasPorEstado.PENDIENTE").exists())
                .andExpect(jsonPath("$.empresasPorEstado.SUSPENDIDA").exists());

        mvc.perform(con(cliente, get("/api/panel"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/panel")).andExpect(status().isUnauthorized());
    }

    // ---------- Catálogo ----------

    @Test
    void elCatalogoDiceQuienVende_ySiHayUnidadesEnSedesActivas() throws Exception {
        Long categoria = categoria().getId();
        Producto agotado = productos.guardar(Producto.nuevo(unico("Agotado"), null, BigDecimal.ONE, null,
                categoria, empresa.getId(), 0));
        inventario(agotado.getId(), sede.getId(), 0, 1);
        Producto soloEnSedeCerrada = productos.guardar(Producto.nuevo(unico("Cerrada"), null, BigDecimal.ONE, null,
                categoria, empresa.getId(), 0));
        Almacen cerrada = almacen(empresa.getId());
        inventario(soloEnSedeCerrada.getId(), cerrada.getId(), 9, 1);
        cerrada.desactivar();
        almacenes.guardar(cerrada);

        String ruta = "$[?(@.id == %d)].%s";
        mvc.perform(get("/api/productos").param("idEmpresa", empresa.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(ruta.formatted(celular.getId(), "disponible")).value(contains(true)))
                .andExpect(jsonPath(ruta.formatted(celular.getId(), "empresa")).value(contains(empresa.getNombre())))
                .andExpect(jsonPath(ruta.formatted(agotado.getId(), "disponible")).value(contains(false)))
                .andExpect(jsonPath(ruta.formatted(soloEnSedeCerrada.getId(), "disponible")).value(contains(false)));

        mvc.perform(get("/api/productos/{id}", celular.getId()))
                .andExpect(jsonPath("$.empresa").value(empresa.getNombre()))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.logoEmpresaUrl").doesNotExist());
    }
}
