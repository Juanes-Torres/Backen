package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase.Item;
import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase.RegistrarVentaCommand;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.VentaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.EstadoAlmacen;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.domain.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Registrar una venta SIN Spring y SIN base de datos (puertos con clases falsas).
 */
class RegistrarVentaServiceTest {

    private static final Long EMPRESA = 1L;
    private static final Long OTRA_EMPRESA = 2L;
    private static final Long SEDE = 10L;
    private static final Long CELULAR = 100L;      // 1.500.000, hay 5 en la SEDE
    private static final Long AUDIFONOS = 200L;    //    50.000, hay 2 en la SEDE
    private static final Long PRODUCTO_AJENO = 300L;

    private final Solicitante vendedor = new Solicitante(7L, Rol.VENDEDOR, EMPRESA);
    private final Solicitante admin = new Solicitante(8L, Rol.ADMINISTRADOR, EMPRESA);

    private VentaRepositoryFalso ventas;
    private InventarioRepositoryFalso inventarios;
    private RegistrarVentaService servicio;

    @BeforeEach
    void preparar() {
        ventas = new VentaRepositoryFalso();
        inventarios = new InventarioRepositoryFalso();
        inventarios.agregar(1L, CELULAR, SEDE, 5);
        inventarios.agregar(2L, AUDIFONOS, SEDE, 2);

        ProductoRepositoryFalso productos = new ProductoRepositoryFalso();
        productos.agregar(CELULAR, "Celular", "1500000", EMPRESA);
        productos.agregar(AUDIFONOS, "Audífonos", "50000", EMPRESA);
        productos.agregar(PRODUCTO_AJENO, "De otra empresa", "10", OTRA_EMPRESA);

        AlmacenRepositoryFalso almacenes = new AlmacenRepositoryFalso();
        almacenes.agregar(new Almacen(SEDE, "Sede Centro", "Bogotá", null, EstadoAlmacen.ACTIVO, EMPRESA));
        almacenes.agregar(new Almacen(11L, "Sede Norte", "Bogotá", null, EstadoAlmacen.ACTIVO, EMPRESA));

        UsuarioRepositoryFalso usuarios = new UsuarioRepositoryFalso();
        usuarios.agregar(new Usuario(7L, "Vendedor", "vendedor@tecno.com", "hash", Rol.VENDEDOR,
                null, SEDE, EMPRESA, LocalDate.now(), true));
        usuarios.agregar(new Usuario(9L, "Ana", "ana@correo.com", "hash", Rol.CLIENTE,
                null, null, null, LocalDate.now(), true));

        servicio = new RegistrarVentaService(ventas, inventarios, productos, almacenes, usuarios);
    }

    @Test
    void unaVentaDescuentaElStockYCalculaElTotalConElPrecioDelProducto() {
        Venta venta = servicio.registrar(vendedor, comando(null, "ana@correo.com",
                new Item(CELULAR, 2), new Item(AUDIFONOS, 1)));

        assertEquals(new BigDecimal("3050000"), venta.getTotal());
        assertEquals("F-000001", venta.getNumeroFactura());
        assertEquals(SEDE, venta.getIdAlmacen());          // el vendedor vende en su sede
        assertEquals(9L, venta.getIdCliente());
        assertEquals(3, inventarios.cantidad(CELULAR));
        assertEquals(1, inventarios.cantidad(AUDIFONOS));
    }

