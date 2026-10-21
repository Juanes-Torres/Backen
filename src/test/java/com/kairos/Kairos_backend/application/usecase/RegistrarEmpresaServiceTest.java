package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase.RegistrarEmpresaCommand;
import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase.ResultadoRegistroEmpresa;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba del registro de empresas SIN Spring y SIN base de datos (puertos con clases falsas).
 */
class RegistrarEmpresaServiceTest {

    private EmpresaRepositoryFalso empresas;
    private UsuarioRepositoryFalso usuarios;
    private RegistrarEmpresaService servicio;

    @BeforeEach
    void preparar() {
        empresas = new EmpresaRepositoryFalso();
        usuarios = new UsuarioRepositoryFalso();
        RegistrarUsuarioService registrarUsuario = new RegistrarUsuarioService(usuarios,
                new AlmacenRepositoryFalso(), new PasswordEncoderFalso(), usuario -> { });
        servicio = new RegistrarEmpresaService(empresas, usuarios, registrarUsuario);
    }

    @Test
    void laEmpresaQuedaPendienteYSuAdministradorPerteneceAElla() {
        ResultadoRegistroEmpresa resultado = servicio.registrar(comando("900111222-3", "admin@tecno.com"));

        assertEquals(EstadoEmpresa.PENDIENTE, resultado.empresa().getEstado());
        assertEquals(Rol.ADMINISTRADOR, resultado.administrador().getRol());
        assertEquals(resultado.empresa().getId(), resultado.administrador().getIdEmpresa());
    }

    @Test
    void siElNitYaExiste_noSeGuardaNada() {
        servicio.registrar(comando("900111222-3", "admin@tecno.com"));

        assertThrows(RecursoDuplicadoException.class,
                () -> servicio.registrar(comando("900111222-3", "otro@tecno.com")));
        assertEquals(1, empresas.datos.size());
        assertEquals(1, usuarios.datos.size());
    }

    @Test
    void siElEmailDelAdministradorYaExiste_noSeGuardaLaEmpresa() {
        servicio.registrar(comando("900111222-3", "admin@tecno.com"));

        assertThrows(RecursoDuplicadoException.class,
                () -> servicio.registrar(comando("800999888-1", "admin@tecno.com")));
        assertEquals(1, empresas.datos.size());
        assertTrue(empresas.existePorNit("900111222-3"));
    }

    private RegistrarEmpresaCommand comando(String nit, String emailAdmin) {
        return new RegistrarEmpresaCommand("Tecno SAS", nit, "contacto@tecno.com", null,
                "Laura Díaz", emailAdmin, "secreta123", null);
    }

    // ===== Clases falsas (implementaciones simples de los puertos) =====

    private static class EmpresaRepositoryFalso implements EmpresaRepositoryPort {
        final List<Empresa> datos = new ArrayList<>();

        @Override
        public Empresa guardar(Empresa e) {
            Empresa conId = new Empresa((long) datos.size() + 1, e.getNombre(), e.getNit(), e.getEmail(),
                    e.getTelefono(), e.getEstado(), e.getFechaRegistro());
            datos.add(conId);
            return conId;
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

    private static class UsuarioRepositoryFalso implements UsuarioRepositoryPort {
        final List<Usuario> datos = new ArrayList<>();

        @Override
        public Usuario guardar(Usuario u) {
            Usuario conId = new Usuario((long) datos.size() + 1, u.getNombre(), u.getEmail(),
                    u.getPasswordHash(), u.getRol(), u.getTelefono(), u.getIdAlmacen(), u.getIdEmpresa(),
                    u.getFechaRegistro(), u.isActivo());
            datos.add(conId);
            return conId;
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
        public List<Usuario> listarTodos() {
            return List.copyOf(datos);
        }

        @Override
        public List<Usuario> listarPorEmpresa(Long idEmpresa) {
            return datos.stream().filter(u -> u.perteneceA(idEmpresa)).toList();
        }
    }

    private static class AlmacenRepositoryFalso implements AlmacenRepositoryPort {
        @Override
        public Almacen guardar(Almacen almacen) {
            return almacen;
        }

        @Override
        public Optional<Almacen> buscarPorId(Long id) {
            return Optional.empty();
        }

        @Override
        public List<Almacen> listarPorEmpresa(Long idEmpresa) {
            return List.of();
        }
    }

    private static class PasswordEncoderFalso implements PasswordEncoderPort {
        @Override
        public String cifrar(String passwordPlano) {
            return "cifrado-" + passwordPlano;
        }

        @Override
        public boolean coincide(String passwordPlano, String passwordHash) {
            return passwordHash.equals(cifrar(passwordPlano));
        }
    }
}
