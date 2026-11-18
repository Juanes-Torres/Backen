package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.domain.model.TipoMovimiento;

import java.util.List;

/**
 * CASO DE USO: controlar el stock de cada producto en cada almacén de UNA empresa.
 * idEmpresa sale del token: el inventario de otra empresa responde 404.
 */
public interface GestionarInventarioUseCase {

    List<InventarioDetalle> listar(Long idEmpresa, Long idAlmacen);

    List<InventarioDetalle> listarBajoStock(Long idEmpresa);

    InventarioDetalle registrar(Long idEmpresa, Long idProducto, Long idAlmacen, int cantidadInicial, int stockMinimo);

    InventarioDetalle ajustar(Long idEmpresa, Long idInventario, TipoMovimiento tipo, int cantidad);

    InventarioDetalle cambiarStockMinimo(Long idEmpresa, Long idInventario, int stockMinimo);
}
