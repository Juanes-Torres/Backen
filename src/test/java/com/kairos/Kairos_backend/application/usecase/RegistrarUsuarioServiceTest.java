package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarUsuarioCommand;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.NotificacionPort;
import com.kairos.Kairos_backend.application.port.out.PasswordEncoderPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba del caso de uso SIN Spring, SIN base de datos y SIN Gmail.
 * Los puertos se reemplazan por clases falsas escritas a mano:
 * esa es la ventaja de la arquitectura hexagonal.
 */
class RegistrarUsuarioServiceTest {

    private UsuarioRepositoryFalso usuarios;
    private NotificacionFalsa notificaciones;
    private RegistrarUsuarioService servicio;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryFalso();
        notificaciones = new NotificacionFalsa();
        servicio = new RegistrarUsuarioService(usuarios, new AlmacenRepositoryFalso(),
                new PasswordEncoderFalso(), notificaciones);
    }

    @Test
    void alRegistrarSeEnviaLaBienvenidaUnaVezConElUsuarioGuardado() {
        Usuario creado = servicio.registrar(comando("ana@correo.com"));

        assertEquals(1, notificaciones.enviados.size());
        Usuario notificado = notificaciones.enviados.get(0);
        assertNotNull(notificado.getId(), "Se debe notificar al usuario ya guardado (con id)");
        assertEquals(creado.getId(), notificado.getId());
        assertEquals("ana@correo.com", notificado.getEmail());
    }

    @Test
    void siElEmailYaExisteNoSeEnviaCorreo() {
        servicio.registrar(comando("ana@correo.com"));
        notificaciones.enviados.clear();

        assertThrows(RecursoDuplicadoException.class,
                () -> servicio.registrar(comando("ana@correo.com")));
        assertTrue(notificaciones.enviados.isEmpty());
    }

    private RegistrarUsuarioCommand comando(String email) {
        return new RegistrarUsuarioCommand("Ana Ruiz", email, "secreta123", Rol.CLIENTE, null, null);
    }

    // ===== Clases falsas (implementaciones simples de los puertos) =====

    /** Anota a quién se le "envió" la bienvenida, sin enviar nada. */
    private static class NotificacionFalsa implements NotificacionPort {
        final List<Usuario> enviados = new ArrayList<>();

        @Override
        public void enviarBienvenida(Usuario usuario) {
            enviados.add(usuario);
        }
    }

    /** Guarda los usuarios en una lista y les asigna id como lo haría la base de datos. */
    private static class UsuarioRepositoryFalso implements UsuarioRepositoryPort {
        private final List<Usuario> datos = new ArrayList<>();

        @Override
        public Usuario guardar(Usuario u) {
            Usuario conId = new Usuario((long) datos.size() + 1, u.getNombre(), u.getEmail(),
                    u.getPasswordHash(), u.getRol(), u.getTelefono(), u.getIdAlmacen(),
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
    }

    /** No hay almacenes: estas pruebas registran clientes. */
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
        public List<Almacen> listarTodos() {
            return List.of();
        }

        @Override
        public boolean existePorId(Long id) {
            return false;
        }
    }

    /** "Cifra" agregando un prefijo: suficiente para la prueba. */
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
