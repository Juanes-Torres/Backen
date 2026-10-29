package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Categoria;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F2 y F3 de punta a punta: categorías, productos, almacenes e inventario, con sus reglas
 * y el aislamiento entre empresas (SCRUM-59).
 */
class CatalogoEInventarioIntegracionTest extends PruebaIntegracion {

    private Empresa empresa;
    private Usuario admin;
    private Usuario vendedor;
    private Almacen sede;
    private Categoria categoria;

    @BeforeEach
    void preparar() {
        empresa = empresaActiva();
        sede = almacen(empresa.getId());
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());
        categoria = categoria();
    }

    // ---------- Categorías ----------

    @Test
    void categorias_soloElAdminLasCreaYRenombra() throws Exception {
        String nombre = unico("Tablets");

        mvc.perform(json(post("/api/categorias"), "{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isUnauthorized());

        String respuesta = mvc.perform(con(admin, json(post("/api/categorias"), "{\"nombre\":\"" + nombre + "\"}")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = idDe(respuesta, "$.id");

        mvc.perform(con(admin, json(put("/api/categorias/" + id), "{\"nombre\":\"" + nombre + " Pro\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombre + " Pro"));

        mvc.perform(con(admin, json(post("/api/categorias"), "{\"nombre\":\"" + nombre + " Pro\"}")))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    // ---------- Productos ----------

    @Test
    void productos_elAdminLosCreaEditaYBorra() throws Exception {
        String cuerpo = "{\"nombre\":\"Galaxy Prueba\",\"descripcion\":\"Celular\",\"precio\":1500000,\"marca\":\"Samsung\",\"idCategoria\":" + categoria.getId() + "}";

        String respuesta = mvc.perform(con(admin, json(post("/api/productos"), cuerpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Galaxy Prueba"))
                .andReturn().getResponse().getContentAsString();
        long id = idDe(respuesta, "$.id");

        mvc.perform(get("/api/productos/" + id)).andExpect(status().isOk());

        mvc.perform(con(admin, json(put("/api/productos/" + id), cuerpo.replace("1500000", "1400000"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(1400000));

        mvc.perform(con(admin, delete("/api/productos/" + id))).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void productos_reglas_precioNegativoCategoriaInexistenteYConInventario_400o404() throws Exception {
        mvc.perform(con(admin, json(post("/api/productos"),
                        "{\"nombre\":\"Malo\",\"precio\":-1,\"idCategoria\":" + categoria.getId() + "}")))
                .andExpect(status().isBadRequest());

        mvc.perform(con(admin, json(post("/api/productos"),
                        "{\"nombre\":\"Sin categoria\",\"precio\":10,\"idCategoria\":999999}")))
                .andExpect(status().is4xxClientError());

        Producto conStock = producto(empresa.getId(), categoria.getId(), "100000");
        inventario(conStock.getId(), sede.getId(), 3, 1);
        mvc.perform(con(admin, delete("/api/productos/" + conStock.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void catalogoPublico_filtraPorCategoriaYNombre_ySoloMuestraEmpresasActivas() throws Exception {
        Producto propio = producto(empresa.getId(), categoria.getId(), "50000");

        Empresa pendiente = empresas.guardar(Empresa.nueva(unico("Pendiente"), unico("NIT"), unico("p") + "@kairos.com", "300"));
        Producto oculto = producto(pendiente.getId(), categoria.getId(), "50000");

        mvc.perform(get("/api/productos").param("idCategoria", categoria.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(propio.getId().intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(oculto.getId().intValue()))));

        mvc.perform(get("/api/productos").param("nombre", propio.getNombre().substring(0, 12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(propio.getId().intValue())));

        mvc.perform(get("/api/productos/" + oculto.getId())).andExpect(status().isNotFound());
    }

    @Test
    void aislamiento_elAdminNoModificaProductosDeOtraEmpresa_404() throws Exception {
        Producto ajeno = producto(empresaActiva().getId(), categoria.getId(), "10000");

        mvc.perform(con(admin, json(put("/api/productos/" + ajeno.getId()),
                        "{\"nombre\":\"Robado\",\"precio\":1,\"idCategoria\":" + categoria.getId() + "}")))
                .andExpect(status().isNotFound());
        mvc.perform(con(admin, delete("/api/productos/" + ajeno.getId()))).andExpect(status().isNotFound());
    }

    @Test
    void unClienteNoPuedeCrearProductos_403() throws Exception {
        Usuario cliente = usuario(Rol.CLIENTE, null, null);

        mvc.perform(con(cliente, json(post("/api/productos"),
                        "{\"nombre\":\"X\",\"precio\":1,\"idCategoria\":" + categoria.getId() + "}")))
                .andExpect(status().isForbidden());
    }

    // ---------- Almacenes ----------

    @Test
    void almacenes_elAdminLosCreaEditaDesactivaYActiva() throws Exception {
        String respuesta = mvc.perform(con(admin, json(post("/api/almacenes"),
                        "{\"nombre\":\"Sede Prueba\",\"ciudad\":\"Chía\",\"direccion\":\"Cra 1\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEmpresa").value(empresa.getId()))
                .andReturn().getResponse().getContentAsString();
        long id = idDe(respuesta, "$.id");

        mvc.perform(con(admin, json(put("/api/almacenes/" + id), "{\"nombre\":\"Sede Editada\",\"ciudad\":\"Cota\",\"direccion\":\"Cra 2\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sede Editada"));
        mvc.perform(con(admin, patch("/api/almacenes/" + id + "/desactivar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
        mvc.perform(con(admin, patch("/api/almacenes/" + id + "/activar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        mvc.perform(con(vendedor, get("/api/almacenes"))).andExpect(status().isOk());
        mvc.perform(con(vendedor, get("/api/almacenes/" + id))).andExpect(status().isOk());
    }

    @Test
    void aislamiento_elAdminNoVeAlmacenesDeOtraEmpresa_404() throws Exception {
        Almacen ajeno = almacen(empresaActiva().getId());

        mvc.perform(con(admin, get("/api/almacenes/" + ajeno.getId()))).andExpect(status().isNotFound());
        mvc.perform(con(admin, get("/api/almacenes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(ajeno.getId().intValue()))));
    }

    // ---------- Inventario ----------

    @Test
    void inventario_registrarConsultarAjustarYCambiarMinimo() throws Exception {
        Producto producto = producto(empresa.getId(), categoria.getId(), "80000");

        String respuesta = mvc.perform(con(admin, json(post("/api/inventario"),
                        "{\"idProducto\":" + producto.getId() + ",\"idAlmacen\":" + sede.getId() + ",\"cantidadInicial\":10,\"stockMinimo\":3}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.producto").value(producto.getNombre()))
                .andExpect(jsonPath("$.almacen").value(sede.getNombre()))
                .andReturn().getResponse().getContentAsString();
        long id = idDe(respuesta, "$.id");

        // Un solo registro por producto y almacén
        mvc.perform(con(admin, json(post("/api/inventario"),
                        "{\"idProducto\":" + producto.getId() + ",\"idAlmacen\":" + sede.getId() + ",\"cantidadInicial\":1,\"stockMinimo\":1}")))
                .andExpect(status().isConflict());

        mvc.perform(con(vendedor, get("/api/inventario").param("idAlmacen", sede.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));

        mvc.perform(con(vendedor, json(patch("/api/inventario/" + id + "/ajuste"), "{\"tipo\":\"SALIDA\",\"cantidad\":8}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDisponible").value(2));
        mvc.perform(con(vendedor, json(patch("/api/inventario/" + id + "/ajuste"), "{\"tipo\":\"ENTRADA\",\"cantidad\":1}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadDisponible").value(3));

        // El stock nunca queda negativo
        mvc.perform(con(vendedor, json(patch("/api/inventario/" + id + "/ajuste"), "{\"tipo\":\"SALIDA\",\"cantidad\":9999}")))
                .andExpect(status().isBadRequest());

        mvc.perform(con(admin, json(patch("/api/inventario/" + id + "/stock-minimo"), "{\"stockMinimo\":5}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockMinimo").value(5));

        mvc.perform(con(admin, get("/api/inventario/bajo-stock")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
    }

    @Test
    void inventario_noSeRegistraEnUnAlmacenInactivo() throws Exception {
        Producto producto = producto(empresa.getId(), categoria.getId(), "80000");
        sede.desactivar();
        almacenes.guardar(sede);

        mvc.perform(con(admin, json(post("/api/inventario"),
                        "{\"idProducto\":" + producto.getId() + ",\"idAlmacen\":" + sede.getId() + ",\"cantidadInicial\":1,\"stockMinimo\":1}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aislamiento_noSeAjustaInventarioDeOtraEmpresa_404() throws Exception {
        Long otra = empresaActiva().getId();
        Inventario ajeno = inventario(producto(otra, categoria.getId(), "1000").getId(), almacen(otra).getId(), 5, 1);

        mvc.perform(con(admin, json(patch("/api/inventario/" + ajeno.getId() + "/ajuste"), "{\"tipo\":\"SALIDA\",\"cantidad\":1}")))
                .andExpect(status().isNotFound());
        mvc.perform(con(admin, json(patch("/api/inventario/" + ajeno.getId() + "/stock-minimo"), "{\"stockMinimo\":1}")))
                .andExpect(status().isNotFound());
    }
}
