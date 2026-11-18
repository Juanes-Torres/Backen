package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regla de quién puede ver o cambiar a quién (cada empresa solo ve lo suyo).
 */
class PermisosUsuarioTest {

    private static final Long TECNO = 1L;
    private static final Long MUNDO_CEL = 2L;

    private final Usuario vendedorTecno = usuario(10L, Rol.VENDEDOR, TECNO);
    private final Usuario vendedorMundoCel = usuario(20L, Rol.VENDEDOR, MUNDO_CEL);
    private final Usuario cliente = usuario(30L, Rol.CLIENTE, null);

    private final Solicitante adminTecno = new Solicitante(1L, Rol.ADMINISTRADOR, TECNO);
    private final Solicitante superadmin = new Solicitante(2L, Rol.SUPERADMIN, null);

    @Test
    void unAdministradorGestionaALosEmpleadosDeSuEmpresa() {
        assertTrue(PermisosUsuario.puedeGestionar(adminTecno, vendedorTecno));
    }

    @Test
    void unAdministradorNoGestionaAEmpleadosDeOtraEmpresaNiAClientes() {
        assertFalse(PermisosUsuario.puedeGestionar(adminTecno, vendedorMundoCel));
        assertFalse(PermisosUsuario.puedeGestionar(adminTecno, cliente));
    }

    @Test
    void elSuperadminGestionaALosClientesPeroNoALosEmpleados() {
        assertTrue(PermisosUsuario.puedeGestionar(superadmin, cliente));
        assertFalse(PermisosUsuario.puedeGestionar(superadmin, vendedorTecno));
    }

    @Test
    void cadaUnoSeGestionaASiMismo() {
        Solicitante elCliente = new Solicitante(30L, Rol.CLIENTE, null);
        Solicitante elVendedor = new Solicitante(10L, Rol.VENDEDOR, TECNO);

        assertTrue(PermisosUsuario.puedeGestionar(elCliente, cliente));
        assertFalse(PermisosUsuario.puedeGestionar(elVendedor, vendedorMundoCel));
    }

    private static Usuario usuario(Long id, Rol rol, Long idEmpresa) {
        Long idAlmacen = rol == Rol.VENDEDOR ? 5L : null;
        return new Usuario(id, "Usuario " + id, "u" + id + "@correo.com", "hash", rol,
                null, idAlmacen, idEmpresa, LocalDate.now(), true);
    }
}
