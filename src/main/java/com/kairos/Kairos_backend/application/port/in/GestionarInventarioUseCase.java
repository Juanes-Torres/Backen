package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.domain.model.TipoMovimiento;

import java.util.List;

/**
 * CASO DE USO: controlar el stock de cada producto en cada almacén.
 */
public interface GestionarInventarioUseCase {

    List<InventarioDetalle> listar(Long idAlmacen);

    List<InventarioDetalle> listarBajoStock();

    InventarioDetalle registrar(Long idProducto, Long idAlmacen, int cantidadInicial, int stockMinimo);

    InventarioDetalle ajustar(Long idInventario, TipoMovimiento tipo, int cantidad);

    InventarioDetalle cambiarStockMinimo(Long idInventario, int stockMinimo);
}
