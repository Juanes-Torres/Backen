package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Catálogo público: solo se ven los productos de empresas ACTIVAS (sin Spring ni base de datos).
 */
class ProductoServiceTest {

    private static final Long ACTIVA = 1L;
    private static final Long SUSPENDIDA = 2L;

    private ProductoService servicio;

    @BeforeEach
    void preparar() {
        EmpresaRepositoryFalso empresas = new EmpresaRepositoryFalso();
        empresas.agregar(ACTIVA, EstadoEmpresa.ACTIVA);
        empresas.agregar(SUSPENDIDA, EstadoEmpresa.SUSPENDIDA);

        ProductoRepositoryFalso productos = new ProductoRepositoryFalso(empresas);
        productos.agregar(10L, "Galaxy A55", ACTIVA);
        productos.agregar(20L, "Moto G", SUSPENDIDA);

        // Categorías e inventario no se usan al consultar el catálogo
        servicio = new ProductoService(productos, null, null, empresas);
    }

    @Test
    void elCatalogoSinEmpresaMuestraSoloLasEmpresasActivas() {
        List<Producto> catalogo = servicio.buscar(null, null, null);

        assertEquals(1, catalogo.size());
        assertEquals("Galaxy A55", catalogo.get(0).getNombre());
    }

    @Test
    void elCatalogoDeUnaEmpresaNoActivaRespondeNoEncontrada() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.buscar(SUSPENDIDA, null, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.buscar(99L, null, null));
    }

    @Test
    void unProductoDeUnaEmpresaSuspendidaNoSeMuestra() {
        assertEquals("Galaxy A55", servicio.obtener(10L).getNombre());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(20L));
    }

    // ===== Clases falsas =====

    private static class EmpresaRepositoryFalso implements EmpresaRepositoryPort {
        private final List<Empresa> datos = new ArrayList<>();

        void agregar(Long id, EstadoEmpresa estado) {
            datos.add(new Empresa(id, "Empresa " + id, "900-" + id, "e" + id + "@correo.com", null,
                    estado, LocalDate.now()));
        }

        @Override
        public Empresa guardar(Empresa empresa) {
            datos.add(empresa);
            return empresa;
        }

        @Override
        public Optional<Empresa> buscarPorId(Long id) {
            return datos.stream().filter(e -> e.getId().equals(id)).findFirst();
        }

        @Override
        public boolean existePorNit(String nit) {
            return buscarPorNit(nit).isPresent();
        }

        @Override
        public Optional<Empresa> buscarPorNit(String nit) {
            return datos.stream().filter(e -> e.getNit().equals(nit)).findFirst();
        }

        @Override
        public List<Empresa> listarPorEstado(EstadoEmpresa estado) {
            return datos.stream().filter(e -> e.getEstado() == estado).toList();
        }

        @Override
        public List<Empresa> listarTodas() {
            return List.copyOf(datos);
        }
    }

    /** Imita la consulta real: solo devuelve productos de empresas activas. */
    private static class ProductoRepositoryFalso implements ProductoRepositoryPort {
        private final List<Producto> datos = new ArrayList<>();
        private final EmpresaRepositoryFalso empresas;

        ProductoRepositoryFalso(EmpresaRepositoryFalso empresas) {
            this.empresas = empresas;
        }

        void agregar(Long id, String nombre, Long idEmpresa) {
            datos.add(new Producto(id, nombre, null, BigDecimal.TEN, null, 1L, idEmpresa));
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
            return datos.stream()
                    .filter(p -> empresas.buscarPorId(p.getIdEmpresa()).map(Empresa::estaActiva).orElse(false))
                    .filter(p -> idEmpresa == null || p.perteneceA(idEmpresa))
                    .toList();
        }

        @Override
        public boolean existePorId(Long id) {
            return buscarPorId(id).isPresent();
        }

        @Override
        public void eliminar(Long id) {
            datos.removeIf(p -> p.getId().equals(id));
        }
    }
}
