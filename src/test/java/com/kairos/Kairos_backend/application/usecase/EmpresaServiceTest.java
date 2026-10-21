package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba de la gestión de empresas del superadministrador, sin Spring ni base de datos.
 */
class EmpresaServiceTest {

    private EmpresaRepositoryFalso empresas;
    private EmpresaService servicio;

    @BeforeEach
    void preparar() {
        empresas = new EmpresaRepositoryFalso();
        servicio = new EmpresaService(empresas);
        empresas.guardar(new Empresa(1L, "Tecno SAS", "900111222-3", "a@tecno.com", null,
                EstadoEmpresa.PENDIENTE, null));
        empresas.guardar(new Empresa(2L, "Mundo Cel", "800999888-1", "b@mundo.com", null,
                EstadoEmpresa.ACTIVA, null));
    }

    @Test
    void alAprobarSeGuardaLaEmpresaActiva() {
        servicio.aprobar(1L);

        assertEquals(EstadoEmpresa.ACTIVA, empresas.buscarPorId(1L).orElseThrow().getEstado());
    }

    @Test
    void unCambioNoPermitido_noModificaLaEmpresa() {
        assertThrows(ReglaNegocioException.class, () -> servicio.rechazar(2L));

        assertEquals(EstadoEmpresa.ACTIVA, empresas.buscarPorId(2L).orElseThrow().getEstado());
    }

    @Test
    void unaEmpresaQueNoExiste_respondeNoEncontrada() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.suspender(99L));
    }

    @Test
    void sePuedenFiltrarLasPendientes() {
        List<Empresa> pendientes = servicio.listar(EstadoEmpresa.PENDIENTE);

        assertEquals(1, pendientes.size());
        assertEquals("Tecno SAS", pendientes.get(0).getNombre());
        assertEquals(2, servicio.listar(null).size());
    }

    /** Guarda COPIAS, como una base de datos: si el servicio no llama a guardar, no cambia nada. */
    private static class EmpresaRepositoryFalso implements EmpresaRepositoryPort {
        private final Map<Long, Empresa> datos = new HashMap<>();

        @Override
        public Empresa guardar(Empresa e) {
            datos.put(e.getId(), copia(e));
            return copia(e);
        }

        @Override
        public Optional<Empresa> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id)).map(EmpresaRepositoryFalso::copia);
        }

        @Override
        public boolean existePorNit(String nit) {
            return buscarPorNit(nit).isPresent();
        }

        @Override
        public Optional<Empresa> buscarPorNit(String nit) {
            return datos.values().stream().filter(e -> e.getNit().equals(nit)).findFirst()
                    .map(EmpresaRepositoryFalso::copia);
        }

        @Override
        public List<Empresa> listarPorEstado(EstadoEmpresa estado) {
            return datos.values().stream().filter(e -> e.getEstado() == estado).toList();
        }

        @Override
        public List<Empresa> listarTodas() {
            return List.copyOf(datos.values());
        }

        private static Empresa copia(Empresa e) {
            return new Empresa(e.getId(), e.getNombre(), e.getNit(), e.getEmail(), e.getTelefono(),
                    e.getEstado(), e.getFechaRegistro());
        }
    }
}
