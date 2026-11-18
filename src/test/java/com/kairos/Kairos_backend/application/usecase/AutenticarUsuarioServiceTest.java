package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.AutenticarUsuarioUseCase.ResultadoLogin;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.CredencialesInvalidasException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba del login SIN Spring, SIN base de datos y SIN JWT (puertos con clases falsas).
 */
class AutenticarUsuarioServiceTest {

    private static final String PASSWORD = "secreta123";

    private UsuarioRepositoryFalso usuarios;
    private EmpresaRepositoryFalso empresas;
    private AutenticarUsuarioService servicio;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryFalso();
        empresas = new EmpresaRepositoryFalso();
        servicio = new AutenticarUsuarioService(usuarios, empresas, new PasswordEncoderFalso(),
                usuario -> "token-de-" + usuario.getEmail());
    }

    @Test
    void unEmpleadoDeUnaEmpresaActivaEntra_yRecibeSuEmpresa() {
        empresas.agregar(1L, EstadoEmpresa.ACTIVA);
        usuarios.agregar("admin@tecno.com", Rol.ADMINISTRADOR, 1L);

        ResultadoLogin resultado = servicio.autenticar("admin@tecno.com", PASSWORD);

        assertEquals("token-de-admin@tecno.com", resultado.token());
        assertEquals(1L, resultado.empresa().getId());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEmpresa.class, names = {"PENDIENTE", "RECHAZADA", "SUSPENDIDA"})
    void unEmpleadoDeUnaEmpresaNoActivaNoEntra(EstadoEmpresa estado) {
        empresas.agregar(1L, estado);
        usuarios.agregar("admin@tecno.com", Rol.ADMINISTRADOR, 1L);

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar("admin@tecno.com", PASSWORD));
    }

    @Test
    void elMensajeExplicaQueLaEmpresaEstaPendiente() {
        empresas.agregar(1L, EstadoEmpresa.PENDIENTE);
        usuarios.agregar("admin@tecno.com", Rol.ADMINISTRADOR, 1L);

        CredencialesInvalidasException error = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar("admin@tecno.com", PASSWORD));

        assertEquals("La empresa aún no ha sido aprobada por el superadministrador", error.getMessage());
    }

    @Test
    void unClienteEntraSinEmpresa() {
        usuarios.agregar("ana@correo.com", Rol.CLIENTE, null);

        ResultadoLogin resultado = servicio.autenticar("ana@correo.com", PASSWORD);

        assertNull(resultado.empresa());
    }

    @Test
    void conContrasenaIncorrecta_noSeRevisaLaEmpresa() {
        empresas.agregar(1L, EstadoEmpresa.PENDIENTE);
        usuarios.agregar("admin@tecno.com", Rol.ADMINISTRADOR, 1L);

        // Mismo mensaje de siempre: no se revela que el email existe ni el estado de su empresa
        CredencialesInvalidasException error = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar("admin@tecno.com", "otra-clave"));

        assertEquals("Email o contraseña incorrectos", error.getMessage());
    }

    // ===== Clases falsas (implementaciones simples de los puertos) =====

    private static class UsuarioRepositoryFalso implements UsuarioRepositoryPort {
        private final List<Usuario> datos = new ArrayList<>();

        void agregar(String email, Rol rol, Long idEmpresa) {
            datos.add(new Usuario((long) datos.size() + 1, "Usuario", email, "cifrado-" + PASSWORD,
                    rol, null, null, idEmpresa, LocalDate.now(), true));
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

        List<Usuario> listarTodos() {
            return List.copyOf(datos);
        }

        @Override
        public List<Usuario> listarPorEmpresa(Long idEmpresa) {
            return datos.stream().filter(u -> u.perteneceA(idEmpresa)).toList();
        }
    }

    private static class EmpresaRepositoryFalso implements EmpresaRepositoryPort {
        private final List<Empresa> datos = new ArrayList<>();

        void agregar(Long id, EstadoEmpresa estado) {
            datos.add(new Empresa(id, "Tecno SAS", "900111222-" + id, "a@tecno.com", null,
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
