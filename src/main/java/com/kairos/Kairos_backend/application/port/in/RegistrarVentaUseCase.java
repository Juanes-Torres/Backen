package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Venta;

import java.util.List;

/**
 * CASO DE USO: registrar una venta (RF04). Lo usan vendedores y administradores.
 */
public interface RegistrarVentaUseCase {

    Venta registrar(Solicitante solicitante, RegistrarVentaCommand command);

    /**
     * idAlmacen: solo lo envía el administrador (el vendedor siempre vende en su sede).
     * emailCliente: opcional, para asociar la venta a un cliente registrado.
     */
    record RegistrarVentaCommand(Long idAlmacen, String emailCliente, List<Item> items) {
    }

    record Item(Long idProducto, int cantidad) {
    }
}
