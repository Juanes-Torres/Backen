package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarInventarioUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.TipoMovimiento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioService implements GestionarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepository;
    private final ProductoRepositoryPort productoRepository;
    private final AlmacenRepositoryPort almacenRepository;

    public InventarioService(InventarioRepositoryPort inventarioRepository,
                             ProductoRepositoryPort productoRepository,
                             AlmacenRepositoryPort almacenRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.almacenRepository = almacenRepository;
    }

    @Override
    public List<InventarioDetalle> listar(Long idAlmacen) {
        return inventarioRepository.listarDetalle(idAlmacen);
    }

    @Override
    public List<InventarioDetalle> listarBajoStock() {
        return inventarioRepository.listarBajoStock();
    }

    @Override
    public InventarioDetalle registrar(Long idProducto, Long idAlmacen, int cantidadInicial, int stockMinimo) {
        if (idProducto == null || !productoRepository.existePorId(idProducto)) {
            throw new RecursoNoEncontradoException("No existe un producto con id " + idProducto);
        }
        Almacen almacen = almacenRepository.buscarPorId(idAlmacen)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un almacén con id " + idAlmacen));
        if (!almacen.estaActivo()) {
            throw new ReglaNegocioException("No se puede registrar inventario en un almacén inactivo");
        }
        if (inventarioRepository.existePorProductoYAlmacen(idProducto, idAlmacen)) {
            throw new RecursoDuplicadoException(
                    "Ese producto ya tiene inventario en ese almacén; use el ajuste de stock");
        }

        Inventario guardado = inventarioRepository.guardar(
                Inventario.nuevo(idProducto, idAlmacen, cantidadInicial, stockMinimo));
        return detalle(guardado.getId());
    }

    @Override
    public InventarioDetalle ajustar(Long idInventario, TipoMovimiento tipo, int cantidad) {
        Inventario inventario = obtener(idInventario);
        inventario.ajustar(tipo, cantidad);          // el dominio impide el stock negativo
        inventarioRepository.guardar(inventario);
        return detalle(idInventario);
    }

    @Override
    public InventarioDetalle cambiarStockMinimo(Long idInventario, int stockMinimo) {
        Inventario inventario = obtener(idInventario);
        inventario.cambiarStockMinimo(stockMinimo);
        inventarioRepository.guardar(inventario);
        return detalle(idInventario);
    }

    private Inventario obtener(Long id) {
        return inventarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un registro de inventario con id " + id));
    }

    private InventarioDetalle detalle(Long id) {
        return inventarioRepository.buscarDetallePorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un registro de inventario con id " + id));
    }
}
