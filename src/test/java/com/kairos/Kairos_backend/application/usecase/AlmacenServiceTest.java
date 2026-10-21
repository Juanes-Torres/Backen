package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.EstadoAlmacen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cada empresa solo ve y cambia SUS sedes (sin Spring ni base de datos).
 */
class AlmacenServiceTest {

    private static final Long TECNO = 1L;
    private static final Long MUNDO_CEL = 2L;

    private AlmacenRepositoryFalso almacenes;
    private AlmacenService servicio;

    @BeforeEach
    void preparar() {
        almacenes = new AlmacenRepositoryFalso();
        servicio = new AlmacenService(almacenes);
        almacenes.guardar(new Almacen(10L, "Sede Centro", "Bogotá", null, EstadoAlmacen.ACTIVO, TECNO));
        almacenes.guardar(new Almacen(20L, "Sede Norte", "Tunja", null, EstadoAlmacen.ACTIVO, MUNDO_CEL));
    }

    @Test
    void cadaEmpresaListaSoloSusSedes() {
        List<Almacen> deTecno = servicio.listar(TECNO);

        assertEquals(1, deTecno.size());
        assertEquals("Sede Centro", deTecno.get(0).getNombre());
    }

    @Test
    void unaSedeDeOtraEmpresaSeTrataComoQueNoExiste() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(TECNO, 20L));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cambiarEstado(TECNO, 20L, false));

        assertEquals(EstadoAlmacen.ACTIVO, almacenes.buscarPorId(20L).orElseThrow().getEstado());
    }

    @Test
    void unaSedeNuevaQuedaEnLaEmpresaDelToken() {
        Almacen creada = servicio.crear(MUNDO_CEL, "Sede Sur", "Bogotá", null);

        assertEquals(MUNDO_CEL, creada.getIdEmpresa());
    }

    @Test
    void unaSedeSinEmpresa_lanzaError() {
        assertThrows(ReglaNegocioException.class, () -> Almacen.nuevo("Sede", "Bogotá", null, null));
    }

    /** Guarda COPIAS, como una base de datos. */
    private static class AlmacenRepositoryFalso implements AlmacenRepositoryPort {
        private final Map<Long, Almacen> datos = new HashMap<>();
        private long siguienteId = 100;

        @Override
        public Almacen guardar(Almacen a) {
            Long id = a.getId() == null ? siguienteId++ : a.getId();
            Almacen copia = new Almacen(id, a.getNombre(), a.getCiudad(), a.getDireccion(), a.getEstado(), a.getIdEmpresa());
            datos.put(id, copia);
            return copia;
        }

        @Override
        public Optional<Almacen> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id)).map(a -> new Almacen(a.getId(), a.getNombre(),
                    a.getCiudad(), a.getDireccion(), a.getEstado(), a.getIdEmpresa()));
        }

        @Override
        public List<Almacen> listarPorEmpresa(Long idEmpresa) {
            return datos.values().stream().filter(a -> a.perteneceA(idEmpresa)).toList();
        }
    }
}