    @Test
    void siUnProductoNoTieneStock_noSeGuardaNadaNiSeDescuentaNada() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(vendedor, comando(null, null,
                        new Item(CELULAR, 1), new Item(AUDIFONOS, 3))));

        assertTrue(error.getMessage().startsWith("Audífonos: Stock insuficiente"));
        assertTrue(ventas.datos.isEmpty());
        assertEquals(5, inventarios.cantidad(CELULAR));
        assertEquals(2, inventarios.cantidad(AUDIFONOS));
    }

    @Test
    void lasFacturasSonConsecutivasDentroDeLaEmpresa() {
        servicio.registrar(vendedor, comando(null, null, new Item(CELULAR, 1)));
        Venta segunda = servicio.registrar(vendedor, comando(null, null, new Item(CELULAR, 1)));

        assertEquals("F-000002", segunda.getNumeroFactura());
    }

    @Test
    void unVendedorNoPuedeVenderEnOtraSede() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(vendedor, comando(11L, null, new Item(CELULAR, 1))));
    }

    @Test
    void elAdministradorDebeIndicarLaSede() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(admin, comando(null, null, new Item(CELULAR, 1))));

        Venta venta = servicio.registrar(admin, comando(SEDE, null, new Item(CELULAR, 1)));
        assertNull(venta.getIdCliente());   // venta de mostrador, sin cliente
    }

    @Test
    void noSeVendenProductosDeOtraEmpresa() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrar(vendedor, comando(null, null, new Item(PRODUCTO_AJENO, 1))));
    }

    @Test
    void elClienteDebeExistir() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrar(vendedor, comando(null, "nadie@correo.com", new Item(CELULAR, 1))));
    }

    private static RegistrarVentaCommand comando(Long idAlmacen, String emailCliente, Item... items) {
        return new RegistrarVentaCommand(idAlmacen, emailCliente, List.of(items));
    }

    // ===== Clases falsas (implementaciones simples de los puertos) =====

    private static class VentaRepositoryFalso implements VentaRepositoryPort {
        final List<Venta> datos = new ArrayList<>();

        @Override
        public Venta guardar(Venta v) {
            Venta conId = new Venta((long) datos.size() + 1, v.getNumeroFactura(), v.getIdEmpresa(), v.getIdAlmacen(),
                    v.getIdVendedor(), v.getIdCliente(), v.getFecha(), v.getEstado(), v.getDetalles());
            datos.add(conId);
            return conId;
        }

        @Override
        public Optional<Venta> buscarPorId(Long id) {
            return datos.stream().filter(v -> v.getId().equals(id)).findFirst();
        }

        @Override
        public long contarPorEmpresa(Long idEmpresa) {
            return datos.stream().filter(v -> v.getIdEmpresa().equals(idEmpresa)).count();
        }
    }

    /** Guarda COPIAS, como una base de datos: lo que no se guarda, no cambia. */
    private static class InventarioRepositoryFalso implements InventarioRepositoryPort {
        private final Map<Long, Inventario> datos = new HashMap<>();

        void agregar(Long id, Long idProducto, Long idAlmacen, int cantidad) {
            datos.put(id, new Inventario(id, idProducto, idAlmacen, cantidad, 0));
        }

        int cantidad(Long idProducto) {
            return datos.values().stream().filter(i -> i.getIdProducto().equals(idProducto))
                    .findFirst().orElseThrow().getCantidadDisponible();
        }

        private static Inventario copia(Inventario i) {
            return new Inventario(i.getId(), i.getIdProducto(), i.getIdAlmacen(), i.getCantidadDisponible(), i.getStockMinimo());
        }

        @Override
        public Inventario guardar(Inventario inventario) {
            datos.put(inventario.getId(), copia(inventario));
            return inventario;
        }

        @Override
        public Optional<Inventario> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id)).map(InventarioRepositoryFalso::copia);
        }

        @Override
        public Optional<Inventario> buscarParaDescontar(Long idProducto, Long idAlmacen) {
            return datos.values().stream()
                    .filter(i -> i.getIdProducto().equals(idProducto) && i.getIdAlmacen().equals(idAlmacen))
                    .findFirst().map(InventarioRepositoryFalso::copia);
        }

        @Override
        public boolean existePorProductoYAlmacen(Long idProducto, Long idAlmacen) {
            return buscarParaDescontar(idProducto, idAlmacen).isPresent();
        }

        @Override
        public boolean existePorProducto(Long idProducto) {
            return datos.values().stream().anyMatch(i -> i.getIdProducto().equals(idProducto));
        }

        @Override
        public List<InventarioDetalle> listarDetalle(Long idEmpresa, Long idAlmacen) {
            return List.of();
        }

        @Override
        public List<InventarioDetalle> listarBajoStock(Long idEmpresa) {
            return List.of();
        }

        @Override
        public Optional<InventarioDetalle> buscarDetallePorId(Long id) {
            return Optional.empty();
        }
    }

    private static class ProductoRepositoryFalso implements ProductoRepositoryPort {
        private final List<Producto> datos = new ArrayList<>();

        void agregar(Long id, String nombre, String precio, Long idEmpresa) {
            datos.add(new Producto(id, nombre, null, new BigDecimal(precio), null, 1L, idEmpresa));
        }

        @Override
        public Producto guardar(Producto producto) {
            datos.add(producto);
            return producto;
        }

        @Override
        public Optional<Producto> buscarPorId(Long id) {
            return datos.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Producto> buscarEnCatalogo(Long idEmpresa, Long idCategoria, String nombre) {
            return List.copyOf(datos);
        }

        @Override
        public void eliminar(Long id) {
            datos.removeIf(p -> p.getId().equals(id));
        }
    }

    private static class AlmacenRepositoryFalso implements AlmacenRepositoryPort {
        private final List<Almacen> datos = new ArrayList<>();

        void agregar(Almacen almacen) {
            datos.add(almacen);
        }

        @Override
        public Almacen guardar(Almacen almacen) {
            datos.add(almacen);
            return almacen;
        }

        @Override
        public Optional<Almacen> buscarPorId(Long id) {
            return datos.stream().filter(a -> a.getId().equals(id)).findFirst();
        }

        @Override
        public List<Almacen> listarPorEmpresa(Long idEmpresa) {
            return datos.stream().filter(a -> a.perteneceA(idEmpresa)).toList();
        }
    }

    private static class UsuarioRepositoryFalso implements UsuarioRepositoryPort {
        private final List<Usuario> datos = new ArrayList<>();

        void agregar(Usuario usuario) {
            datos.add(usuario);
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            datos.add(usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return datos.stream().filter(u -> u.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            return datos.stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        @Override
        public boolean existePorEmail(String email) {
            return buscarPorEmail(email).isPresent();
        }

        @Override
        public List<Usuario> listarPorEmpresa(Long idEmpresa) {
            return datos.stream().filter(u -> u.perteneceA(idEmpresa)).toList();
        }
    }
}
