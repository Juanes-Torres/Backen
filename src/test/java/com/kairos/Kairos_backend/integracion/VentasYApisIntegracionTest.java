package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort;
import com.kairos.Kairos_backend.application.port.out.GeneradorQrPort;
import com.kairos.Kairos_backend.domain.exception.ServicioExternoNoDisponibleException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ventas, QR de la factura y código de barras de punta a punta.
 * Las APIs externas se reemplazan por clases falsas (sin internet): así se prueba
 * también qué pasa cuando la API está caída (503).
 */
class VentasYApisIntegracionTest extends PruebaIntegracion {

    static final String CODIGO_CAIDO = "11111111";
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G'};

    @TestConfiguration
    static class ApisFalsas {

        @Bean
        @Primary
        GeneradorQrPort qrFalso() {
            return contenido -> PNG;
        }

        @Bean
        @Primary
        CatalogoExternoPort catalogoFalso() {
            return codigo -> {
                if (codigo.valor().equals(CODIGO_CAIDO)) {
                    throw new ServicioExternoNoDisponibleException("API de prueba caída");
                }
                return codigo.valor().equals("190199098572")
                        ? Optional.of(new CatalogoExternoPort.DatosProductoExterno("AirPods 2", "Apple", "Audífonos", "Audio"))
                        : Optional.empty();
            };
        }
    }

    private Usuario admin;
    private Usuario vendedor;
    private Almacen sede;
    private Inventario stock;

    @BeforeEach
    void preparar() {
        Empresa empresa = empresaActiva();
        sede = almacen(empresa.getId());
        admin = usuario(Rol.ADMINISTRADOR, empresa.getId(), null);
        vendedor = usuario(Rol.VENDEDOR, empresa.getId(), sede.getId());
        Producto producto = producto(empresa.getId(), categoria().getId(), "129000");
        stock = inventario(producto.getId(), sede.getId(), 5, 1);
    }

    @Test
    void elVendedorRegistraUnaVenta_seDescuentaElStockYSeGeneraElQr() throws Exception {
        Usuario cliente = usuario(Rol.CLIENTE, null, null);

        String respuesta = mvc.perform(con(vendedor, json(post("/api/ventas"),
                        "{\"emailCliente\":\"" + cliente.getEmail() + "\",\"items\":[{\"idProducto\":" + stock.getIdProducto() + ",\"cantidad\":2}]}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFactura").value("F-000001"))
                .andExpect(jsonPath("$.total").value(258000))
                .andExpect(jsonPath("$.idCliente").value(cliente.getId()))
                .andReturn().getResponse().getContentAsString();
        long id = idDe(respuesta, "$.id");

        org.junit.jupiter.api.Assertions.assertEquals(3, inventarios.buscarPorId(stock.getId()).orElseThrow().getCantidadDisponible());

        mvc.perform(con(vendedor, get("/api/ventas/" + id + "/qr")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(PNG));

        // El cliente que compró también ve su QR; otro cliente no
        mvc.perform(con(cliente, get("/api/ventas/" + id + "/qr"))).andExpect(status().isOk());
        mvc.perform(con(usuario(Rol.CLIENTE, null, null), get("/api/ventas/" + id + "/qr"))).andExpect(status().isNotFound());
    }

    @Test
    void sinStockSuficiente_laVentaNoSeGuarda_400() throws Exception {
        mvc.perform(con(vendedor, json(post("/api/ventas"),
                        "{\"items\":[{\"idProducto\":" + stock.getIdProducto() + ",\"cantidad\":99}]}")))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertEquals(5, inventarios.buscarPorId(stock.getId()).orElseThrow().getCantidadDisponible());
    }

    @Test
    void elAdminVendeIndicandoLaSede_yUnaVentaSinProductosEs400() throws Exception {
        mvc.perform(con(admin, json(post("/api/ventas"),
                        "{\"idAlmacen\":" + sede.getId() + ",\"items\":[{\"idProducto\":" + stock.getIdProducto() + ",\"cantidad\":1}]}")))
                .andExpect(status().isCreated());

        mvc.perform(con(admin, json(post("/api/ventas"), "{\"idAlmacen\":" + sede.getId() + ",\"items\":[]}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.items").exists());
    }

    @Test
    void unClienteNoPuedeRegistrarVentas_403() throws Exception {
        mvc.perform(con(usuario(Rol.CLIENTE, null, null), json(post("/api/ventas"),
                        "{\"items\":[{\"idProducto\":" + stock.getIdProducto() + ",\"cantidad\":1}]}")))
                .andExpect(status().isForbidden());
    }

    @Test
    void codigoDeBarras_encontradoNoEncontradoInvalidoYApiCaida() throws Exception {
        mvc.perform(con(admin, get("/api/productos/codigo-barras/190199098572")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Apple"));

        mvc.perform(con(admin, get("/api/productos/codigo-barras/12345678"))).andExpect(status().isNotFound());
        mvc.perform(con(admin, get("/api/productos/codigo-barras/abc"))).andExpect(status().isBadRequest());
        mvc.perform(con(admin, get("/api/productos/codigo-barras/" + CODIGO_CAIDO)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        mvc.perform(get("/api/productos/codigo-barras/190199098572")).andExpect(status().isUnauthorized());
    }
}
