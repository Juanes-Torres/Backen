package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.domain.model.Rol;

/**
 * Regla ÚNICA de quién puede ver una venta. La usan el QR de la factura y la consulta
 * de ventas, así la regla no se repite (SOLID: responsabilidad única).
 */
final class PermisosVenta {

    private PermisosVenta() {
    }

    /**
     * La ve un empleado de la empresa de la venta o el cliente que compró.
     * Una venta ajena se trata igual que una que no existe.
     */
    static boolean puedeVer(Solicitante solicitante, Long idEmpresaVenta, Long idClienteVenta) {
        boolean empleadoDeLaEmpresa = (solicitante.rol() == Rol.ADMINISTRADOR || solicitante.rol() == Rol.VENDEDOR)
                && idEmpresaVenta.equals(solicitante.idEmpresa());
        boolean clienteQueCompro = solicitante.rol() == Rol.CLIENTE
                && solicitante.idUsuario().equals(idClienteVenta);
        return empleadoDeLaEmpresa || clienteQueCompro;
    }
}
