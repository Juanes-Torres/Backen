package com.kairos.Kairos_backend.integracion;

import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.CategoriaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.TokenPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Categoria;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base de las pruebas de integración: levanta el backend completo (controllers, seguridad,
 * servicios y PostgreSQL) y le hace peticiones HTTP de mentira con MockMvc.
 *
 * @Transactional: cada prueba se DESHACE al terminar (rollback). kairos_db queda igual que antes.
 * El correo va apagado para no enviar mensajes reales durante las pruebas.
 */
@SpringBootTest(properties = "kairos.mail.enabled=false")
@AutoConfigureMockMvc
@Transactional
public abstract class PruebaIntegracion {

    protected static final String PASSWORD = "Clave123*";

    private static final AtomicInteger contador = new AtomicInteger();

    @Autowired protected MockMvc mvc;
    @Autowired protected TokenPort tokens;
    @Autowired protected PasswordEncoderPort passwords;
    @Autowired protected UsuarioRepositoryPort usuarios;
    @Autowired protected EmpresaRepositoryPort empresas;
    @Autowired protected AlmacenRepositoryPort almacenes;
    @Autowired protected CategoriaRepositoryPort categorias;
    @Autowired protected ProductoRepositoryPort productos;
    @Autowired protected InventarioRepositoryPort inventarios;

    /** Texto distinto en cada llamada, para no chocar con datos que ya existen (emails, NIT, nombres). */
    protected static String unico(String base) {
        return base + "-" + System.nanoTime() % 1_000_000 + "-" + contador.incrementAndGet();
    }

    protected Empresa empresaActiva() {
        Empresa empresa = Empresa.nueva(unico("Empresa"), unico("NIT"), unico("e") + "@kairos.com", "3000000000");
        empresa.aprobar();
        return empresas.guardar(empresa);
    }

    protected Usuario usuario(Rol rol, Long idEmpresa, Long idAlmacen) {
        String hash = passwords.cifrar(PASSWORD);
        return usuarios.guardar(Usuario.nuevo("Usuario " + rol, unico(rol.name().toLowerCase()) + "@kairos.com",
                hash, rol, "3001112233", idAlmacen, idEmpresa));
    }

    protected Almacen almacen(Long idEmpresa) {
        return almacenes.guardar(Almacen.nuevo(unico("Sede"), "Bogotá", "Calle 1 # 2-3", idEmpresa));
    }

    protected Categoria categoria() {
        return categorias.guardar(Categoria.nueva(unico("Categoria")));
    }

    protected Producto producto(Long idEmpresa, Long idCategoria, String precio) {
        return productos.guardar(Producto.nuevo(unico("Producto"), "De prueba", new BigDecimal(precio),
                "Marca", idCategoria, idEmpresa));
    }

    protected Inventario inventario(Long idProducto, Long idAlmacen, int cantidad, int minimo) {
        return inventarios.guardar(Inventario.nuevo(idProducto, idAlmacen, cantidad, minimo));
    }

    /** Agrega el token JWT del usuario a la petición. */
    protected MockHttpServletRequestBuilder con(Usuario usuario, MockHttpServletRequestBuilder peticion) {
        return peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.generarToken(usuario));
    }

    /** Lee un id de la respuesta JSON, por ejemplo idDe(respuesta, "$.empresa.id"). */
    protected static long idDe(String respuestaJson, String ruta) {
        return ((Number) JsonPath.read(respuestaJson, ruta)).longValue();
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }
}
